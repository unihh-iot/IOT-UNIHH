# 系统架构

## 整体架构

自上而下分为五层：物理设备经现场总线接入独立协议驱动，驱动与核心微服务之间全部交互走 EMQX 消息总线，存储层按数据形态策略路由落库。

![UNIHH-IOT 整体架构](/images/architecture.svg)

## 服务拆分

### 独立微服务进程

| 模块名称 | 进程类型 | 默认端口 | 核心职责 |
| :--- | :--- | :--- | :--- |
| `unihh-gateway` | 微服务 | 28000 | 统一访问入口、REST 路由分发、CORS 处理、JWT 鉴权拦截 |
| `unihh-admin` | 微服务 | 28001 | 用户角色 RBAC、设备台账、物模型定义、驱动生命周期托管、控制指令下发 |
| `unihh-data` | 微服务 | 28002 | 点位清洗归一化、规则引擎运算、告警触发、存储引擎持久化分流 |
| `unihh-driver-modbus` | 驱动服务 | 28011 | Modbus TCP/RTU 采集与寄存器反写 |
| `unihh-driver-s7` | 驱动服务 | 28012 | 西门子 S7-200 SMART/300/1200/1500，DB 块及各存储区读写 |
| `unihh-driver-opcua` | 驱动服务 | 28013 | OPC UA NodeId 节点采集与方法调用 |

### 共享 Maven SDK 模块

| 模块名称 | 核心职责 |
| :--- | :--- |
| `unihh-common` | 全局通用模型（`PointData`、`DriverInfo`、`DeviceConfig`）、统一响应体、EMQX MQTT 客户端底座 |
| `unihh-protocol` | 驱动基类 `AbstractDriverPlugin`、采集轮询引擎 `DriverPollingEngine`、端点控制器 `BaseDriverController`、工业大小端工具 `IndustrialEndianUtils` |
| `unihh-storage` | 策略存储接口 `DataStorageEngine`、路由适配器 `StorageEngineRouter`，内置 Memory/Redis/MySQL/TDengine 实现 |

## 技术选型

| 层级 | 组件/技术 | 说明 |
| :--- | :--- | :--- |
| 基础框架 | Java 8 / 17 + Spring Boot | 核心开发语言与轻量框架生态 |
| 构建管理 | Maven | 统一管理全局与子模块依赖 |
| 网关中心 | Spring Web | API 路由转发、跨域配置、JWT 鉴权拦截 |
| 消息总线 | EMQX 5.x | 数采与控制总线，支持 MQTT 5.0 共享订阅与保留消息 |
| 南向驱动底座 | `unihh-protocol` | 抽象驱动基类、周期数采引擎、LWT 遗嘱保活、REST 调试接口 |
| 持久化存储 | TDengine 3.x / TimescaleDB / MySQL | 时序数据、配置数据分类落库 |
| 高速缓存 | Redis | 点位实时值镜像、用户 Token 与会话保持 |
| 前端交互 | Vue 3 + Element Plus + Vite + ECharts | 管理控制台与工业看板 |

## 核心数据流

### 驱动生命周期与遗嘱保活

驱动启动即向平台自动注册，此后以 5 秒周期心跳保活；异常掉线由 LWT 遗嘱与看门狗双重兜底。

![驱动生命周期与遗嘱保活时序](/images/driver-lifecycle.svg)

要点：

- 驱动正常下线或异常崩溃，管理中心都能在秒级感知；
- 看门狗在 MQTT 遗嘱之外提供第二重保障：心跳超过 15 秒未更新即判定离线；
- 驱动离线会级联将其下挂设备标记为离线，避免虚假在线状态。

### 采集与存储分流

采集数据经总线进入数据中心，完成规则计算与告警判定后按数据形态路由落库。

![采集与存储分流](/images/data-flow.svg)

### 反向控制链路

控制指令从平台侧经网关鉴权后，通过专属 MQTT 主题定向到达驱动，在通道锁内串行写入设备。

![反向控制链路](/images/control-flow.svg)

半双工总线（如 RS485）上的读写在通道锁内自动排队串行执行，详见[驱动管理操作说明](../manual/drivers.md)。

## MQTT 主题规范

平台所有驱动交互均基于 MQTT 主题，完整主题表见 [MQTT 主题参考](../reference/mqtt-topics.md)。
