/*
 * Copyright (c) 2026 unihh.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Website: https://unihh.com
 * Author: deversun
 */

package com.unihh.iot.driver.modbus;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.DriverInfo;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.driver.modbus.plugin.ModbusTcpDriverPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modbus 驱动与平台 MQTT 消息总线交互模式单元测试
 */
public class ModbusMqttIntegrationTest {

    @Test
    public void testDriverRegistrationPayload() {
        DriverInfo info = new DriverInfo();
        info.setDriverCode("modbus-tcp");
        info.setDriverName("Modbus TCP 工业协议驱动");
        info.setVersion("2.0.0");
        info.setJarFileName("unihh-driver-modbus.jar");
        info.setStatus(1);
        info.setDescription("支持 Modbus TCP 8大标准功能码");
        info.setServiceUrl("http://192.168.1.100:28011");

        String jsonStr = JSON.toJSONString(info);
        Assert.assertNotNull(jsonStr);

        JSONObject obj = JSON.parseObject(jsonStr);
        Assert.assertEquals("modbus-tcp", obj.getString("driverCode"));
        Assert.assertEquals("Modbus TCP 工业协议驱动", obj.getString("driverName"));
        Assert.assertEquals(Integer.valueOf(1), obj.getInteger("status"));
    }

    @Test
    public void testDeviceConnectionAndPointsPayload() {
        DeviceConfig dev = new DeviceConfig("DEV_MODBUS_TEST", "测试冷水机组PLC", "PK_COOLING", "modbus-tcp", "192.168.1.200", 502);
        dev.setParam("slaveId", 2);
        dev.setBaseIntervalMs(2000);
        dev.setAcqStatus(1);

        List<ThingModel.PropertySpec> points = new ArrayList<>();
        ThingModel.PropertySpec p1 = new ThingModel.PropertySpec("temp_supply", "供水温度", "DOUBLE", "R", "℃");
        p1.setAddress("40001");
        ThingModel.PropertySpec p2 = new ThingModel.PropertySpec("temp_return", "回水温度", "DOUBLE", "R", "℃");
        p2.setAddress("40003");
        points.add(p1);
        points.add(p2);

        JSONObject msg = new JSONObject();
        msg.put("action", "UPDATE");
        msg.put("deviceId", dev.getDeviceId());
        msg.put("driverCode", dev.getDriverCode());
        msg.put("acqStatus", dev.getAcqStatus());
        msg.put("host", dev.getHost());
        msg.put("port", dev.getPort());
        msg.put("connectionParams", dev.getConnectionParams());
        msg.put("slaveId", dev.getParamOrDefault("slaveId", 1));
        msg.put("baseIntervalMs", dev.getBaseIntervalMs());
        msg.put("deviceConfig", dev);
        msg.put("points", points);
        msg.put("timestamp", System.currentTimeMillis());

        String jsonPayload = msg.toJSONString();
        Assert.assertTrue(jsonPayload.contains("192.168.1.200"));
        Assert.assertTrue(jsonPayload.contains("40001"));
        Assert.assertTrue(jsonPayload.contains("temp_supply"));
        Assert.assertTrue(jsonPayload.contains("\"slaveId\":2"));

        // 测试通用驱动引擎 DriverPollingEngine
        com.unihh.iot.protocol.base.DriverPollingEngine engine = new com.unihh.iot.protocol.base.DriverPollingEngine();
        engine.addOrUpdateDevice(dev, points);
        Assert.assertEquals(1, engine.getActiveDeviceCount());

        DeviceConfig saved = engine.getDeviceConfig("DEV_MODBUS_TEST");
        Assert.assertNotNull(saved);
        Assert.assertEquals("192.168.1.200", saved.getHost());
        Assert.assertEquals(502, saved.getPort());
        Assert.assertEquals(Integer.valueOf(2), saved.getParamOrDefault("slaveId", 1));
        Assert.assertEquals(Integer.valueOf(2000), saved.getBaseIntervalMs());

        engine.removeDevice("DEV_MODBUS_TEST");
        Assert.assertEquals(0, engine.getActiveDeviceCount());
    }

