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

To start our customer subscriptions you can do:

```text
./gradlew -q execute -PmainClass=io.temporal.sample.starter.SubscriptionWorkflowStarter
```

This will start subscription workflows for 5 customers with ids:

```text
Id-0
Id-1
Id-2
Id-3
Id-4
```

You can query billing information for a specific customer after the workflows have started with:

```text
./gradlew -q execute -PmainClass=io.temporal.sample.starter.SubscriptionWorkflowQuerier --args="Id-0"
```

Here you can pass in an Id of an existing subscription customer such as "Id-0" for example


