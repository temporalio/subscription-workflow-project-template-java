# Temporal Subscription Workflow Template - Java

Temporal customer subscription workflow example. 

### Setup

#### Build the example

```text
./gradlew build
```

#### Run Temporal server

```bash
git clone https://github.com/temporalio/docker-compose.git
cd docker-compose
docker-compose up
```

#### Start the example

```text
./gradlew -q execute -PmainClass=io.temporal.sample.starter.SubscriptionWorkflowStarter
```