    @Test
    public void testEnumDataTypeAndDecodingAt40003() {
        com.unihh.iot.driver.modbus.protocol.frame.ModbusDataType dt = 
                com.unihh.iot.driver.modbus.protocol.frame.ModbusDataType.fromString("ENUM");
        Assert.assertEquals(com.unihh.iot.driver.modbus.protocol.frame.ModbusDataType.ENUM, dt);
        Assert.assertEquals(2, dt.getByteLength());

        DeviceConfig config = new DeviceConfig("DEV_TEST", "测试设备", "pk_test", "modbus-tcp", "127.0.0.1", 502);

        List<ThingModel.PropertySpec> points = new ArrayList<>();
        ThingModel.PropertySpec enumPoint = new ThingModel.PropertySpec("workMode", "工况模式", "ENUM", "RW", "");
        enumPoint.setAddress("40003");
        points.add(enumPoint);

        // 构造 Modbus TCP 响应报文 (针对 40003 单独返回 2 字节数据, 值 = 2):
        byte[] rawResponse = new byte[]{
                0x00, 0x01,
                0x00, 0x00,
                0x00, 0x05,
                0x01,
                0x03,
                0x02,
                0x00, 0x02
        };

        com.unihh.iot.protocol.model.ParsedPayload payload = 
                com.unihh.iot.driver.modbus.protocol.codec.ModbusResponseDecoder.decode(rawResponse, config, points);
        Assert.assertNotNull(payload);
        Assert.assertEquals(1, payload.getPoints().size());

        PointData pd = payload.getPoints().get(0);
        Assert.assertEquals("workMode", pd.getPointCode());
        Assert.assertEquals(1, pd.getQuality());
        Assert.assertEquals(2.0, pd.getValue(), 0.001);
    }

    @Test
    public void testInvokeServiceAndControlMessageHandling() throws Exception {
        com.unihh.iot.protocol.base.DriverPollingEngine engine = new com.unihh.iot.protocol.base.DriverPollingEngine();
        DeviceConfig dev = new DeviceConfig("DEV_001", "1号电机", "pk_demo", "modbus-tcp", "127.0.0.1", 502);
        List<ThingModel.PropertySpec> points = new ArrayList<>();
        ThingModel.PropertySpec p1 = new ThingModel.PropertySpec("targetSpeed", "变频目标转速", "INT", "RW", "RPM");
        p1.setAddress("40007");
        points.add(p1);
        engine.addOrUpdateDevice(dev, points);

        // 验证 engine.findPoint 模糊与精确匹配
        Assert.assertNotNull(engine.findPoint("DEV_001", "targetSpeed"));
        Assert.assertEquals("40007", engine.findPoint("DEV_001", "speed").getAddress());

        // 测试 ModbusTcpDriverPlugin invokeService
        ModbusTcpDriverPlugin plugin = Mockito.spy(new ModbusTcpDriverPlugin());
        Mockito.doReturn(true).when(plugin).writePoint(Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());

        Map<String, Object> serviceParams = new HashMap<>();
        serviceParams.put("speed", 2800);
        serviceParams.put("mode", "AUTO");
        List<Map<String, Object>> resolvedPoints = new ArrayList<>();
        Map<String, Object> rp1 = new HashMap<>();
        rp1.put("pointCode", "speed");
        rp1.put("address", "40007");
        rp1.put("value", 2800);
        rp1.put("dataType", "INT");
        rp1.put("byteOrder", "CDAB");
        resolvedPoints.add(rp1);
        serviceParams.put("resolvedPoints", resolvedPoints);

        boolean ok = plugin.invokeService(dev, "startCoolingFan", serviceParams);
        Assert.assertTrue(ok);
        Mockito.verify(plugin, Mockito.atLeastOnce()).writePoint(Mockito.eq(dev), Mockito.eq("speed"), Mockito.eq("40007"), Mockito.eq(2800), Mockito.eq("INT"));

        // 测试 BaseDriverMqttManager handleControlMessage 消费 INVOKE_SERVICE
        com.unihh.iot.protocol.base.BaseDriverMqttManager mqttManager = new com.unihh.iot.protocol.base.BaseDriverMqttManager();

        // 反射注入 mock plugin 与 engine
        java.lang.reflect.Field pluginField = com.unihh.iot.protocol.base.BaseDriverMqttManager.class.getDeclaredField("driverPlugin");
        pluginField.setAccessible(true);
        pluginField.set(mqttManager, plugin);

        java.lang.reflect.Field engineField = com.unihh.iot.protocol.base.BaseDriverMqttManager.class.getDeclaredField("pollingEngine");
        engineField.setAccessible(true);
        engineField.set(mqttManager, engine);

        Method handleControl = com.unihh.iot.protocol.base.BaseDriverMqttManager.class.getDeclaredMethod("handleControlMessage", String.class, String.class);
        handleControl.setAccessible(true);

        JSONObject ruleDownlink = new JSONObject();
        ruleDownlink.put("messageId", "rule_test_001");
        ruleDownlink.put("action", "INVOKE_SERVICE");
        ruleDownlink.put("deviceId", "DEV_001");
        ruleDownlink.put("driverCode", "modbus-tcp");
        ruleDownlink.put("serviceCode", "startCoolingFan");
        JSONObject pJson = new JSONObject();
        pJson.put("speed", 2800);
        ruleDownlink.put("params", pJson);
        ruleDownlink.put("resolvedPoints", resolvedPoints);

        // 第一次执行，正常分发调用 invokeService
        handleControl.invoke(mqttManager, ruleDownlink.toJSONString(), "modbus-tcp");
        Mockito.verify(plugin, Mockito.atLeastOnce()).invokeService(Mockito.any(), Mockito.eq("startCoolingFan"), Mockito.any());

        // 第二次执行 (模拟同时推送到专属主题导致重复下发)，幂等去重跳过
        handleControl.invoke(mqttManager, ruleDownlink.toJSONString(), "modbus-tcp");

        engine.removeDevice("DEV_001");
    }

