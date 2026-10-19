# Operation Manual

This manual covers every functional page of the UNIHH-IOT platform. It is written for field engineers, system integrators and operations staff.

## Signing In

Open the frontend URL (`http://localhost:5173` in local development) and sign in. The default super-admin account is `admin` / `unihhiot`.

![Login](/images/login.png)

Credentials are stored as a token in the browser; when it expires you are redirected back to the login page. Forgotten passwords are reset by an administrator under **System → User Management**.

::: warning
Change the default passwords immediately after production deployment and issue individual accounts with proper roles to every operator.
:::

## Layout

After signing in you land on the dashboard. The left sidebar carries the navigation menu, filtered by your role's permissions; the top bar holds the current user and the sign-out action.

## Page Index

| Page | Description |
| :--- | :--- |
| [Dashboard](./dashboard.md) | Global status of devices, drivers, throughput and alerts |
| [Driver Management](./drivers.md) | Driver status, parameters and heartbeat self-checks |
| [Device Management](./devices.md) | Device registry, point binding, acquisition toggle, write-back control |
| [Thing Models](./thingmodels.md) | Property / service / event modeling |
| [Data Monitor](./data.md) | Live point values, quality flags and snapshots |
| [Rule Engine](./rules.md) | Threshold conditions and linked actions |
| [Alarm Center](./alarms.md) | Event browsing, acknowledgment and disposition |
| [Log Audit](./logs.md) | Write-back command trail and login audit |
| [System Management](./system.md) | Users, roles, menus and dictionaries |

## Recommended First Path

To walk the full "model → connect → acquire → alert" chain for the first time:

1. [Create a thing model](./thingmodels.md) defining the properties, services and events of one device type;
2. [Check the driver](./drivers.md) for that protocol is running and online;
3. [Create a device](./devices.md), bind the model and driver, configure connection parameters and point mapping;
4. [Watch live data](./data.md) to confirm acquisition and quality;
5. [Configure a rule](./rules.md) on a key point;
6. [Handle the alarm](./alarms.md) — simulate an out-of-range value and verify the chain.
