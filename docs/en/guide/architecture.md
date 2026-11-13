# Architecture

## Overview

Five layers from top to bottom: physical devices connect through field buses into standalone protocol drivers; all interaction between drivers and core microservices runs over the EMQX message bus; the storage layer routes data by shape through policy routing.

![UNIHH-IOT architecture](/images/architecture-en.svg)

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

Drivers register themselves on startup and stay alive with a 5-second heartbeat; abnormal disconnects are covered by both the LWT will message and the watchdog.

![Driver lifecycle and LWT keep-alive sequence](/images/driver-lifecycle-en.svg)

Key points:

- The admin center senses driver shutdown or crash within seconds, either via LWT or the watchdog;
- The watchdog is the second line of defense: a driver is declared offline after 15 seconds without a heartbeat;
- When a driver goes offline, its devices are cascaded offline to avoid phantom online states.

### Acquisition and storage fan-out

Telemetry enters the data center over the bus, passes rule and alert checks, then lands in storage routed by data shape.

![Acquisition and storage fan-out](/images/data-flow-en.svg)

### Write-back control chain

Control commands pass gateway auth, then reach the driver over a dedicated MQTT topic and are written inside the channel lock.

![Write-back control chain](/images/control-flow-en.svg)

Reads and writes on half-duplex buses (e.g. RS485) are queued through the channel lock automatically — see the [Driver Management](../manual/drivers.md) manual.

## MQTT Topics

All driver interactions run over MQTT topics. The complete topic table lives in the [MQTT Topic Reference](../reference/mqtt-topics.md).
