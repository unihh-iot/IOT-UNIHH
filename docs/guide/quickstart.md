# 快速开始

本文档引导你在本地把 UNIHH-IOT 完整跑起来，全过程约 10 分钟。

## 环境要求

| 组件 | 版本要求 | 用途 |
| :--- | :--- | :--- |
| Docker | 20+ | 运行 EMQX 消息总线 |
| JDK | 8 或 17 | 后端微服务 |
| Maven | 3.6+ | 后端构建 |
| MySQL | 8.0 | 业务数据存储 |
| Node.js | 16+ | 前端构建 |
| Redis | 5.0+ | 状态缓存（可选） |
| TDengine | 3.x | 时序存储（可选） |

## 第一步：启动 EMQX 消息总线

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

端口说明：

| 端口 | 用途 |
| :--- | :--- |
| 1883 | MQTT TCP，微服务与驱动的通信总线 |
| 8883 | MQTT SSL/TLS |
| 8083 | MQTT WebSocket |
| 8084 | MQTT WSS |
| 18083 | EMQX Dashboard 管理控制台 |

启动后访问 `http://localhost:18083`（账号 `admin` / `public`）确认 EMQX 正常运行。

## 第二步：初始化数据库

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

## 第三步：构建并启动后端服务

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

## 第四步：启动前端

```bash
cd unihh-frontend
npm install    # 或 pnpm install
npm run dev
```

浏览器访问 `http://localhost:5173`。

## 第五步：登录平台

| 系统入口 | 访问地址 | 账号 | 默认密码 |
| :--- | :--- | :--- | :--- |
| UNIHH-IOT 平台 | `http://localhost:5173` | `admin` | `unihhiot` |
| EMQX Dashboard | `http://localhost:18083` | `admin` | `public` |
| MQTT 总线 | `tcp://localhost:1883` | `unihh` | `unihh` |

::: warning 安全提醒
部署到生产环境前，请立即修改以上所有默认密码。平台密码在【系统管理 → 用户管理】中修改；EMQX 凭据在各微服务的 `application.yml` 中配置。
:::

## 验证平台状态

登录后进入监控大盘，确认以下状态：

1. **挂载协议驱动数** > 0，驱动显示在线；
2. **接入数采设备总数** 与你配置的设备数一致。

若驱动显示离线，可进入【驱动管理】点击**通道心跳自检**排查，或参考 [FAQ](../reference/faq.md)。

## 下一步

- [操作手册](../manual/)：了解每个功能页面的使用方式
- [驱动开发指南](./driver-dev.md)：接入你自己的协议