    @Test
    public void testDoubleAndFloatEndiannessEncoding() {
        // 1. 测试 32位 Float CDAB 编码与解码对齐 (0.88f)
        float fVal = 0.88f;
        int[] fRegs = com.unihh.iot.protocol.util.IndustrialEndianUtils.toRegistersFloat32(fVal, "CDAB");
        Assert.assertEquals(2, fRegs.length);
        byte[] fBytes = new byte[4];
        fBytes[0] = (byte) ((fRegs[0] >> 8) & 0xFF);
        fBytes[1] = (byte) (fRegs[0] & 0xFF);
        fBytes[2] = (byte) ((fRegs[1] >> 8) & 0xFF);
        fBytes[3] = (byte) (fRegs[1] & 0xFF);
        float decodedFloat = com.unihh.iot.protocol.util.IndustrialEndianUtils.readFloat32(fBytes, 0, "CDAB");
        Assert.assertEquals(fVal, decodedFloat, 0.0001f);

        // 2. 测试 64位 Double CDAB 编码与解码对齐 (0.66)
        double dVal = 0.66;
        int[] dRegs = com.unihh.iot.protocol.util.IndustrialEndianUtils.toRegistersFloat64(dVal, "CDAB");
        Assert.assertEquals(4, dRegs.length);
        byte[] dBytes = new byte[8];
        for (int i = 0; i < 4; i++) {
            dBytes[i * 2] = (byte) ((dRegs[i] >> 8) & 0xFF);
            dBytes[i * 2 + 1] = (byte) (dRegs[i] & 0xFF);
        }
        double decodedDouble = com.unihh.iot.protocol.util.IndustrialEndianUtils.readFloat64(dBytes, 0, "CDAB");
        Assert.assertEquals(dVal, decodedDouble, 0.000001);

        // 3. 测试 64位 Double ABCD 编码与解码对齐
        int[] dRegsAbcd = com.unihh.iot.protocol.util.IndustrialEndianUtils.toRegistersFloat64(dVal, "ABCD");
        byte[] dBytesAbcd = new byte[8];
        for (int i = 0; i < 4; i++) {
            dBytesAbcd[i * 2] = (byte) ((dRegsAbcd[i] >> 8) & 0xFF);
            dBytesAbcd[i * 2 + 1] = (byte) (dRegsAbcd[i] & 0xFF);
        }
        double decodedAbcd = com.unihh.iot.protocol.util.IndustrialEndianUtils.readFloat64(dBytesAbcd, 0, "ABCD");
        Assert.assertEquals(dVal, decodedAbcd, 0.000001);
    }

