# 驱动开发指南

平台南向驱动基于 `unihh-protocol` 底座开发。MQTT 连接、注册、心跳、配置同步、反控指令监听等样板能力全部由底座托管，开发者只需实现协议报文的编解码逻辑。

## 三步交付一个驱动

### 第一步：创建工程并引入 SDK

新建 Maven 工程，引入 `unihh-protocol` 依赖：

```xml
<dependency>
    <groupId>com.unihh.iot</groupId>
    <artifactId>unihh-protocol</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 第二步：继承 `AbstractDriverPlugin` 实现核心逻辑

```java
package com.unihh.iot.driver.custom;

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.protocol.annotation.IoTDriver;
import com.unihh.iot.protocol.base.AbstractDriverPlugin;
import com.unihh.iot.protocol.model.DriverMetadata;
import com.unihh.iot.protocol.model.ParsedPayload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@IoTDriver(code = "custom-protocol", name = "自定义工业协议驱动", version = "2.0.0")
public class CustomDriverPlugin extends AbstractDriverPlugin {

    @Override
    public String getDriverCode() {
        return "custom-protocol";
    }

    @Override
    public DriverMetadata getMetadata() {
        DriverMetadata meta = new DriverMetadata();
        meta.setDriverCode(getDriverCode());
        meta.setDriverName("自定义工业协议驱动");
        meta.setVersion("2.0.0");
        return meta;
    }

    @Override
    public boolean openConnection(DeviceConfig config) throws Exception {
        // 与物理设备握手建立长连接
        return true;
    }

    @Override
    public ParsedPayload readPoints(DeviceConfig config, List<ThingModel.PropertySpec> points) throws Exception {
        ParsedPayload payload = new ParsedPayload();
        // 批量采集点位并填充：
        // payload.addPoint(pointCode, value, quality, timestamp)
        return payload;
    }

    @Override
    public boolean writePoint(DeviceConfig config, String pointCode, String address, Object value, String dataType) throws Exception {
        // 反向控制：编码写入报文并发送至设备
        return true;
    }
}
```

三个核心方法的职责：

| 方法 | 触发时机 | 职责 |
| :--- | :--- | :--- |
| `openConnection` | 设备配置下发后 | 与设备建立连接，失败时返回 `false` 由底座安排重试 |
| `readPoints` | 按设备数采周期轮询 | 批量读取点位，填充工程值、品质与时间戳 |
| `writePoint` | 收到反控指令 | 编码写报文并发送，返回是否写入成功 |

::: tip 通道互斥
`readPoints` 与 `writePoint` 的调用已由底座通过 `getChannelLock` 串行化。针对 RS485 半双工总线或单会话 TCP 设备，你不需要（也不应该）在插件内部再为同一设备加锁。
:::

### 第三步：编写启动入口并运行

```java
package com.unihh.iot.driver.custom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.unihh.iot"})
public class CustomDriverApplication {
    public static void main(String[] args) {
        System.setProperty("server.port", "28019");
        SpringApplication.run(CustomDriverApplication.class, args);
    }
}
```

驱动启动后，底座自动完成在主平台的在线注册、心跳维持、配置拉取与采集调度。打开平台【驱动管理】页面，片刻后即可看到新驱动卡片出现并显示在线。

## 底座提供了什么

| 能力 | 说明 |
| :--- | :--- |
| 在线自注册 | 驱动启动后向 `iot/driver/register` 广播元数据与 HTTP 宿主地址 |
| 周期心跳 | 默认每 5 秒上报心跳与活跃通道指标 |
| LWT 掉线遗嘱 | 进程崩溃时由 EMQX 广播遗嘱消息，平台秒级感知 |
| 配置热同步 | 平台修改设备/点位后经 `iot/device/config/update` 热通知，无需重启驱动 |
| 采集调度 | `DriverPollingEngine` 按设备数采周期自动调度 `readPoints` |
| 反控监听 | 订阅 `iot/driver/{code}/cmd`，解码后调用 `writePoint` |
| REST 调试接口 | 内置 `/api/v1/driver/status` 状态端点，供平台 Ping 自检 |

## 数据类型与点位映射

物模型属性支持的数据类型：`FLOAT`、`DOUBLE`、`INT`、`INT32`、`INT64`、`BOOL`、`STRING`、`ENUM`。

设备绑定点位时可配置：

- **物理寄存器地址**：按协议语义填写（如 Modbus 的 `40001`、S7 的 `DB1.DBW0`）；
- **缩放系数（Scale）与零点偏置（Offset）**：工程值 = 原始值 × Scale + Offset，在平台侧自动换算，驱动只需上送原始值；
- **采样触发模式**：`PERIODIC`（周期采样）或 `COV`（值变化上报）。

## 参考现有驱动

仓库内置三个可直接参考的驱动实现：

| 驱动 | 位置 | 参考价值 |
| :--- | :--- | :--- |
| Modbus | `drivers/unihh-driver-modbus` | TCP/RTU 双模式、多从站轮询、寄存器反写 |
| 西门子 S7 | `drivers/unihh-driver-s7` | S7-200 SMART/300/1200/1500，DB 块与各存储区读写 |
| OPC UA | `drivers/unihh-driver-opcua` | NodeId 订阅采集与方法调用 |

## 贡献新驱动

欢迎为 BACnet、DL/T 645、三菱 MC、欧姆龙 FINS 等协议贡献驱动插件。提交流程与规范见仓库 [README](https://github.com/unihh-iot/UNIHH) 的贡献指南章节。
