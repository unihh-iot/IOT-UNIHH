# Introduction

UNIHH-IOT is a lightweight open-source IoT platform built for industrial field scenarios. It provides multi-protocol southbound data acquisition, dynamic thing modeling, distributed rule computing, multi-engine storage routing and northbound write-back control. The backend consists of Java microservices connected by an EMQX message bus, and the frontend is built with Vue 3. Both are fully open source under the Apache License 2.0 and free for commercial use.

## Problems It Solves

Legacy industrial data acquisition platforms share a set of well-known pain points. UNIHH-IOT addresses each of them at the architectural level:

| Legacy approach | UNIHH-IOT |
| :--- | :--- |
| Heavy cloud-native dependency stacks with multi-GB memory footprints, impossible to keep running on edge industrial PCs | Core services fit in a 64–128 MB heap, drivers in 32–64 MB — Raspberry Pi-class hardware is enough |
| Drivers loaded in-process via class loaders; one bad driver crashes the whole platform | Every protocol driver is an independent microservice talking over EMQX; failures stay isolated |
| Read-only acquisition; write-back suffers from packet interleaving and frame misalignment | A channel-level fair mutex (`getChannelLock`) serializes polling and write-back automatically |
| Outdated frontends with prohibitive customization costs | Full UI rewritten in Vue 3 + Element Plus, backed by complete REST APIs |

## Key Features

- **Bidirectional read/write**: high-concurrency polling acquisition (Read/Decode) plus millisecond-level northbound control (Write/Encode).
- **Distributed driver architecture**: drivers built on `AbstractDriverPlugin` and `BaseDriverMqttManager` get self-registration, heartbeats, LWT last-will, hot config sync and command listening out of the box.
- **Channel-level fair mutex**: serialized read/write on RS485 half-duplex buses and single-session TCP links.
- **Unified thing model**: properties, services and events with dynamic mapping between physical points and logical measurements.
- **Pluggable storage**: the `unihh-storage` module routes by strategy across TDengine, TimescaleDB, InfluxDB, MySQL, Redis and in-memory buffers, supporting single `save` and batch `saveBatch`.
- **Ops-friendly deployment**: microservices locate each other through plain `application.yml` addresses — no registry required; each module runs as a single JAR with `java -jar`.

## A Look at the Platform

The dashboard presents device availability, driver health, acquisition throughput and alert status at a glance:

![Dashboard](/images/dashboard.png)

Protocol drivers are managed as cards with channel heartbeat self-checks and millisecond RTT probing:

![Protocol Drivers](/images/drivers.png)

See the [Operation Manual](/en/manual/) for the complete walkthrough of every page.

## Next Steps

- [Quick Start](./quickstart.md) — get the platform running in five steps
- [Architecture](./architecture.md) — service topology and data flows
- [Driver Development Guide](./driver-dev.md) — ship a new protocol driver in three steps
