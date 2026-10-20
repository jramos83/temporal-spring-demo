# temporal-spring-demo

A minimal Spring Boot application that demonstrates [Temporal](https://temporal.io/) workflow orchestration locally and against Temporal Cloud.

## What it does

When you start a workflow, the following sequence runs:

```
POST /workflow/start
        │
        ▼
  DemoWorkflow.run()
        │
        ├─► Activity A  (logs a message)
        │
        ├─► Activity B  (logs a message)
        │
        └─► Workflow.await(...)   ← blocks here indefinitely
                │
                │   POST /workflow/{id}/signal
                │
                └─► proceed(message) signal received
                        │
                        ▼
                   Workflow completes
                   returns "Done. Signal was: <message>"
```

The workflow is **durable** — if the Worker process crashes while waiting for the signal, Temporal replays the event history and the workflow picks up exactly where it left off.

## Project structure

```
src/main/java/com/example/temporal/
├── TemporalDemoApplication.java      # Spring Boot entry point
├── activity/
│   ├── LoggingActivities.java        # @ActivityInterface
│   └── LoggingActivitiesImpl.java    # logs with timestamp, marked @Component
├── workflow/
│   ├── DemoWorkflow.java             # @WorkflowInterface (run + proceed signal + getStatus query)
│   └── DemoWorkflowImpl.java         # blocks on Workflow.await(() -> signalReceived)
├── config/
│   ├── TemporalConfig.java           # wires stubs, client, worker — profile-aware
│   └── TemporalWorkerStarter.java    # starts/stops WorkerFactory with Spring lifecycle
└── web/
    └── WorkflowController.java       # REST endpoints: start, status, signal
```

## Prerequisites

- Java 17+
- [Temporal CLI](https://docs.temporal.io/cli)

## Running locally

**1. Start the Temporal dev server**

```bash
temporal server start-dev
```

Temporal UI will be available at http://localhost:8233.

**2. Start the Spring Boot app**

```bash
./gradlew bootRun
```

The app connects to `localhost:7233` by default.

## REST API

### Start a workflow

```bash
curl -X POST "http://localhost:8080/workflow/start?input=hello"
```

Response:
```json
{
  "workflowId": "demo-<uuid>",
  "status": "started",
  "note": "Workflow is running activities A & B, then waiting for a signal"
}
```

### Check status

```bash
curl "http://localhost:8080/workflow/{workflowId}/status"
```

Returns one of: `STARTED` → `RUNNING_ACTIVITY_A` → `RUNNING_ACTIVITY_B` → `WAITING_FOR_SIGNAL` → `COMPLETED`

### Send the signal (unblocks the workflow)

```bash
curl -X POST "http://localhost:8080/workflow/{workflowId}/signal?message=go"
```

You can also send the signal from the **Temporal UI** → open the workflow → **More Actions → Send a Signal** → signal name `proceed`, payload `"your message"`.

## Temporal Cloud

Swap to the `cloud` Spring profile to connect to [Temporal Cloud](https://cloud.temporal.io) instead of a local dev server.

**1. Update `application-cloud.properties`**

```properties
temporal.target=your-namespace.tmprl.cloud:7233
temporal.namespace=your-namespace.your-account-id
temporal.tls.cert-path=certs/client.pem
temporal.tls.key-path=certs/client.key
```

Get the target and namespace from your Temporal Cloud namespace page. Download the client certificate and key from the namespace's **CA Certificates** settings.

**2. Place your certs**

```
certs/
├── client.pem
└── client.key
```

Keep the `certs/` directory out of version control (it is gitignored).

**3. Run with the cloud profile**

```bash
./gradlew bootRun --args='--spring.profiles.active=cloud'
```

The REST API is identical — only the Temporal connection changes.

## Key Temporal concepts used

| Concept | Where |
|---|---|
| `@WorkflowInterface` / `@WorkflowMethod` | `DemoWorkflow.java` |
| `@SignalMethod` | `proceed()` in `DemoWorkflow.java` |
| `@QueryMethod` | `getStatus()` in `DemoWorkflow.java` |
| `@ActivityInterface` / `@ActivityMethod` | `LoggingActivities.java` |
| `Workflow.await(condition)` | `DemoWorkflowImpl.java` — durable block until signal |
| `WorkflowServiceStubs` | `TemporalConfig.java` — local vs cloud connection |
| `WorkerFactory` | `TemporalConfig.java` + `TemporalWorkerStarter.java` |
