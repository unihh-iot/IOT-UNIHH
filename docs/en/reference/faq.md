# FAQ

## Deployment and Startup

### A microservice fails with an MQTT connection error

Check in order:

1. Is the EMQX container running: `docker ps | grep unihh-emqx`;
2. Is port 1883 reachable: `telnet localhost 1883`;
3. Do the `application.yml` files use the right MQTT credentials (`unihh` / `unihh` by default);
4. Does the EMQX Dashboard (`http://localhost:18083`) show client connections.

### Is the service startup order strict?

The recommended order is `gateway → admin → data → drivers`. A driver started before admin still registers fine (admin re-syncs registrations when it comes up), but MySQL and EMQX must be ready first.

### Will it run on minimal hardware?

Yes. Core services take `-Xmx128m` and drivers `-Xmx64m` (see [Deployment](../guide/deploy.md#jvm-memory-tuning)). A 2 GB Raspberry Pi can host the minimal system: EMQX + three services + one driver.

## Drivers and Acquisition

### A driver shows offline — how do I debug it?

1. Run the **channel heartbeat self-check** on [Driver Management](../manual/drivers.md); the RTT tells network issues from process issues;
2. Confirm the process is alive and its port is listening;
3. Check driver logs for MQTT errors;
4. The watchdog timeout is 15 seconds — brief network jitter recovers on its own.

### A point keeps showing abnormal (Quality=0)?

Bad quality means the link works but the read fails. Likely causes, roughly in order:

1. Wrong physical register address (e.g. Modbus function-code area vs. address offset mismatch);
2. Data type does not match the register width (e.g. reading a DOUBLE space with INT);
3. Wrong byte order (see `IndustrialEndianUtils` in the driver SDK);
4. The device rejects the read (permissions, partially readable registers).

### Values disagree with the field instrument?

A systematic offset is a range conversion issue: check the point's **Scale and Offset** — engineering value = raw × Scale + Offset. Wildly jumping values usually mean byte order.

### How do I reduce RS485 bus load?

- Increase the device base acquisition cycle;
- Switch slowly-changing points to `COV` (report on change) instead of periodic sampling;
- Bind only the points you actually need;
- Write-back and polling are already serialized by the channel lock — no extra configuration needed.

## Write-Back Control

### A write-back command fails

1. Confirm the point's access mode is RW (read-only points cannot be written);
2. Check the params snapshot and failure reason in [Log Audit](../manual/logs.md);
3. Confirm the device is online and reading on the same channel;
4. Run the driver heartbeat self-check.

### Can write-back collide with polling (interleaved frames)?

No. The driver applies a fair mutex per device channel (`getChannelLock`), so polling and writes queue up and execute serially — that is precisely the platform's design goal for half-duplex buses.

## Data and Storage

### Where do live values live?

In Redis as a live snapshot (read directly by the frontend) plus the time-series store on disk. The engine is chosen by the `StorageEngineRouter` strategy — see [Deployment](../guide/deploy.md#choosing-storage-engines).

### Can I skip TDengine?

Yes. Running `db/unihh_iot.sql` alone is enough; history falls back to the other configured engines or MySQL. Add TDengine when you need high-frequency time-series analytics.

## Accounts and Security

### What are the default credentials?

| Entry | Account | Default password |
| :--- | :--- | :--- |
| Platform console | `admin` | `unihhiot` |
| EMQX Dashboard | `admin` | `public` |
| MQTT bus | `unihh` | `unihh` |

Change all of them before production.

### I forgot the admin password

Have another administrator reset it under **System → User Management**. If it is the only account, update the password field for that row in the `sys_user` table (bcrypt) or copy a known bcrypt hash from another account temporarily.

## Misc

### How do I add a new protocol?

Follow the [Driver Development Guide](../guide/driver-dev.md): extend `AbstractDriverPlugin`, implement three methods, done. PRs for new drivers are welcome.

### A problem the docs do not cover?

Open an issue on the [GitHub repository](https://github.com/unihh-iot/UNIHH) with the protocol, device model and log excerpts — maintainers will follow up.
