# Quick Start

This guide walks you through running UNIHH-IOT locally end to end. Budget about 10 minutes.

## Prerequisites

| Component | Version | Purpose |
| :--- | :--- | :--- |
| Docker | 20+ | EMQX message bus |
| JDK | 8 or 17 | Backend microservices |
| Maven | 3.6+ | Backend build |
| MySQL | 8.0 | Business data |
| Node.js | 16+ | Frontend build |
| Redis | 5.0+ | State cache (optional) |
| TDengine | 3.x | Time-series storage (optional) |

## Step 1: Start the EMQX Message Bus

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

Port reference:

| Port | Purpose |
| :--- | :--- |
| 1883 | MQTT TCP — bus for services and drivers |
| 8883 | MQTT SSL/TLS |
| 8083 | MQTT WebSocket |
| 8084 | MQTT WSS |
| 18083 | EMQX Dashboard web console |

Verify EMQX is up at `http://localhost:18083` (`admin` / `public`).

## Step 2: Initialize the Database

Create the schema in MySQL 8.0 and run the init script:

```sql
CREATE DATABASE unihh_iot DEFAULT CHARACTER SET utf8mb4;
```

```bash
# Business data (required)
mysql -u root -p unihh_iot < db/unihh_iot.sql

# Time-series storage (if enabling TDengine)
taos -s "CREATE DATABASE unihh_iot"
taos -u root -d unihh_iot < db/tdengine_schema.sql
```

## Step 3: Build and Start the Backend

```bash
# Build all modules from the repository root
mvn clean package -DskipTests
```

Start the core services in order:

```bash
# 1. Gateway :28000
java -jar unihh-gateway/target/unihh-gateway.jar

# 2. Admin center :28001
java -jar unihh-admin/target/unihh-admin.jar

# 3. Data center :28002
java -jar unihh-data/target/unihh-data.jar

# 4. Any driver you need, e.g. Modbus :28011
java -jar drivers/unihh-driver-modbus/target/unihh-driver-modbus.jar
```

For production, use the minimal-heap JVM flags described in [Deployment](./deploy.md#jvm-memory-tuning).

## Step 4: Start the Frontend

```bash
cd unihh-frontend
npm install    # or pnpm install
npm run dev
```

Open `http://localhost:5173` in your browser.

## Step 5: Sign In

| Entry | URL | Account | Default Password |
| :--- | :--- | :--- | :--- |
| UNIHH-IOT platform | `http://localhost:5173` | `admin` | `unihhiot` |
| EMQX Dashboard | `http://localhost:18083` | `admin` | `public` |
| MQTT bus | `tcp://localhost:1883` | `unihh` | `unihh` |

::: warning Security notice
Change all default passwords before going to production. Platform passwords are managed under **System → User Management**; EMQX credentials live in each service's `application.yml`.
:::

## Verify the Platform

After signing in, check the dashboard:

1. **Mounted protocol drivers** > 0 with drivers online;
2. **Connected devices** matches the devices you created.

If a driver shows offline, run the **channel heartbeat self-check** on the [Driver Management](../manual/drivers.md) page, or see the [FAQ](../reference/faq.md).

## Next Steps

- [Operation Manual](../manual/) — how to use every page
- [Driver Development Guide](./driver-dev.md) — integrate your own protocol
