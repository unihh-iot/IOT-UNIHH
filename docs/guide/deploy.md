# 部署与运维

## 部署形态

UNIHH-IOT 微服务间通过静态配置文件（`application.yml` / 环境变量）显式映射服务地址，不依赖注册中心，因此支持多种部署形态：

| 形态 | 适用场景 | 说明 |
| :--- | :--- | :--- |
| 单机裸机 JAR | 边缘网关、小体量项目 | 每个模块一个 JAR，`java -jar` 直接启停 |
| 边缘 + 云端混合 | 集团型多厂区 | 驱动跑在车间工控机，核心服务部署在机房或云主机 |
| 容器化 | 已有 Docker/K8s 基础设施 | 仓库自带 `docker-compose.yml` 一键编排，也可迁入 K8s |

## Docker Compose 部署

仓库根目录的 `docker-compose.yml` 编排了完整平台：1 个 MySQL、1 个 EMQX、3 个核心微服务与 3 个协议驱动，全部服务位于 `unihh-network` 桥接网络内，通过容器名互相寻址。

### 首次启动

```bash
# 1. 构建后端模块（镜像使用本地编译产物）
mvn clean package -DskipTests

# 2. 构建镜像（Linux/macOS 用 build-docker.sh，Windows 用 build-docker.bat）
bash build-docker.sh

# 3. 启动全部服务
docker compose up -d

# 4. 确认就绪：MySQL 需通过内置健康检查后 admin 才会启动
docker compose ps
```

MySQL 首次启动时自动执行 `db/unihh_iot.sql` 完成建库导表（挂载在 `/docker-entrypoint-initdb.d/`），数据持久化在命名卷 `mysql_data` 中。

### 构建与推送镜像

`build-docker.sh` 支持两种用法：

```bash
bash build-docker.sh          # 构建全部镜像
bash build-docker.sh unihh-admin   # 只构建单个模块镜像

# 启动单个模块（不连带重启依赖）
docker compose up -d --no-deps unihh-admin

# 推送镜像到私有仓库（配合 CI/CD）
docker compose push
```

镜像命名与推送目标由两个变量控制，可在 `.env` 文件或命令行覆盖：

| 变量 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `DOCKER_REGISTRY` | `registry.cn-shenzhen.aliyuncs.com/deversun` | 镜像仓库地址 |
| `VERSION` | `1.0.0` | 镜像标签 |

```bash
# 示例：推送到自己的仓库
DOCKER_REGISTRY=registry.example.com VERSION=1.1.0 docker compose build
DOCKER_REGISTRY=registry.example.com VERSION=1.1.0 docker compose push
```

### 环境变量与凭据

各服务的连接信息通过 compose 环境变量注入，生产环境建议集中调整：

| 服务 | 关键变量 | 默认值 |
| :--- | :--- | :--- |
| unihh-mysql | `MYSQL_ROOT_PASSWORD` | `123456` |
| unihh-admin | `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | 指向 `unihh-mysql:3306`，root / 123456 |
| unihh-admin / data / 驱动 | `EMQX_SERVER_URI` / `EMQX_USERNAME` / `EMQX_PASSWORD` | `tcp://unihh-emqx:1883`，unihh / unihh |
| unihh-gateway | `UNIHH_ADMIN_HOST` / `UNIHH_DATA_HOST` | `unihh-admin` / `unihh-data` |

::: warning
修改 MySQL 密码时需同步修改所有引用它的服务变量，并删除 `mysql_data` 卷后重新初始化（注意先备份业务数据）。
:::

### 日常运维命令

```bash
docker compose logs -f unihh-admin        # 跟踪某服务日志
docker compose restart unihh-driver-modbus  # 重启单个驱动
docker compose stop                       # 停止全部服务（保留容器）
docker compose down                       # 停止并删除容器（数据卷保留）
docker compose down -v                    # 连同数据卷一起删除（彻底重置）
```

### 容器化升级

1. 备份业务库：`docker exec unihh-mysql mysqldump -uroot -p123456 unihh_iot > backup.sql`；
2. 拉取新代码后重新构建：`mvn clean package -DskipTests && bash build-docker.sh`；
3. 滚动更新：`docker compose up -d`（compose 只重建镜像有变化的服务）；
4. 确认【驱动管理】中全部驱动恢复在线。

## Nginx 反向代理与前端托管

生产环境建议由 Nginx 统一对外：托管前端静态资源、反向代理 REST API，并承担 TLS 终结。后端微服务只监听内网端口。

### 构建前端产物

