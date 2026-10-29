<div align="right">
  <strong><a href="README.zh.md">简体中文</a></strong> | <strong><a href="README.md">English</a></strong>
</div>

# UNIHH-IOT Industrial IoT Data Acquisition & Device Management Platform

[![UNIHH-IOT](https://img.shields.io/badge/UNIHH--IOT-Platform-blue.svg?style=flat-square)](https://github.com/unihh-iot/UNIHH)
[![JDK 17](https://img.shields.io/badge/JDK-17+-orange.svg?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1.svg?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-6.0+-DC382D.svg?style=flat-square&logo=redis&logoColor=white)](https://redis.io/)
[![EMQX](https://img.shields.io/badge/EMQX-5.x-009A61.svg?style=flat-square&logo=mqtt&logoColor=white)](https://www.emqx.com/)
[![TDengine](https://img.shields.io/badge/TDengine-3.x-006BE6.svg?style=flat-square)](https://tdengine.com/)
[![GitHub stars](https://img.shields.io/github/stars/unihh-iot/UNIHH?style=flat-square&logo=github)](https://github.com/unihh-iot/UNIHH/stargazers)
[![Gitee stars](https://gitee.com/giteeRepo/unihh/badge/star.svg?theme=dark)](https://gitee.com/giteeRepo/unihh/stargazers)

## 1. Project Overview

UNIHH-IOT is a high-performance, lightweight industrial IoT data acquisition and asset management platform designed for modern intelligent manufacturing and edge-cloud collaboration scenarios. The platform provides south-bound multi-protocol data acquisition, dynamic thing-model abstraction, distributed rule computing, multi-engine persistence routing, and north-bound reverse control with physical channel lifecycle management.

### 1.1 Key Highlights

- **Bidirectional Read & Write with Full-Chain Reverse Control**: Comprehensive support for high-concurrency south-bound point polling (Read / Decode) and north-bound sub-millisecond command dispatching with device register/property overriding (Write / Encode).
- **Distributed Standalone Driver Architecture**: Protocol drivers run as isolated microservice processes (e.g., Modbus, Siemens S7, OPC UA). This eliminates class isolation defects, memory leaks, and cascading failures inherent in legacy monolithic in-process ClassLoader / SPI mechanisms.
- **Automated Lifecycle via EMQX MQTT Bus**: Built on `AbstractDriverPlugin` and `BaseDriverMqttManager`, drivers are fully managed out-of-the-box: auto-registration, heartbeat maintenance, LWT disconnect will messages (`retained=true`), dynamic configuration synchronization, and control command listening.
- **Channel-Level Fair Mutual Exclusion Lock**: For industrial half-duplex RS485 buses and single-session TCP channels, the driver provides target device link-level fair mutual exclusion locks (`getChannelLock`) to serialize read and write operations, eliminating packet interleaving and out-of-order frame collisions.
- **Standardized Thing-Model Abstraction**: Native support for Properties, Services, and Events, enabling dynamic mapping between physical addresses and logical telemetry points.
- **Multi-Engine Storage Strategy (`unihh-storage`)**: Employs Strategy and Factory routing patterns with support for single-record (`save`) and batch (`saveBatch`) persistence across TDengine, TimescaleDB, InfluxDB, ClickHouse, MySQL, Redis, and high-frequency in-memory ring buffers.
- **Bare-Metal & JAR-Friendly Deployment**: Microservices communicate using explicit configuration files (`application.yml` / environment variables), supporting direct `java -jar` operations without heavy service registry dependencies.
- **Ultra-Low Memory Footprint**: Follows zero-copy, object reuse, primitive-type-first principles and minimal JVM heap tuning, keeping heap usage of each microservice process within 64MB ~ 128MB.

---

## 2. System Screenshots

### 2.1 Overview Dashboard
![Overview Dashboard](unihh/images/dashboard.png)

### 2.2 Protocol Driver Management & Probing (Protocol Drivers)
![Protocol Drivers](unihh/images/drivers.png)

### 2.3 Physical Device Ledger & Channel Management (Devices)
![Device Management](unihh/images/devices.png)

### 2.4 Thing-Model Parameters & Telemetry Definitions (Thing Models)
![Thing Models](unihh/images/thingmodels.png)

### 2.5 Real-Time Telemetry Point Data Monitoring (Data Monitor)
![Data Monitor](unihh/images/data.png)

### 2.6 Rule Engine & Linkage Action Configuration (Rule Engine)
![Rule Engine](unihh/images/rules.png)

### 2.7 Alarm Events & History Tracking (Alarm Events)
![Alarm Events](unihh/images/alarms.png)

### 2.8 Audit & Operation Logs (Log Management)
![Log Management](unihh/images/logs.png)

### 2.9 User Management & RBAC Security (User & RBAC)
![User Management](unihh/images/user.png)

### 2.10 Role Assignment & Permissions (Roles)
![Role Management](unihh/images/roles.png)

### 2.11 Dynamic Menu Route Management (Menus)
![Menu Management](unihh/images/menus.png)

### 2.12 System Data Dictionaries (Dicts)
![Dict Management](unihh/images/dicts.png)

### 2.13 Platform Login (Login)
![System Login](unihh/images/login.png)

---

## 3. Quick Start & Environment Setup

### 3.1 Pull & Run the EMQX Industrial Message Bus Image

An official Docker environment image pre-configured with industrial MQTT parameters and credentials is provided:

```bash
# 1. Pull the pre-configured EMQX environment image from Alibaba Cloud Registry
docker pull registry.cn-shenzhen.aliyuncs.com/deversun/unihh-emqx:v20261009

# 2. Run the EMQX container in the background
docker run -d --name unihh-emqx \
  -p 1883:1883 \
  -p 8083:8083 \
  -p 8084:8084 \
  -p 8883:8883 \
  -p 18083:18083 \
  registry.cn-shenzhen.aliyuncs.com/deversun/unihh-emqx:v20261009
```

#### Port Mapping Details
- `1883`: MQTT TCP default communication port (used by microservices and protocol drivers)
- `8883`: MQTT SSL/TLS secure port
- `8083`: MQTT WebSocket port
- `8084`: MQTT WSS (WebSocket with SSL) port
- `18083`: EMQX Dashboard Web UI console port

### 3.2 Default Credentials & Access Endpoints

| Target | Access Endpoint / Protocol | Username | Password | Description |
| :--- | :--- | :--- | :--- | :--- |
| **EMQX Dashboard Console** | `http://localhost:18083` | `admin` | `public` | EMQX Web management console for monitoring client connections and topics |
| **MQTT Bus Client Auth** | `tcp://localhost:1883` | `unihh` | `unihh` | Credentials used by protocol drivers and backend services to connect to EMQX |
| **UNIHH-IOT Platform UI** | Frontend Web Address | `admin` | `unihhiot` | Default administrator account for the IoT platform |

---

## 4. System Architecture Design

### 4.1 Topology Diagram

```text
+-----------------------------------------------------------------------------------+
|                     Physical Devices & Industrial Fieldbuses                      |
|       Modbus PLC/Meters  |  Siemens S7-200/300/1200/1500  |  OPC UA Server/SCADA  |
+----------------------------------------+------------------------------------------+
                                         | Industrial Network (TCP/IP / RS485 Serial)
                                         v
+-----------------------------------------------------------------------------------+
|               Distributed Standalone Protocol Drivers (Southbound Layer)          |
|    [Modbus Driver :28011]   |   [Siemens S7 :28012]   |   [OPC UA Driver :28013]  |
|    Extends AbstractDriverPlugin, fully managed by BaseDriverMqttManager           |
+----------------------------------------+------------------------------------------+
                                         | EMQX MQTT Bus (Data / Heartbeat / Will / Control)
                                         v
+-----------------------------------------------------------------------------------+
|                       Core Industrial Message Bus (Messaging)                     |
|                 EMQX Broker Cluster  <=======>  Redis Cache / Online State        |
+----------------------------------------+------------------------------------------+
                                         | Shared Subscriptions / Dedicated Broadcasts
                                         v
+-----------------------------------------------------------------------------------+
|                      Core Business Microservices (Application Layer)              |
|  [API Gateway unihh-gateway :28000]                                               |
|       |-- REST/JWT Routing & Auth ---> [Management unihh-admin :28001]           |
|       `-- REST/Data Stream ----------> [Data Engine unihh-data :28002]            |
+----------------------------------------+------------------------------------------+
                                         | Strategy Router (StorageEngineRouter)
                                         v
+-----------------------------------------------------------------------------------+
|                         Multi-Engine Persistence (Storage Layer)                  |
|    TDengine (TSDB)  |  TimescaleDB / InfluxDB  |  MySQL 8.0 (App)  |  Redis Cache |
+-----------------------------------------------------------------------------------+
```

### 4.2 Technology Stack

| Layer | Component / Tech | Details |
| :--- | :--- | :--- |
| **Base Language** | Java 8 / 17 + Spring Boot | Core development language and lightweight framework ecosystem |
| **Build Tool** | **Maven** | Unified dependency and build management across all modules (`pom.xml`) |
| **API Gateway** | Spring Web / Gateway | API routing, CORS handling, JWT authentication |
| **Message Bus** | EMQX 5.x Distributed Cluster | Core telemetry and control bus, supporting MQTT 5.0 shared subscriptions & retained messages |
| **Driver Base SDK** | `unihh-protocol` (Maven Module) | Abstract driver plugin base, polling engine, LWT keep-alive, REST diagnostics |
| **Storage Layer** | `unihh-storage` (Maven Module) | Strategy routing supporting single (`save`) and batch (`saveBatch`) operations |
| **Persistence** | TDengine 3.x / TimescaleDB / MySQL | Segmented storage for time-series, operational, and configuration data |
| **Cache** | Redis | Telemetry value mirrors, user tokens, and session state |
| **Frontend** | Vue 3 + Element Plus + Vite | Driver management cards, dynamic Schema forms, heartbeat self-check, telemetry viewer |

---

## 5. Service Modules & Port Conventions

### 5.1 Standalone Microservice Processes

| Module Name | Process Type | Default Port | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| **`unihh-gateway`** | Microservice | 28000 | Unified entry point, REST routing, CORS handling, Token authentication |
| **`unihh-admin`** | Microservice | 28001 | Platform management, RBAC, device registry, thing-model definition, driver management, command dispatch |
| **`unihh-data`** | Microservice | 28002 | Telemetry normalization, thing-model validation, rule engine computing, alarm triggering, storage routing |
| **`unihh-driver-modbus`** | Driver Service | 28011 | Modbus TCP/RTU standalone driver, multi-slave polling and register writing |
| **`unihh-driver-s7`** | Driver Service | 28012 | Siemens S7-200 SMART/300/1200/1500 PLC driver, DB block & memory area read/write |
| **`unihh-driver-opcua`** | Driver Service | 28013 | OPC UA standalone driver, NodeId telemetry collection and method invocation |

### 5.2 Shared Maven SDK Modules

| Module Name | Inclusion | Primary Responsibility |
| :--- | :--- | :--- |
| **`unihh-common`** | Maven Dependency | Shared models (`PointData`, `DriverInfo`, `DeviceConfig`), unified response `R`, EMQX MQTT client base |
| **`unihh-protocol`** | Maven Dependency | South-bound driver base `AbstractDriverPlugin`, polling engine `DriverPollingEngine`, endpoint controller `BaseDriverController`, endianness tools `IndustrialEndianUtils` |
| **`unihh-storage`** | Maven Dependency | Storage interface `DataStorageEngine`, routing adapter `StorageEngineRouter` with Memory/Redis/MySQL/TDengine implementations |

---

## 6. Driver Lifecycle & MQTT Specifications

South-bound protocol drivers run as isolated microservice processes, loosely coupled with the central platform via the EMQX MQTT message bus. Drivers automatically handle **self-registration, periodic heartbeats, LWT offline notification (with Retained messages), dynamic configuration synchronization, and heartbeat self-checks**.

### 6.1 EMQX Industrial Topic Specifications

| Category | Topic Pattern | Publisher | Subscriber | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Driver Registration** | `iot/driver/register` | Driver Service | Admin (`unihh-admin`) | Broadcasts driver metadata and HTTP host URL on startup |
| **Driver Heartbeat** | `iot/driver/heartbeat` | Driver Service | Admin (`unihh-admin`) | Reports periodic heartbeat and active channel counts every 5s |
| **Driver Status & LWT** | `iot/driver/{driverCode}/status` | Driver / EMQX | Admin (`unihh-admin`) | Online/offline and LWT crash notifications (`retained=true`) |
| **Global Config Sync** | `iot/device/config/update` | Admin | All Drivers | Broadcasts device creation, updates, or telemetry mapping changes |
| **Targeted Device Config** | `iot/driver/{driverCode}/device/config` | Admin | Specific Driver | Pushes connection and point configurations for that driver's devices |
| **Telemetry Ingestion** | `iot/driver/{driverCode}/data` | Driver Service | Data (`unihh-data`) | Publishes batch telemetry data for rule processing and storage |
| **Reverse Control Cmd** | `iot/driver/{driverCode}/cmd` | Admin | Specific Driver | Point writing (WRITE) or service invocation (SERVICE) commands |
| **Device Channel State** | `iot/device/status` | Driver Service | Admin (`unihh-admin`) | Notifies the platform of physical link connection or disconnection |

---

## 7. Core Business Workflows & Sequence Diagrams

### 7.1 Driver Self-Registration & LWT Keep-Alive

```text
===================================================================================
                  Driver Lifecycle & Abnormal Disconnect LWT Sequence
===================================================================================

[Driver Process Starts]        [EMQX Broker]                 [Management unihh-admin]
         |                          |                                   |
         |-- 1. setWill(LWT) ------>| (Registers LWT, retained=true)    |
         |-- 2. connect() --------->|                                   |
         |-- 3. publish(ONLINE) --->|---------------------------------->| (Immediate online state sync)
         |-- 4. register ---------->|-- iot/driver/register ----------->| (Saves/updates driver info)
         |                          |                                   |-- Syncs configured devices
         |<-- config/update --------|<-- iot/driver/{code}/device/config|
         |                          |                                   |
         | [Every 5s Heartbeat]     |                                   |
         |-- 5. heartbeat --------->|-- iot/driver/heartbeat ---------->| (Refreshes watchdog timestamp)
         |                          |                                   |
   [Crash / Power Loss]             |                                   |
         x                          |-- 6. Broker broadcasts LWT ------>| (Marks driver OFFLINE)
                                    |   (iot/driver/{code}/status)      |-- Cascades devices to OFFLINE
                                    |                                   |
                                    |                                   | [Watchdog Routine]
                                    |                                   | (Timeout >15s marks OFFLINE)
===================================================================================
```

### 7.2 Telemetry Ingestion & Multi-Engine Storage

```text
===================================================================================
                  Telemetry Polling & Storage Strategy Sequence
===================================================================================

[Physical Devices] ----> [Driver Service] ----> [EMQX Broker] ----> [Data Engine unihh-data] ----> [StorageEngineRouter]
  (PLC / Meters)       (PollingEngine)      (iot/driver/+/data)     (Rules & Threshold Checks)       (Strategy Dispatch)
                              |                                                  |                           |
                       Batch acquisition                                    Normalization &                 +-- TDengine (TSDB)
                       Quality calculation                                  Alarm triggering                +-- MySQL (Relational)
                                                                                                            `-- Redis (Real-time)
===================================================================================
```

### 7.3 Reverse Control & Device Register Writing

```text
===================================================================================
                  Reverse Control Command Dispatch & Locked Writing
===================================================================================

[Frontend / App] -----> [Gateway :28000] -----> [Admin :28001] -----> [EMQX Broker] -----> [Driver Service] -----> [Physical Devices]
  (Write Request)         (Routing & Auth)       (Builds Command)     (iot/driver/{code}/cmd) (Locked Execution)    (Register Write)
                                                                                                 |
                                                                                     getChannelLock(config)
                                                                                     Exclusive fair lock
                                                                                     writePoint(...)
===================================================================================
```

---

## 8. Driver Plugin Development Guide

Creating a new industrial protocol driver requires only three steps:

### 8.1 Step 1: Add SDK Dependency in `pom.xml`

```xml
<dependency>
    <groupId>com.unihh.iot</groupId>
    <artifactId>unihh-protocol</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 8.2 Step 2: Extend `AbstractDriverPlugin`

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
@IoTDriver(code = "custom-protocol", name = "Custom Industrial Protocol Driver", version = "2.0.0")
public class CustomDriverPlugin extends AbstractDriverPlugin {

    @Override
    public String getDriverCode() {
        return "custom-protocol";
    }

    @Override
    public DriverMetadata getMetadata() {
        DriverMetadata meta = new DriverMetadata();
        meta.setDriverCode(getDriverCode());
        meta.setDriverName("Custom Industrial Protocol Driver");
        meta.setVersion("2.0.0");
        return meta;
    }

    @Override
    public boolean openConnection(DeviceConfig config) throws Exception {
        // Handshake and establish connection with physical device
        return true;
    }

    @Override
    public ParsedPayload readPoints(DeviceConfig config, List<ThingModel.PropertySpec> points) throws Exception {
        ParsedPayload payload = new ParsedPayload();
        // Batch read points and populate payload.addPoint(pointCode, value, quality, timestamp)
        return payload;
    }

    @Override
    public boolean writePoint(DeviceConfig config, String pointCode, String address, Object value, String dataType) throws Exception {
        // Reverse control: encode write packet and send to device
        return true;
    }
}
```

### 8.3 Step 3: Write Application Entry Point & Run

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

Once started, the base SDK automatically completes registration, heartbeats, configuration pulling, and scheduling without boilerplate code.

---

## 9. Channel Heartbeat Self-Check & Probing

To provide dependable runtime monitoring, the platform includes active heartbeat probing:

1. **Active Probing Endpoint**: `unihh-admin` provides `/api/v1/drivers/{driverCode}/ping`. Clicking 【Channel Heartbeat Self-Check】 on the UI triggers two-tier validation:
   - Probes the driver's HTTP status endpoint (`{serviceUrl}/api/v1/driver/status` with a 2s timeout) to retrieve active device counts, active sockets, and round-trip latency (RTT);
   - Validates recent MQTT heartbeat timestamps as fallback.
2. **Dynamic State Correction**: If probing succeeds, the driver is confirmed online with measured latency. If the driver is unreachable or down, the database immediately transitions it to offline and cascades all associated devices to offline, preventing false online states.

---

## 10. Multi-Engine Storage Strategy (`unihh-storage`)

The storage module decouples persistence logic from core ingestion using the Strategy pattern:

```java
public interface DataStorageEngine {
    String getEngineType();
    void init(StorageConfig config) throws Exception;
    void save(PointData data) throws Exception;               // Single record (alarms / command ACKs)
    void saveBatch(List<PointData> dataList) throws Exception; // Batch save (high-throughput telemetry)
    List<PointData> queryHistory(HistoryQuery query) throws Exception;
    void close();
}
```

- **Router (`StorageEngineRouter`)**: Built-in engines include `MemoryStorageEngine` (local ring buffer), `RedisStorageEngine` (real-time snapshots), `MySQLStorageEngine` (relational backup), and `TDengineStorageEngine` (time-series engine). The active engine can be dynamically switched via configuration.

---

## 11. Minimal Memory Footprint & JVM Tuning

To run reliably on resource-constrained hardware such as Raspberry Pi and edge gateways:

### 11.1 Link Serialization & Object Pooling

Avoid creating temporary wrapper objects during high-frequency polling. Leverage buffer slicing and object recycling to minimize Young Generation GC pauses.

### 11.2 Recommended Production JVM Parameters

For edge gateways or small-memory instances:

```bash
# Core business services (recommended 64MB ~ 128MB heap)
java -server -Xms64m -Xmx128m -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=64m -Xss256k -XX:+UseSerialGC -jar unihh-admin.jar

# Protocol driver services (recommended 32MB ~ 64MB heap)
java -server -Xms32m -Xmx64m -XX:MaxMetaspaceSize=64m -XX:ReservedCodeCacheSize=32m -Xss256k -XX:+UseSerialGC -jar unihh-driver-modbus.jar
```

---

## 12. Contributing Guidelines

We welcome contributions from the open-source community and industrial IoT developers! Please adhere to the following workflow:

### 12.1 Branch & PR Workflow

All contributions **must be submitted via a new branch and a Pull Request (PR)**. Do not push directly to the `master` branch:

1. **Fork the Repository**: Fork this repository to your personal account.
2. **Create a Feature Branch**:
   ```bash
   git checkout master
   git pull origin master
   git checkout -b feature/driver-bacnet
   ```
3. **Local Coding & Verification**:
   - Ensure clean code and no resource leaks;
   - Pass full builds before pushing:
     ```bash
     mvn test-compile
     cd unihh-frontend && npm run build
     ```
4. **Commit & Push to Your Branch**:
   ```bash
   git add .
   git commit -m "feat(driver): add BACnet/IP protocol driver plugin"
   git push origin feature/driver-bacnet
   ```
5. **Submit a Pull Request (PR)**:
   - Create a PR against the `master` branch of this repository;
   - Describe changes, verification steps, and linked issues;
   - Maintainers will review and merge upon successful validation.

### 12.2 Commit Message Convention

Follow semantic commit conventions:
- `feat:` New features (e.g., new protocol drivers, thing-model capabilities)
- `fix:` Bug fixes (e.g., reconnect handling, frame parsing issues)
- `docs:` Documentation updates
- `refactor:` Code refactoring without behavior changes
- `perf:` Performance optimizations (memory reductions, throughput)
- `test:` Unit and integration test additions

---

## Open Source License

This project is licensed under the [Apache License 2.0](LICENSE.txt). Refer to the local [LICENSE.txt](LICENSE.txt) file for full license terms.