    @Test
    public void testDeviceHealthStatusPublishing() throws Exception {
        com.unihh.iot.protocol.base.DriverPollingEngine engine = new com.unihh.iot.protocol.base.DriverPollingEngine();
        com.unihh.iot.protocol.base.BaseDriverMqttManager mqttManager = Mockito.mock(com.unihh.iot.protocol.base.BaseDriverMqttManager.class);
        ModbusTcpDriverPlugin plugin = Mockito.mock(ModbusTcpDriverPlugin.class);

        java.lang.reflect.Field pluginField = com.unihh.iot.protocol.base.DriverPollingEngine.class.getDeclaredField("driverPlugin");
        pluginField.setAccessible(true);
        pluginField.set(engine, plugin);

        java.lang.reflect.Field mqttField = com.unihh.iot.protocol.base.DriverPollingEngine.class.getDeclaredField("mqttManager");
        mqttField.setAccessible(true);
        mqttField.set(engine, mqttManager);

        DeviceConfig dev = new DeviceConfig("DEV_HEALTH_TEST", "健康度测试设备", "PK_TEST", "modbus-tcp", "127.0.0.1", 502);
        List<ThingModel.PropertySpec> points = new ArrayList<>();
        ThingModel.PropertySpec p = new ThingModel.PropertySpec("temp", "温度", "DOUBLE", "R", "℃");
        p.setAddress("40001");
        points.add(p);

        // 1. 模拟采集成功 (quality=1)
        com.unihh.iot.protocol.model.ParsedPayload successPayload = new com.unihh.iot.protocol.model.ParsedPayload();
        successPayload.setDeviceId("DEV_HEALTH_TEST");
        PointData goodPd = new PointData("PK_TEST", "DEV_HEALTH_TEST", "temp", 25.5, System.currentTimeMillis());
        goodPd.setQuality(1);
        successPayload.getPoints().add(goodPd);
        Mockito.when(plugin.readPoints(Mockito.any(), Mockito.any())).thenReturn(successPayload);

        engine.addOrUpdateDevice(dev, points);

        Method pollMethod = com.unihh.iot.protocol.base.DriverPollingEngine.class.getDeclaredMethod("pollDevice", String.class);
        pollMethod.setAccessible(true);
        pollMethod.invoke(engine, "DEV_HEALTH_TEST");

        // 验证上报在线状态 1
        Mockito.verify(mqttManager, Mockito.times(1)).publishDeviceStatus(Mockito.eq("DEV_HEALTH_TEST"), Mockito.eq(1), Mockito.anyString());

        // 2. 模拟采集失败第一次 (quality=0)
        com.unihh.iot.protocol.model.ParsedPayload failPayload = new com.unihh.iot.protocol.model.ParsedPayload();
        failPayload.setDeviceId("DEV_HEALTH_TEST");
        PointData badPd = new PointData("PK_TEST", "DEV_HEALTH_TEST", "temp", 0.0, System.currentTimeMillis());
        badPd.setQuality(0);
        badPd.setRawValue("COMM_FAILURE: Connection refused");
        failPayload.getPoints().add(badPd);
        Mockito.when(plugin.readPoints(Mockito.any(), Mockito.any())).thenReturn(failPayload);

        pollMethod.invoke(engine, "DEV_HEALTH_TEST");
        // 第一次失败防抖，不应上报离线
        Mockito.verify(mqttManager, Mockito.never()).publishDeviceStatus(Mockito.eq("DEV_HEALTH_TEST"), Mockito.eq(0), Mockito.anyString());

        // 3. 模拟连续第二次失败，触发确诊离线
        pollMethod.invoke(engine, "DEV_HEALTH_TEST");
        Mockito.verify(mqttManager, Mockito.times(1)).publishDeviceStatus(Mockito.eq("DEV_HEALTH_TEST"), Mockito.eq(0), Mockito.anyString());

        // 4. 再次自愈恢复
        Mockito.when(plugin.readPoints(Mockito.any(), Mockito.any())).thenReturn(successPayload);
        pollMethod.invoke(engine, "DEV_HEALTH_TEST");
        Mockito.verify(mqttManager, Mockito.times(2)).publishDeviceStatus(Mockito.eq("DEV_HEALTH_TEST"), Mockito.eq(1), Mockito.anyString());

        engine.removeDevice("DEV_HEALTH_TEST");
    }
}
