# Deployment and Operations

## Deployment Topologies

Microservices locate each other through plain `application.yml` configuration (or environment variables) — no service registry involved. This keeps several topologies simple:

| Topology | Fits | Notes |
| :--- | :--- | :--- |
| Bare-metal JARs on one host | Edge gateways, small projects | One JAR per module, start/stop with `java -jar` |
| Edge + cloud hybrid | Multi-site groups | Drivers on shop-floor PCs, core services in a server room or cloud |
| Containers | Existing Docker/K8s infrastructure | Repository ships a `docker-compose.yml` orchestrating the full stack; migrate to K8s as needed |

## Docker Compose Deployment

The `docker-compose.yml` at the repository root orchestrates the complete platform: one MySQL, one EMQX, three core microservices and three protocol drivers, all on the `unihh-network` bridge and addressing each other by container name.

### First Start

```bash
# 1. build backend modules (images package locally compiled JARs)
mvn clean package -DskipTests

# 2. build images (build-docker.sh on Linux/macOS, build-docker.bat on Windows)
bash build-docker.sh

# 3. start everything
docker compose up -d

# 4. verify: admin starts only after MySQL passes its health check
docker compose ps
```

On first start MySQL automatically runs `db/unihh_iot.sql` (mounted at `/docker-entrypoint-initdb.d/`); data persists in the named volume `mysql_data`.

### Building and Pushing Images

`build-docker.sh` supports two modes:

```bash
bash build-docker.sh              # build all images
bash build-docker.sh unihh-admin  # build a single module image

# start one service without touching its dependencies
docker compose up -d --no-deps unihh-admin

# push images to a registry (for CI/CD)
docker compose push
```

Image naming and push target are controlled by two variables, overridable in `.env` or on the command line:

| Variable | Default | Purpose |
| :--- | :--- | :--- |
| `DOCKER_REGISTRY` | `registry.cn-shenzhen.aliyuncs.com/deversun` | Registry address |
| `VERSION` | `1.0.0` | Image tag |

```bash
# example: push to your own registry
DOCKER_REGISTRY=registry.example.com VERSION=1.1.0 docker compose build
DOCKER_REGISTRY=registry.example.com VERSION=1.1.0 docker compose push
```

### Environment Variables and Credentials

Connection settings are injected via compose environment variables; adjust them centrally for production:

| Service | Key variables | Default |
| :--- | :--- | :--- |
| unihh-mysql | `MYSQL_ROOT_PASSWORD` | `123456` |
| unihh-admin | `SPRING_DATASOURCE_URL` / `USERNAME` / `PASSWORD` | `unihh-mysql:3306`, root / 123456 |
| admin / data / drivers | `EMQX_SERVER_URI` / `EMQX_USERNAME` / `EMQX_PASSWORD` | `tcp://unihh-emqx:1883`, unihh / unihh |
| unihh-gateway | `UNIHH_ADMIN_HOST` / `UNIHH_DATA_HOST` | `unihh-admin` / `unihh-data` |

::: warning
When changing the MySQL password, update every service that references it and delete the `mysql_data` volume to re-initialize (back up business data first).
:::

### Daily Commands

```bash
docker compose logs -f unihh-admin          # follow one service's logs
docker compose restart unihh-driver-modbus  # restart a single driver
docker compose stop                         # stop all (keep containers)
docker compose down                         # stop and remove containers (keep volumes)
docker compose down -v                      # also remove volumes (full reset)
```

### Upgrading the Container Stack

1. Back up the business DB: `docker exec unihh-mysql mysqldump -uroot -p123456 unihh_iot > backup.sql`;
2. Pull the new code and rebuild: `mvn clean package -DskipTests && bash build-docker.sh`;
3. Rolling update: `docker compose up -d` (only services whose images changed are recreated);
4. Verify all drivers are back online in Driver Management.

## Nginx Reverse Proxy and Frontend Hosting

In production, put Nginx at the edge: serve the frontend static build, reverse-proxy the REST API, and terminate TLS. Backend microservices keep listening on internal ports only.

### Build the Frontend

```bash
cd unihh-frontend
npm install
npm run build    # output goes to unihh-frontend/dist/
```

### Nginx Configuration Example

```nginx
server {
    listen 80;
    server_name unihh.example.com;

    # frontend static assets (Vue Router runs in history mode, fall back to index.html)
    root /opt/unihh/unihh-frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # Vite output is content-hashed, safe to cache long
    location /assets/ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # REST API reverse proxy to the gateway (path passed through as-is)
    location /api/ {
        proxy_pass http://127.0.0.1:28000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 60s;
    }
}
```

Key points:

- Do **not** append a path to `proxy_pass` — the `/api` prefix must reach the gateway untouched, which routes on `/api/v1/*`; this matches the vite dev proxy behavior exactly;
- Serving frontend and API on the same domain eliminates CORS entirely; the gateway also handles CORS itself for split deployments;
- Set the `Host` header once, the standard way — the gateway previously had a duplicate-header issue with proxies, so avoid stacking headers.

### Optional: Proxy the EMQX Dashboard

To reach the EMQX console under the same domain, add a location (restrict by source IP or intranet only):

```nginx
location /emqx-dashboard/ {
    proxy_pass http://127.0.0.1:18083/;
    proxy_set_header Host $host;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

### HTTPS

- Terminate TLS at Nginx (`listen 443 ssl`); service-to-service traffic stays plain HTTP inside the network;
- MQTT over TLS uses EMQX's own port 8883 and does **not** go through Nginx;
- For write-back control, also tighten the MQTT bus: enable EMQX TLS and keep 1883 off the public internet.

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

## Upgrading Bare-metal JARs

1. Back up the MySQL business database and TDengine;
2. Build the new version: `mvn clean package -DskipTests`;
3. Replace and restart core services one by one (data first, then admin, then gateway);
4. Restart drivers one by one and confirm they come back online on Driver Management.

Drivers and the platform are decoupled through the MQTT bus: a briefly offline driver loses nothing, and its configuration re-syncs automatically on reconnect.
