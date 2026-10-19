# Architecture

## Overview

```text
+-----------------------------------------------------------------------------------+
|                  Physical devices and field buses (physical layer)                 |
|      Modbus PLC/meters  |  Siemens S7-200/300/1200/1500  |  OPC UA Server / SCADA  |
+----------------------------------------+------------------------------------------+
                                         | Field network (TCP/IP / serial RS485)
                                         v
+-----------------------------------------------------------------------------------+
|                    Distributed independent protocol drivers (southbound layer)     |
|   [Modbus driver :28011]   |   [Siemens S7 driver :28012]  |  [OPC UA driver :28013]|
|   Extends AbstractDriverPlugin; MQTT communication managed by BaseDriverMqttManager|
+----------------------------------------+------------------------------------------+
                                         | EMQX MQTT bus (data / heartbeat / LWT / control)
                                         v
+-----------------------------------------------------------------------------------+
|                              Core message bus (messaging layer)                    |
|                  EMQX Broker cluster  <=======>  Redis cache / online state        |
+----------------------------------------+------------------------------------------+
                                         | Shared subscriptions / dedicated topics
                                         v
+-----------------------------------------------------------------------------------+
|                        Core business microservices (application layer)             |
|  [Gateway unihh-gateway :28000]                                                    |
|       |-- REST/JWT routing & auth --> [Admin unihh-admin :28001] (models/devices)  |
|       `-- REST data forwarding ----> [Data unihh-data :28002] (rules/persistence)  |
+----------------------------------------+------------------------------------------+
                                         | Strategy routing (StorageEngineRouter)
                                         v
+-----------------------------------------------------------------------------------+
|                             Pluggable persistence (storage layer)                  |
|    TDengine (time series)  |  TimescaleDB / InfluxDB  |  MySQL 8.0  |  Redis cache |
+-----------------------------------------------------------------------------------+
```

## Services

### Independent microservice processes

| Module | Type | Default port | Responsibility |
| :--- | :--- | :--- | :--- |
| `unihh-gateway` | Microservice | 28000 | Single entry point, REST routing, CORS, JWT auth interception |
| `unihh-admin` | Microservice | 28001 | RBAC, device registry, thing models, driver lifecycle, control commands |
| `unihh-data` | Microservice | 28002 | Point normalization, rule computing, alert triggering, storage routing |
| `unihh-driver-modbus` | Driver | 28011 | Modbus TCP/RTU acquisition and register write-back |
| `unihh-driver-s7` | Driver | 28012 | Siemens S7-200 SMART/300/1200/1500, DB blocks and memory areas |
| `unihh-driver-opcua` | Driver | 28013 | OPC UA NodeId acquisition and method calls |

### Shared Maven SDK modules

| Module | Responsibility |
| :--- | :--- |
| `unihh-common` | Shared models (`PointData`, `DriverInfo`, `DeviceConfig`), unified response body, EMQX client foundation |
| `unihh-protocol` | Driver base class `AbstractDriverPlugin`, polling engine `DriverPollingEngine`, endpoint controller `BaseDriverController`, `IndustrialEndianUtils` |
| `unihh-storage` | Storage interface `DataStorageEngine`, router `StorageEngineRouter`, built-in Memory/Redis/MySQL/TDengine engines |

## Technology Stack

| Layer | Technology | Notes |
| :--- | :--- | :--- |
| Framework | Java 8 / 17 + Spring Boot | Lean service runtime |
| Build | Maven | Multi-module dependency management |
| Gateway | Spring Web | Routing, CORS, JWT interception |
| Message bus | EMQX 5.x | MQTT 5.0 shared subscriptions and retained messages |
| Driver foundation | `unihh-protocol` | Base classes, polling engine, LWT keep-alive, REST debug endpoints |
| Persistence | TDengine 3.x / TimescaleDB / MySQL | Time-series and configuration data |
| Cache | Redis | Real-time point mirror, tokens and sessions |
| Frontend | Vue 3 + Element Plus + Vite + ECharts | Admin console and industrial dashboards |

## Core Data Flows

### Driver lifecycle and last-will keep-alive

```text
[Driver process]             [EMQX Broker]                    [Admin unihh-admin]
      |                           |                                   |
      |-- 1. setWill(LWT) ------->| (last-will registered, retained)  |
      |-- 2. connect() ---------->|                                   |
      |-- 3. publish(ONLINE) ---->|---------------------------------->| (sync driver online)
      |-- 4. register ----------->|-- iot/driver/register ----------->| (store registration)
      |                           |                                   |-- push devices & points
      |<-- config/update ---------|<-- iot/driver/{code}/device/config|
      |                           |                                   |
      | [heartbeat every 5s]      |                                   |
      |-- 5. heartbeat ---------->|-- iot/driver/heartbeat ---------->| (refresh watchdog)
      |                           |                                   |
   [crash / power loss]           |                                   |
      x                           |-- 6. Broker publishes LWT ------->| (mark offline)
                                  |   (iot/driver/{code}/status)      |-- cascade devices offline
                                  |                                   |
                                  |                                   | [Watchdog patrol]
                                  |                                   | (offline after 15s silence)
```

Key points:

- The admin center senses driver shutdown or crash within seconds, either via LWT or the watchdog;
- The watchdog is the second line of defense: a driver is declared offline after 15 seconds without a heartbeat;
- When a driver goes offline, its devices are cascaded offline to avoid phantom online states.

### Acquisition and storage fan-out

```text
[Devices] --> [Driver PollingEngine] --> [EMQX iot/driver/+/data] --> [Data unihh-data] --> [StorageEngineRouter]
 (PLC/etc.)    decode / eng. value /        shared subscription       rule & alert checks     +-- TDengine (time series)
               quality flag                 consumption               cleansing               +-- MySQL (records)
                                                                                             `-- Redis (live snapshot)
```

### Write-back control chain

```text
[UI / caller] --> [Gateway :28000] --> [Admin :28001] --> [EMQX iot/driver/{code}/cmd] --> [Driver] --> [Device]
  write request     routing / auth       build command          dedicated topic           channel lock     register write
                                                                                       writePoint(...)
```

Reads and writes on half-duplex buses (e.g. RS485) are queued through the channel lock automatically — see the [Driver Management](../manual/drivers.md) manual.

## MQTT Topics

All driver interactions run over MQTT topics. The complete topic table lives in the [MQTT Topic Reference](../reference/mqtt-topics.md).
