# 部署与运维

## 部署形态

UNIHH-IOT 微服务间通过静态配置文件（`application.yml` / 环境变量）显式映射服务地址，不依赖注册中心，因此支持多种部署形态：

| 形态 | 适用场景 | 说明 |
| :--- | :--- | :--- |
| 单机裸机 JAR | 边缘网关、小体量项目 | 每个模块一个 JAR，`java -jar` 直接启停 |
| 边缘 + 云端混合 | 集团型多厂区 | 驱动跑在车间工控机，核心服务部署在机房或云主机 |
| 容器化 | 已有 Docker/K8s 基础设施 | 每个微服务一个容器，EMQX 建议单独部署或使用集群 |

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

## 升级

1. 备份 MySQL 业务库与 TDengine 时序库；
2. `mvn clean package -DskipTests` 构建新版本；
3. 逐个替换核心服务 JAR 并重启（先 data，再 admin，最后 gateway）；
4. 逐个重启驱动服务，确认【驱动管理】中全部驱动恢复在线。

驱动与平台通过 MQTT 总线解耦，驱动短暂离线不会丢失平台侧配置，重新上线后自动同步。
