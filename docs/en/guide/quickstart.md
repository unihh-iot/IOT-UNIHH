# Quick Start

This guide walks you through running UNIHH-IOT locally end to end. Two options are provided:

- **Option 1: Docker Compose (recommended)** — one command brings up every backend service and dependency, ideal for evaluation and standard deployment;
- **Option 2: Bare-metal JAR** — build and start each module individually, ideal for edge gateways and fine-grained control.

## Prerequisites

| Component | Version | Purpose |
| :--- | :--- | :--- |
| Docker | 20+ (with Compose v2) | Run all services (Option 1) |
| JDK | 8 or 17 | Backend build (both options) |
| Maven | 3.6+ | Backend build |
| Node.js | 16+ | Frontend build and dev server |
| MySQL client | 8.0 | Only for manual DB init in Option 2 |

::: tip Hardware
Any 2-core / 4GB machine runs the full stack comfortably. Core services are tuned to 64–128MB heaps and drivers to 32–64MB — see [Deployment](./deploy.md).
:::

## Option 1: Docker Compose (recommended)

The repository ships a `docker-compose.yml` orchestrating 8 services: MySQL, EMQX, the gateway, admin, data center and three protocol drivers. MySQL mounts `db/unihh_iot.sql` on first start and initializes the schema automatically.

### Step 1: Build the Backend Modules

Images are packaged from locally compiled JARs (same artifacts as bare-metal):

```bash
# from the repository root
mvn clean package -DskipTests
```

### Step 2: Build Images and Start

```bash
# Linux / macOS
bash build-docker.sh

# Windows
build-docker.bat
```

The script runs `docker compose build`, then start everything:

```bash
docker compose up -d
```

### Step 3: Wait for Readiness

```bash
# all 8 containers should be healthy / running
docker compose ps

# tail logs and check for errors
docker compose logs -f
```

Services and ports:

| Service | Container | Ports | Purpose |
| :--- | :--- | :--- | :--- |
| MySQL 8.0 | `unihh-mysql` | 3306 | Business DB `unihh_iot` (root / 123456, auto-initialized) |
| EMQX 5.x | `unihh-emqx` | 1883 / 8083 / 8084 / 8883 / 18083 | MQTT bus and Dashboard (admin / public) |
| Gateway | `unihh-gateway` | 28000 | Unified REST entry |
| Admin | `unihh-admin` | 28001 | Management service |
| Data center | `unihh-data` | 28002 | Rule engine and storage routing |
| Modbus driver | `unihh-driver-modbus` | 28011 | Modbus TCP/RTU |
| S7 driver | `unihh-driver-s7` | 28012 | Siemens S7 |
| OPC UA driver | `unihh-driver-opcua` | 28013 | OPC UA |

EMQX ports: 1883 MQTT TCP (bus), 8883 SSL/TLS, 8083 WebSocket, 8084 WSS, 18083 Dashboard console.

### Step 4: Start the Frontend

The compose stack does not include a frontend container. Run it locally:

```bash
cd unihh-frontend
npm install    # or pnpm install
npm run dev
```

Open `http://localhost:3000`.

::: tip Production
`npm run dev` is for local development only. In production run `npm run build` and serve the output through Nginx with the API reverse-proxied — see [Deployment → Nginx Reverse Proxy and Frontend Hosting](./deploy.md#nginx-reverse-proxy-and-frontend-hosting).
:::

### Step 5: Sign In

| Entry | URL | Account | Default Password |
| :--- | :--- | :--- | :--- |
| UNIHH-IOT platform | `http://localhost:3000` | `admin` | `unihhiot` |
| EMQX Dashboard | `http://localhost:18083` | `admin` | `public` |
| MQTT bus | `tcp://localhost:1883` | `unihh` | `unihh` |

::: warning Security
Change every default password before production. Platform passwords live in **System → Users**; MySQL and EMQX credentials are set in `docker-compose.yml` environment variables — see [Deployment → Docker Compose](./deploy.md#docker-compose-deployment).
:::

## Option 2: Bare-metal JAR

Ideal for edge gateways (Raspberry Pi / industrial PCs) or per-process control.

### Step 1: Start the EMQX Message Bus

The official image ships with MQTT authentication preconfigured:

```bash
docker run -d --name unihh-emqx \
  -p 1883:1883 \
  -p 8083:8083 \
  -p 8084:8084 \
  -p 8883:8883 \
  -p 18083:18083 \
  registry.cn-shenzhen.aliyuncs.com/deversun/unihh-emqx:v20261009
```

Verify EMQX at `http://localhost:18083` (`admin` / `public`).

### Step 2: Initialize the Database

```sql
CREATE DATABASE unihh_iot DEFAULT CHARACTER SET utf8mb4;
```

```bash
# business data (required)
mysql -u root -p unihh_iot < db/unihh_iot.sql

# time-series storage (optional, for TDengine)
taos -s "CREATE DATABASE unihh_iot"
taos -u root -d unihh_iot < db/tdengine_schema.sql
```

### Step 3: Build and Start the Backend

```bash
mvn clean package -DskipTests
```

Start core services in order:

```bash
# 1. Gateway :28000
java -jar unihh-gateway/target/unihh-gateway.jar

# 2. Admin :28001
java -jar unihh-admin/target/unihh-admin.jar

# 3. Data center :28002
java -jar unihh-data/target/unihh-data.jar

# 4. Drivers you need, e.g. Modbus :28011
java -jar drivers/unihh-driver-modbus/target/unihh-driver-modbus.jar
```

For production use the minimal-heap flags from [Deployment](./deploy.md#jvm-tuning).

### Step 4: Start the Frontend

```bash
cd unihh-frontend
npm install    # or pnpm install
npm run dev
```

Open `http://localhost:3000` and sign in with the accounts above.

## Verify the Platform

After signing in, check the dashboard:

1. **Mounted protocol drivers** > 0 and showing online;
2. **Connected devices** matches your configured device count.

If a driver shows offline, run **Channel Self-check** in Driver Management or see the [FAQ](../reference/faq.md).

## Next Steps

- [User Manual](../manual/): what every page does
- [Driver Development](./driver-dev.md): integrate your own protocol
- [Deployment](./deploy.md): storage engines, JVM tuning and upgrades for production
