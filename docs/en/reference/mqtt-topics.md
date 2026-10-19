# MQTT Topic Reference

All interaction between southbound drivers and the admin/data centers flows through the EMQX message bus, following MQTT 5.0. Custom drivers and third-party integrations can talk directly using the topics below.

## Bus Access

| Item | Default |
| :--- | :--- |
| Address | `tcp://localhost:1883` (TLS on 8883) |
| Account / password | `unihh` / `unihh` |
| Broker | EMQX 5.x, shared subscriptions and retained messages supported |

## Topic Table

| Category | Topic | Publisher | Consumer | Description |
| :--- | :--- | :--- | :--- | :--- |
| Driver registration | `iot/driver/register` | Driver | Admin center | Driver broadcasts its metadata and HTTP endpoint at startup |
| Driver heartbeat | `iot/driver/heartbeat` | Driver | Admin center | Every 5s by default, with active channel metrics |
| Driver status / last-will | `iot/driver/{driverCode}/status` | Driver / EMQX | Admin center | Online/offline notices; the broker publishes the LWT will on crash (retained=true) |
| Device config sync | `iot/device/config/update` | Admin center | All drivers | Global hot notice for device or point changes |
| Dedicated device config | `iot/driver/{driverCode}/device/config` | Admin center | Target driver | Directed push of connection and point definitions |
| Point data upload | `iot/driver/{driverCode}/data` | Driver | Data center | Periodic acquisition results, consumed via shared subscription |
| Write-back command | `iot/driver/{driverCode}/cmd` | Admin center | Target driver | Point write (WRITE) or service invocation (SERVICE) |
| Device channel state | `iot/device/status` | Driver | Admin center | Physical link drop/recovery; platform updates device state |

## Consumption Patterns

- **Shared subscriptions**: the `iot/driver/+/data` stream is consumed with an MQTT 5.0 shared subscription, so multiple data-center instances can split the throughput;
- **Dedicated topics**: config sync and commands use `{driverCode}`-scoped topics, so unrelated drivers never process extra traffic;
- **Retained messages**: driver status is retained, so late subscribers learn the latest state immediately.

## Reliability Mechanisms

| Mechanism | Description |
| :--- | :--- |
| LWT last-will | Drivers register a will on connect; EMQX publishes the offline message if the process dies |
| Heartbeat watchdog | Platform-side safety net: a driver is declared offline after 15s without a heartbeat |
| Channel mutex | Drivers apply a fair lock per device channel — commands arriving concurrently on MQTT still execute serially on the wire |

## Integration Advice

Third-party systems (e.g. an MES) that only consume data can share-subscribe to `iot/driver/+/data` directly. For issuing control, prefer the platform REST API (`/api/v1/devices` control endpoints) over publishing raw MQTT: you get authentication, parameter snapshots and the complete execution audit trail for free.
