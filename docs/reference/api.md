# 接口速览

平台所有 REST 接口经统一网关（默认 `:28000`）暴露，携带 JWT Token 访问。接口按资源域划分为以下控制器，同一资源同时提供 `/api/v1/*` 与 `/admin/*` 两组前缀。

## 鉴权

| 项 | 说明 |
| :--- | :--- |
| 登录接口 | `POST /api/v1/auth/login`，返回 Token |
| 请求头 | `Authorization: Bearer <token>` |
| 未授权响应 | 401，前端自动跳转登录页 |

## 资源域

| 控制器 | 基础路径 | 覆盖能力 |
| :--- | :--- | :--- |
| AuthController | `/api/v1/auth` | 登录、登出、当前用户信息 |
| DashboardController | `/api/v1/dashboard` | 大盘统计卡片与图表数据 |
| DriverController | `/api/v1/drivers` | 驱动列表、状态；`GET|POST /api/v1/drivers/{driverCode}/ping` 通道心跳自检 |
| DeviceController | `/api/v1/devices` | 设备 CRUD、点位绑定、数采启停、反控下发 |
| ThingModelController | `/api/v1/thingmodels` | 物模型模板 CRUD、属性/服务/事件定义 |
| RuleController | `/api/v1/rules` | 规则 CRUD、启停、联动动作配置 |
| AlarmController | `/api/v1/alarms` | 事件查询、确认、恢复、处置记录 |
| LogController | `/api/v1/logs` | 反控下发日志、登录审计日志 |
| SysUserController | `/api/v1/system/user` | 用户 CRUD、状态启停 |
| SysRoleController | `/api/v1/system/role` | 角色 CRUD、权限分配 |
| SysMenuController | `/api/v1/system/menu` | 菜单树维护 |
| SysDictController | `/api/v1/system/dict` | 字典类型与键值维护 |
| ProtocolFallbackController | `/api/v1/protocol` | 协议兼容降级处理 |

## 调用示例

```bash
# 1. 登录获取 Token
curl -X POST http://localhost:28000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "unihhiot"}'

# 2. 携带 Token 查询设备列表
curl http://localhost:28000/api/v1/devices?pageNum=1&pageSize=10 \
  -H "Authorization: Bearer <token>"

# 3. 对 Modbus 驱动执行心跳自检
curl -X POST http://localhost:28000/api/v1/drivers/modbus/ping \
  -H "Authorization: Bearer <token>"
```

## 说明

- 响应体为统一 `R` 结构（`code` / `message` / `data`），`code = 200` 表示成功；
- 分页接口通用参数：`pageNum`、`pageSize`；
- 前端 `unihh-frontend/src/api/` 目录封装了全部接口调用，可直接作为对接参考；
- 驱动侧另有内置调试端点 `/api/v1/driver/status`（驱动进程自身暴露，返回活跃设备与 Socket 句柄数），平台 Ping 自检即调用该端点。
