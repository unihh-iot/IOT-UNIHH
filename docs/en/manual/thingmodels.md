# Thing Models

A thing model is the standardized abstraction of a device type: its acquisition, control and alerting capabilities defined once as a template. Devices bound to a model share a uniform data structure, so upstream applications never deal with protocol differences.

![Thing Models](/images/thingmodels.png)

## Finding Templates

Search by **template code / name** and filter by industry category. Built-in categories:

- Rotating machinery / variable-frequency motors
- PLC controllers / automation
- Industrial Ethernet gateways
- Environment and energy monitoring

## Creating a Model

| Field | Description |
| :--- | :--- |
| Model code (ID) | Unique; use a business-semantic identifier (e.g. `centrifugal-fan`) |
| Model name | Display name |
| Industry category | For organization and search |
| Description | What kind of devices this template covers |

## The Three Elements

A model consists of properties, services and events, edited in separate sections.

### Properties

Properties describe measurable state — the basic unit of acquisition:

| Field | Description |
| :--- | :--- |
| Point code | Unique identifier used in acquisition and storage (e.g. `MotorTemp`) |
| Point name | Display name (e.g. "motor winding temperature") |
| Data type | FLOAT / DOUBLE / INT / STRING / BOOL / ENUM |
| Enum mapping | For ENUM: key-value pairs (e.g. `1: running, 0: stopped`) |
| Access mode | R (read-only) / RW (read-write; only RW properties can be controlled) |
| Sampling mode | `PERIODIC` (on the device cycle) / `COV` (report on change) |
| Unit | °C, MPa, kW… used in display and alerts |
| Description | Free-form detail |

### Services

Services describe compound actions a device can perform (e.g. "remote start", "set frequency to 30 Hz"):

| Field | Description |
| :--- | :--- |
| Service code | Unique identifier |
| Service name | Display name |
| Input parameters | The parameter list required to invoke the service (name, type, default) |

Services are invoked from the control panel on [Device Management](./devices.md).

### Events

Events describe abnormal conditions or state reports a device can raise:

| Field | Description |
| :--- | :--- |
| Event code | Unique identifier |
| Event name | Display name |
| Event type | Fault / alarm / info, etc. |
| Alert level | Default severity when triggered |
| Payload description | Event payload structure and trigger conditions |

## How Models Fit Together

```
Thing model (template) ──bind──> Device (instance) ──point mapping──> Physical registers
       │                                                        │
       └── properties / events ──> Rule engine targets <────────┘
```

- One model serves many devices; edit once, effective everywhere;
- [Rules](./rules.md) reference model properties and events directly as monitoring targets;
- Built-in templates can be duplicated and adjusted as a starting point.

## Modeling Advice

- Align property granularity with actual instrument measurements — one measurement, one property — for traceability;
- Mark controllable states as RW and pure monitoring values as R to prevent accidental writes;
- Prefer `COV` sampling for slowly-changing points to cut bus and storage load significantly.
