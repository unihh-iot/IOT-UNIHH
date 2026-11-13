# 快速开始

本文档引导你在本地把 UNIHH-IOT 完整跑起来。提供两种方式：

- **方式一：Docker Compose 一键部署（推荐）** —— 一条命令拉起全部后端服务与基础设施，适合快速体验与标准部署；
- **方式二：裸机 JAR 部署** —— 逐个模块构建启动，适合边缘网关与需要精细控制的场景。

## 环境要求

| 组件 | 版本要求 | 用途 |
| :--- | :--- | :--- |
| Docker | 20+（含 Docker Compose v2） | 运行全部服务（方式一） |
| JDK | 8 或 17 | 构建后端模块（两种方式均需） |
| Maven | 3.6+ | 后端构建 |
| Node.js | 16+ | 前端构建与开发 |
| MySQL 客户端 | 8.0 | 仅方式二手动初始化数据库时需要 |

::: tip 硬件参考
2 核 4GB 内存的普通电脑或云主机即可跑通全部服务。仓库已按极低内存优化，核心服务堆 64~128MB、单驱动 32~64MB，详见[部署与运维](./deploy.md)。
:::

## 方式一：Docker Compose 一键部署（推荐）

仓库根目录提供 `docker-compose.yml`，共编排 8 个服务：MySQL、EMQX、网关、管理中心、数据中心与 3 个协议驱动。MySQL 首次启动会自动挂载 `db/unihh_iot.sql` 完成建库导表，无需手工初始化。

### 第一步：构建后端模块

镜像打包使用本地已编译的 JAR（与裸机部署同一产物）：

```bash
# 仓库根目录执行
mvn clean package -DskipTests
```

### 第二步：构建镜像并启动

```bash
# Linux / macOS
bash build-docker.sh

# Windows
build-docker.bat
```

脚本内部执行 `docker compose build` 打包全部镜像，完成后一键启动：

```bash
docker compose up -d
```

### 第三步：确认服务就绪

```bash
# 查看各容器状态（8 个容器均为 healthy / running 即就绪）
docker compose ps

# 跟踪启动日志，确认无报错
docker compose logs -f
```

各服务端口与用途：

| 服务 | 容器名 | 端口 | 用途 |
| :--- | :--- | :--- | :--- |
| MySQL 8.0 | `unihh-mysql` | 3306 | 业务库 `unihh_iot`（root / 123456，首启自动导表） |
| EMQX 5.x | `unihh-emqx` | 1883 / 8083 / 8084 / 8883 / 18083 | MQTT 总线与 Dashboard（admin / public） |
| 统一网关 | `unihh-gateway` | 28000 | REST API 统一入口 |
| 管理中心 | `unihh-admin` | 28001 | 平台管理服务 |
| 数据中心 | `unihh-data` | 28002 | 规则引擎与存储分流 |
| Modbus 驱动 | `unihh-driver-modbus` | 28011 | Modbus TCP/RTU |
| S7 驱动 | `unihh-driver-s7` | 28012 | 西门子 S7 |
| OPC UA 驱动 | `unihh-driver-opcua` | 28013 | OPC UA |

EMQX 端口说明：1883 MQTT TCP（总线）、8883 SSL/TLS、8083 WebSocket、8084 WSS、18083 Dashboard 控制台。

### 第四步：启动前端

当前编排不包含前端容器，前端在本地开发模式下运行：

```bash
cd unihh-frontend
npm install    # 或 pnpm install
npm run dev
```

浏览器访问 `http://localhost:3000`。

::: tip 生产环境
`npm run dev` 仅用于本地开发。生产环境请执行 `npm run build` 后用 Nginx 托管构建产物并反向代理 API，完整配置见[部署与运维 → Nginx 反向代理与前端托管](./deploy.md#nginx-反向代理与前端托管)。
:::

### 第五步：登录平台

| 系统入口 | 访问地址 | 账号 | 默认密码 |
| :--- | :--- | :--- | :--- |
| UNIHH-IOT 平台 | `http://localhost:3000` | `admin` | `unihhiot` |
| EMQX Dashboard | `http://localhost:18083` | `admin` | `public` |
| MQTT 总线 | `tcp://localhost:1883` | `unihh` | `unihh` |

::: warning 安全提醒
部署到生产环境前，请立即修改以上所有默认密码。平台密码在【系统管理 → 用户管理】中修改；MySQL 与 EMQX 凭据可在 `docker-compose.yml` 的环境变量中调整，详见[部署与运维 → Docker Compose 部署](./deploy.md#docker-compose-部署)。
:::

## 方式二：裸机 JAR 部署

适合边缘网关（树莓派/工控机）或需要逐进程管控的场景。

### 第一步：启动 EMQX 消息总线

官方镜像已预置好 MQTT 鉴权配置，直接运行即可：

```bash
docker run -d --name unihh-emqx \
  -p 1883:1883 \
  -p 8083:8083 \
  -p 8084:8084 \
  -p 8883:8883 \
  -p 18083:18083 \
  registry.cn-shenzhen.aliyuncs.com/deversun/unihh-emqx:v20261009
```

启动后访问 `http://localhost:18083`（账号 `admin` / `public`）确认 EMQX 正常运行。

### 第二步：初始化数据库

在 MySQL 8.0 中创建数据库并执行初始化脚本：

```sql
CREATE DATABASE unihh_iot DEFAULT CHARACTER SET utf8mb4;
```

```bash
# 业务数据（必需）
mysql -u root -p unihh_iot < db/unihh_iot.sql

# 时序存储（如需启用 TDengine）
taos -s "CREATE DATABASE unihh_iot"
taos -u root -d unihh_iot < db/tdengine_schema.sql
```

### 第三步：构建并启动后端服务

```bash
# 根目录一次构建全部模块
mvn clean package -DskipTests
```

按顺序启动核心服务：

```bash
# 1. 统一网关 :28000
java -jar unihh-gateway/target/unihh-gateway.jar

# 2. 管理中心 :28001
java -jar unihh-admin/target/unihh-admin.jar

# 3. 数据中心 :28002
java -jar unihh-data/target/unihh-data.jar

# 4. 需要的协议驱动，例如 Modbus :28011
java -jar drivers/unihh-driver-modbus/target/unihh-driver-modbus.jar
```

生产环境推荐使用极小堆参数启动，详见[部署与运维](./deploy.md#jvm-内存调优)。

### 第四步：启动前端

```bash
cd unihh-frontend
npm install    # 或 pnpm install
npm run dev
```

浏览器访问 `http://localhost:3000`，登录账号见上文第五步的账号表。

## 验证平台状态

登录后进入监控大盘，确认以下状态：

1. **挂载协议驱动数** > 0，驱动显示在线；
2. **接入数采设备总数** 与你配置的设备数一致。

若驱动显示离线，可进入【驱动管理】点击**通道心跳自检**排查，或参考 [FAQ](../reference/faq.md)。

## 下一步

- [操作手册](../manual/)：了解每个功能页面的使用方式
- [驱动开发指南](./driver-dev.md)：接入你自己的协议
- [部署与运维](./deploy.md)：生产环境的存储选型、JVM 调优与升级流程