```bash
cd unihh-frontend
npm install
npm run build    # 产物输出到 unihh-frontend/dist/
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name unihh.example.com;

    # 前端静态资源（Vue Router 为 history 模式，需回退 index.html）
    root /opt/unihh/unihh-frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # Vite 构建产物带内容哈希，可长缓存
    location /assets/ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # REST API 反向代理到统一网关（路径原样透传，不做重写）
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

要点：

- `proxy_pass` 结尾**不要带路径**，`/api` 前缀原样透传给网关——网关按 `/api/v1/*` 路由，与前端开发模式（vite proxy）行为一致；
- 前端与 API 同域部署后不存在跨域问题；网关自身也已处理 CORS，分离部署亦可；
- `Host` 头按标准方式设置一次即可，不要重复注入（网关侧曾修复过代理重复头问题）。

### 可选：代理 EMQX Dashboard

如需从统一域名访问 EMQX 控制台，可增加一段代理（务必限制来源 IP 或仅内网开放）：

```nginx
location /emqx-dashboard/ {
    proxy_pass http://127.0.0.1:18083/;
    proxy_set_header Host $host;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

### HTTPS

- 证书配置在 Nginx（`listen 443 ssl`），微服务之间保持 HTTP 内网通信即可；
- MQTT over TLS 走 EMQX 自身的 8883 端口，**不经过 Nginx**；
- 反向控制等敏感指令建议同时收紧 MQTT 总线：启用 EMQX TLS、将 1883 端口限制在内网或防火墙白名单内。

## JVM 内存调优

平台全链路采用零拷贝、对象池复用与基本类型优先策略，堆内存可控制在极小范围。生产环境推荐参数：

```bash
# 核心业务服务（gateway / admin / data，推荐 64MB~128MB 堆）
java -server -Xms64m -Xmx128m \
  -XX:MaxMetaspaceSize=128m \
  -XX:ReservedCodeCacheSize=64m \
  -Xss256k \
  -XX:+UseSerialGC \
  -jar unihh-admin.jar

# 独立协议驱动（推荐 32MB~64MB 堆）
java -server -Xms32m -Xmx64m \
  -XX:MaxMetaspaceSize=64m \
  -XX:ReservedCodeCacheSize=32m \
  -Xss256k \
  -XX:+UseSerialGC \
  -jar unihh-driver-modbus.jar
```

选型依据：

- **SerialGC**：在小堆（<256MB）场景下停顿短、开销低，优于 G1/ZGC；
- **-Xss256k**：单线程栈压缩，边缘设备线程数受限时收益明显；
- 若服务运行在内存充裕的服务器上且吞吐较高，可将堆上调至 256MB~512MB 并换用 G1。

## 硬件参考配置

| 规模 | 设备 | 参考配置 |
| :--- | :--- | :--- |
| 边缘软网关（<50 台设备） | 树莓派 4B / 低配工控机 | 4 核，2GB 内存，32GB 存储 |
| 单厂区（50~500 台设备） | 1 台服务器 | 4 核 8GB，SSD |
| 多厂区/集团 | EMQX 集群 + 服务集群 | 按设备数水平扩展 EMQX 与微服务实例 |

## 存储引擎选择

`unihh-storage` 通过 `StorageEngineRouter` 策略路由，支持按业务动态指定：

| 引擎 | 适用数据 | 说明 |
| :--- | :--- | :--- |
| TDengine | 高频时序数采 | 首选。压缩比高，万亿点位的写入与降采样查询性能好 |
| TimescaleDB / InfluxDB | 时序数采 | 已有 PG/Influx 技术栈时的替代选择 |
| MySQL | 关键指令响应、告警记录 | 关系型备份，便于关联业务数据 |
| Redis | 点位实时快照 | 前端实时刷新直接读取，不落盘 |
| Memory | 高频采集削峰 | 本地环形缓冲，配合批量落库使用 |

`DataStorageEngine` 统一接口如下，自定义引擎实现该接口并注册即可接入路由：

```java
public interface DataStorageEngine {
    String getEngineType();
    void init(StorageConfig config) throws Exception;
    void save(PointData data) throws Exception;                // 单条保存（指令响应/高危告警）
    void saveBatch(List<PointData> dataList) throws Exception; // 批量保存（高频周期采集削峰）
    List<PointData> queryHistory(HistoryQuery query) throws Exception;
    void close();
}
```

## 日常运维要点

### 驱动健康巡检

- 平台【驱动管理】页可随时点击**通道心跳自检**，对驱动做 HTTP + MQTT 双重探测并返回 RTT 时延；
- 心跳看门狗：驱动心跳超过 15 秒未更新，平台自动置离线并级联下挂设备；
- 建议在监控系统中对 EMQX 的连接数与报文吞吐建立基线告警。

### 日志与审计

- 【日志审计】页提供设备反控下发日志与用户登录审计两类流水，可作为故障追溯依据；
- 反控日志记录了下发参数快照、耗时与执行结果，排查反控失败时优先查看。

### 配置修改

- 修改设备连接参数或点位配置后，平台经 MQTT 热通知对应驱动，无需重启；
- 修改驱动自身配置（如 JVM 参数、端口）需要重启对应驱动进程，平台会在其重新上线后自动恢复注册信息。

## 裸机 JAR 升级

1. 备份 MySQL 业务库与 TDengine 时序库；
2. `mvn clean package -DskipTests` 构建新版本；
3. 逐个替换核心服务 JAR 并重启（先 data，再 admin，最后 gateway）；
4. 逐个重启驱动服务，确认【驱动管理】中全部驱动恢复在线。

驱动与平台通过 MQTT 总线解耦，驱动短暂离线不会丢失平台侧配置，重新上线后自动同步。
