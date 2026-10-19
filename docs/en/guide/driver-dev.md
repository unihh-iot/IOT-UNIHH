# Driver Development Guide

Southbound drivers are built on the `unihh-protocol` foundation. MQTT connectivity, registration, heartbeats, config sync and command listening are all handled by the framework — you only implement protocol encoding and decoding.

## Three Steps to a New Driver

### Step 1: Create a project and add the SDK

Create a Maven project and add the `unihh-protocol` dependency:

```xml
<dependency>
    <groupId>com.unihh.iot</groupId>
    <artifactId>unihh-protocol</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Step 2: Extend `AbstractDriverPlugin`

```java
package com.unihh.iot.driver.custom;

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.protocol.annotation.IoTDriver;
import com.unihh.iot.protocol.base.AbstractDriverPlugin;
import com.unihh.iot.protocol.model.DriverMetadata;
import com.unihh.iot.protocol.model.ParsedPayload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@IoTDriver(code = "custom-protocol", name = "Custom Protocol Driver", version = "2.0.0")
public class CustomDriverPlugin extends AbstractDriverPlugin {

    @Override
    public String getDriverCode() {
        return "custom-protocol";
    }

    @Override
    public DriverMetadata getMetadata() {
        DriverMetadata meta = new DriverMetadata();
        meta.setDriverCode(getDriverCode());
        meta.setDriverName("Custom Protocol Driver");
        meta.setVersion("2.0.0");
        return meta;
    }

    @Override
    public boolean openConnection(DeviceConfig config) throws Exception {
        // Establish a long connection with the physical device
        return true;
    }

    @Override
    public ParsedPayload readPoints(DeviceConfig config, List<ThingModel.PropertySpec> points) throws Exception {
        ParsedPayload payload = new ParsedPayload();
        // Fill in acquired points:
        // payload.addPoint(pointCode, value, quality, timestamp)
        return payload;
    }

    @Override
    public boolean writePoint(DeviceConfig config, String pointCode, String address, Object value, String dataType) throws Exception {
        // Write-back: encode the write frame and send it to the device
        return true;
    }
}
```

What each method does:

| Method | Invoked when | Responsibility |
| :--- | :--- | :--- |
| `openConnection` | After device config is pushed | Open the connection; returning `false` schedules a retry |
| `readPoints` | On each polling cycle | Batch-read points and fill in values, quality and timestamps |
| `writePoint` | On a control command | Encode and send the write frame; return success or failure |

::: tip Channel mutex
Calls to `readPoints` and `writePoint` are already serialized through the framework's `getChannelLock`. For RS485 half-duplex buses or single-session TCP devices, do not add another lock for the same device inside your plugin.
:::

### Step 3: Add a bootstrap class and run

```java
package com.unihh.iot.driver.custom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.unihh.iot"})
public class CustomDriverApplication {
    public static void main(String[] args) {
        System.setProperty("server.port", "28019");
        SpringApplication.run(CustomDriverApplication.class, args);
    }
}
```

Once started, the framework handles registration, heartbeats, config pulls and polling automatically. The new driver will appear on the platform's **Driver Management** page within moments, showing as online.

## What the Framework Provides

| Capability | Details |
| :--- | :--- |
| Self-registration | Broadcasts metadata and HTTP endpoint to `iot/driver/register` at startup |
| Periodic heartbeat | Reports every 5 seconds with active channel metrics |
| LWT last-will | EMQX publishes the will message if the process dies; the platform reacts in seconds |
| Hot config sync | Device/point changes are pushed via `iot/device/config/update` — no driver restart |
| Polling scheduler | `DriverPollingEngine` invokes `readPoints` on each device's acquisition cycle |
| Command listener | Subscribes to `iot/driver/{code}/cmd` and dispatches to `writePoint` |
| REST debug endpoint | Built-in `/api/v1/driver/status` used by the platform's ping self-check |

## Data Types and Point Mapping

Thing model properties support: `FLOAT`, `DOUBLE`, `INT`, `INT32`, `INT64`, `BOOL`, `STRING`, `ENUM`.

When binding points to a device you configure:

- **Physical register address**: protocol-specific (e.g. Modbus `40001`, S7 `DB1.DBW0`);
- **Scale and offset**: engineering value = raw value × Scale + Offset, applied platform-side — drivers only report raw values;
- **Sampling mode**: `PERIODIC` (fixed cycle) or `COV` (report on change).

## Reference Drivers

Three production drivers ship with the repository and make good templates:

| Driver | Location | Worth studying for |
| :--- | :--- | :--- |
| Modbus | `drivers/unihh-driver-modbus` | TCP/RTU dual mode, multi-slave polling, register write-back |
| Siemens S7 | `drivers/unihh-driver-s7` | S7-200 SMART/300/1200/1500, DB blocks and memory areas |
| OPC UA | `drivers/unihh-driver-opcua` | NodeId subscription and method calls |

## Contributing a Driver

Drivers for BACnet, DL/T 645, Mitsubishi MC, Omron FINS and other protocols are welcome. See the contributing section in the [repository README](https://github.com/unihh-iot/UNIHH) for the workflow.
