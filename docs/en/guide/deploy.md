# Deployment and Operations

## Deployment Topologies

Microservices locate each other through plain `application.yml` configuration (or environment variables) — no service registry involved. This keeps several topologies simple:

| Topology | Fits | Notes |
| :--- | :--- | :--- |
| Bare-metal JARs on one host | Edge gateways, small projects | One JAR per module, start/stop with `java -jar` |
| Edge + cloud hybrid | Multi-site groups | Drivers on shop-floor PCs, core services in a server room or cloud |
| Containers | Existing Docker/K8s infrastructure | One container per service; run EMQX separately or as a cluster |

## JVM Memory Tuning

The platform applies zero-copy, object pooling and primitive-first patterns throughout, so heaps can stay very small. Recommended production flags:

```bash
# Core services (gateway / admin / data, 64–128 MB heap)
java -server -Xms64m -Xmx128m \
  -XX:MaxMetaspaceSize=128m \
  -XX:ReservedCodeCacheSize=64m \
  -Xss256k \
  -XX:+UseSerialGC \
  -jar unihh-admin.jar

# Protocol drivers (32–64 MB heap)
java -server -Xms32m -Xmx64m \
  -XX:MaxMetaspaceSize=64m \
  -XX:ReservedCodeCacheSize=32m \
  -Xss256k \
  -XX:+UseSerialGC \
  -jar unihh-driver-modbus.jar
```

Why these choices:

- **SerialGC**: lowest overhead and pause times for small heaps (<256 MB); G1/ZGC add complexity without benefit here;
- **-Xss256k**: compact thread stacks matter when thread counts are limited on edge devices;
- On memory-rich servers with high throughput, raise the heap to 256–512 MB and switch to G1.

## Hardware Reference

| Scale | Hardware | Reference spec |
| :--- | :--- | :--- |
| Edge gateway (<50 devices) | Raspberry Pi 4B / entry IPC | 4 cores, 2 GB RAM, 32 GB storage |
| Single plant (50–500 devices) | 1 server | 4 cores, 8 GB RAM, SSD |
| Multi-plant / group | EMQX + service clusters | Scale EMQX and services horizontally with device count |

## Choosing Storage Engines

`unihh-storage` routes through the `StorageEngineRouter` by strategy:

| Engine | Best for | Notes |
| :--- | :--- | :--- |
| TDengine | High-frequency time series | First choice: high compression, excellent write and downsampling performance |
| TimescaleDB / InfluxDB | Time series | Alternatives when a PG/Influx stack already exists |
| MySQL | Command responses, alert records | Relational backup, easy to join with business data |
| Redis | Live point snapshots | Frontend reads directly; never touches disk |
| Memory | Absorbing acquisition spikes | Local ring buffer, pairs with batch persistence |

To add a custom engine, implement the unified interface and register it with the router:

```java
public interface DataStorageEngine {
    String getEngineType();
    void init(StorageConfig config) throws Exception;
    void save(PointData data) throws Exception;                // single save (command responses, critical alerts)
    void saveBatch(List<PointData> dataList) throws Exception; // batch save (high-frequency acquisition)
    List<PointData> queryHistory(HistoryQuery query) throws Exception;
    void close();
}
```

## Daily Operations

### Driver health checks

- Use the **channel heartbeat self-check** button on the Driver Management page for a dual HTTP + MQTT probe with RTT measurement;
- The watchdog marks a driver offline after 15 seconds without a heartbeat and cascades its devices offline;
- Baseline EMQX connection counts and message throughput in your monitoring system and alert on deviations.

### Logs and audit

- The **Log Audit** page keeps write-back command logs and login audit trails — your primary source for incident reconstruction;
- Write-back logs include parameter snapshots, latency and results; start there when a control command fails.

### Config changes

- Device connection or point changes are hot-pushed to the affected driver over MQTT — no restart needed;
- Driver-side changes (JVM flags, ports) require a driver restart; the platform re-syncs registration automatically once it comes back.

## Upgrading

1. Back up the MySQL business database and TDengine;
2. Build the new version: `mvn clean package -DskipTests`;
3. Replace and restart core services one by one (data first, then admin, then gateway);
4. Restart drivers one by one and confirm they come back online on Driver Management.

Drivers and the platform are decoupled through the MQTT bus: a briefly offline driver loses nothing, and its configuration re-syncs automatically on reconnect.
