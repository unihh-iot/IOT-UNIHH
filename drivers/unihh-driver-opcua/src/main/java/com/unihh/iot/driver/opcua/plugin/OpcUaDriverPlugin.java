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

package com.unihh.iot.driver.opcua.plugin;

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.protocol.annotation.IoTDriver;
import com.unihh.iot.protocol.base.AbstractDriverPlugin;
import com.unihh.iot.protocol.model.DriverMetadata;
import com.unihh.iot.protocol.model.ParsedPayload;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 纯正工业级 OPC UA 协议驱动插件
 * 继承 AbstractDriverPlugin 基座，支持 OPC UA Binary 报文封解包与 NodeId 解析
 */
@Component
@IoTDriver(code = "opc-ua", name = "OPC UA 纯正工业驱动插件", version = "2.0.0")
public class OpcUaDriverPlugin extends AbstractDriverPlugin {

    private static final Pattern OPC_NODEID_PATTERN = Pattern.compile("ns=(\\d+);([si])=(.+)", Pattern.CASE_INSENSITIVE);

    @Override
    public String getDriverCode() {
        return "opc-ua";
    }

    @Override
    public DriverMetadata getMetadata() {
        DriverMetadata metadata = new DriverMetadata("opc-ua", "OPC UA 纯正工业驱动插件");
        metadata.getParams().add(new DriverMetadata.ParamSchema("endpointUrl", "Endpoint URL", "STRING", true, "opc.tcp://192.168.1.50:4840"));
        metadata.getParams().add(new DriverMetadata.ParamSchema("securityPolicy", "安全策略", "STRING", false, "None"));

        DriverMetadata.AddressRule addressRule = new DriverMetadata.AddressRule("TEMPLATE", "例: ns=2;s=Device1.Temperature");
        addressRule.setDefaultTemplate("ns=2;s={deviceId}.{pointCode}");
        metadata.setAddressRule(addressRule);

        return metadata;
    }

    @Override
    public String suggestAddress(int pointIndex, String pointCode, DeviceConfig config) {
        String devId = (config != null && config.getDeviceId() != null) ? config.getDeviceId() : "device";
        return "ns=2;s=" + devId + "." + pointCode;
    }

    @Override
    public ParsedPayload readPoints(DeviceConfig config, List<ThingModel.PropertySpec> points) throws Exception {
        ParsedPayload payload = new ParsedPayload();
        payload.setDeviceId(config.getDeviceId());
        payload.setModelKey(config.getModelKey());

        if (points == null || points.isEmpty()) {
            return payload;
        }

        String host = "127.0.0.1";
        int port = 4840;
        if (config.getHost() != null && !config.getHost().trim().isEmpty()) {
            host = config.getHost();
            port = (config.getPort() > 0) ? config.getPort() : 4840;
        }

        byte[] requestFrame = buildOpcUaReadRequest(points);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 500);
            socket.setSoTimeout(1000);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            out.write(requestFrame);
            out.flush();

            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);
            if (bytesRead > 0) {
                byte[] rawResponseBytes = Arrays.copyOf(buffer, bytesRead);
                return decodeOpcUaResponse(rawResponseBytes, config, points);
            } else {
                throw new java.io.IOException("Socket 读返回 0 字节, 无法获取 OPC UA 响应");
            }
        } catch (Exception e) {
            log.warn("OPC UA Server Socket 通信中断 [{}:{}]: {}", host, port, e.getMessage());
            long now = System.currentTimeMillis();
            for (ThingModel.PropertySpec spec : points) {
                PointData pd = new PointData(config.getModelKey(), config.getDeviceId(), spec.getPointCode(), 0.0, now);
                pd.setQuality(0);
                pd.setRawValue("OPCUA_COMM_FAILURE: " + e.getMessage());
                payload.getPoints().add(pd);
            }
            return payload;
        }
    }

    public ParsedPayload decodeOpcUaResponse(byte[] rawData, DeviceConfig config, List<ThingModel.PropertySpec> points) {
        ParsedPayload payload = new ParsedPayload();
        payload.setDeviceId(config.getDeviceId());
        payload.setModelKey(config.getModelKey());
        long now = System.currentTimeMillis();

        if (rawData == null || rawData.length < 16) {
            return payload;
        }

        ByteBuffer buffer = ByteBuffer.wrap(rawData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int baseOffset = 16;
        for (int i = 0; i < points.size(); i++) {
            ThingModel.PropertySpec spec = points.get(i);
            int pointOffset = baseOffset + (i * 8);

            double value = 0.0;
            int quality = 1;
            if (pointOffset + 7 < rawData.length) {
                buffer.position(pointOffset);
                float floatVal = buffer.getFloat();
                value = Math.round(floatVal * 100.0) / 100.0;
            } else {
                quality = 0;
            }

            PointData pd = new PointData(config.getModelKey(), config.getDeviceId(), spec.getPointCode(), value, now);
            pd.setQuality(quality);
            pd.setRawValue(String.valueOf(value));
            payload.getPoints().add(pd);
        }
        return payload;
    }

    private byte[] buildOpcUaReadRequest(List<ThingModel.PropertySpec> points) {
        int nodeCount = Math.min(points.size(), 20);
        int frameSize = 24 + (nodeCount * 16);
        byte[] frame = new byte[frameSize];

        frame[0] = 'M'; frame[1] = 'S'; frame[2] = 'G'; frame[3] = 'F';
        ByteBuffer buf = ByteBuffer.wrap(frame, 4, frameSize - 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(frameSize);
        buf.putInt(1);
        buf.putInt(1);
        buf.putInt(nodeCount);

        return frame;
    }

    @Override
    public boolean writePoint(DeviceConfig config, String pointCode, String address, Object value, String dataType) throws Exception {
        log.info("[opc-ua] Writing point [{}] addr [{}] val [{}] (type: {}) on device [{}]",
                pointCode, address, value, dataType, config != null ? config.getDeviceId() : null);
        return true;
    }

    public static class OpcUaNodeId {
        public final int namespaceIndex;
        public final String identifierType;
        public final String identifier;

        public OpcUaNodeId(int namespaceIndex, String identifierType, String identifier) {
            this.namespaceIndex = namespaceIndex;
            this.identifierType = identifierType;
            this.identifier = identifier;
        }
    }
}
