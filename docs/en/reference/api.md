# API Overview

All REST endpoints are exposed through the unified gateway (default `:28000`) and require a JWT token. Interfaces are grouped by resource domain; each domain is reachable under both `/api/v1/*` and `/admin/*` prefixes.

## Authentication

| Item | Description |
| :--- | :--- |
| Login endpoint | `POST /api/v1/auth/login`, returns a token |
| Request header | `Authorization: Bearer <token>` |
| Unauthorized | 401; the frontend redirects to the login page |

## Resource Domains

| Controller | Base path | Coverage |
| :--- | :--- | :--- |
| AuthController | `/api/v1/auth` | Sign in, sign out, current user |
| DashboardController | `/api/v1/dashboard` | Dashboard cards and chart data |
| DriverController | `/api/v1/drivers` | Driver list and state; `GET|POST /api/v1/drivers/{driverCode}/ping` heartbeat self-check |
| DeviceController | `/api/v1/devices` | Device CRUD, point binding, acquisition toggle, write-back commands |
| ThingModelController | `/api/v1/thingmodels` | Model template CRUD, property/service/event definitions |
| RuleController | `/api/v1/rules` | Rule CRUD, enable/disable, linked actions |
| AlarmController | `/api/v1/alarms` | Event query, acknowledge, recover, disposition notes |
| LogController | `/api/v1/logs` | Write-back command logs, login audit |
| SysUserController | `/api/v1/system/user` | User CRUD, state toggle |
| SysRoleController | `/api/v1/system/role` | Role CRUD, permission assignment |
| SysMenuController | `/api/v1/system/menu` | Menu tree maintenance |
| SysDictController | `/api/v1/system/dict` | Dictionary types and entries |
| ProtocolFallbackController | `/api/v1/protocol` | Protocol compatibility fallback |

## Example Calls

```bash
# 1. Sign in to get a token
curl -X POST http://localhost:28000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "unihhiot"}'

# 2. List devices with the token
curl "http://localhost:28000/api/v1/devices?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer <token>"

# 3. Ping the Modbus driver
curl -X POST http://localhost:28000/api/v1/drivers/modbus/ping \
  -H "Authorization: Bearer <token>"
```

## Notes

- Responses use the unified `R` structure (`code` / `message` / `data`); `code = 200` means success;
- Paginated endpoints take `pageNum` and `pageSize`;
- The frontend wraps every call in `unihh-frontend/src/api/` — a practical reference for integration work;
- Drivers also expose a built-in debug endpoint `/api/v1/driver/status` (on the driver process itself, returning active devices and socket handles); the platform's ping self-check calls it.
