# Log Audit

The Log Audit page records two critical trails: write-back command logs and login audit logs, supporting incident reconstruction and security review.

![Log Audit](/images/logs.png)

## Status Cards

| Card | Meaning |
| :--- | :--- |
| Write-back commands today | Total control commands dispatched through the platform today |
| Sign-ins today | Total logins today |
| Sensitive config audits | Change records for devices, drivers, rules and other sensitive configuration |
| Avg. write-back latency | Mean time from command dispatch to driver execution return |

## Write-Back Command Logs

The full story of every control command and service invocation.

Filters: fuzzy search on log ID / rule name / device ID / command, plus execution state (SUCCESS / FAILED).

| Column | Description |
| :--- | :--- |
| Log ID | Unique trail number |
| Triggering rule | Empty for manual dispatch; shows the rule for automated actions |
| Target device | The device that received the command |
| Service command | The specific point write or service call |
| Input params snapshot | The exact parameters sent — the key field for auditing mistakes |
| Latency | From dispatch to driver execution return |
| Dispatched at | Send timestamp |
| Result | SUCCESS / FAILED |

**Failure triage order**: read the params snapshot → confirm the device is online → run the [driver heartbeat self-check](./drivers.md) → verify the point's access mode and physical address → on RS485, check bus load and conflicts.

## Login Audit Logs

Every sign-in attempt, successful or not.

Filters: fuzzy search on account / IP / client, plus login state (SUCCESS / FAILED).

| Column | Description |
| :--- | :--- |
| Log ID | Unique trail number |
| Account | The account attempting to sign in |
| IP and geolocation | Source address and region |
| Device and browser | Client fingerprint |
| Time | Login timestamp |

**Security review tips**: periodically filter FAILED records and watch for high failure counts on a single account (credential stuffing) and successful sign-ins from unusual regions.
