# Temporal Subscription Workflow Template - Java

Temporal customer subscription Workflow example. 

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
##### Querying billing information:

You can query billing information for a specific customer after the workflows have started with:

```text
./gradlew -q execute -PmainClass=io.temporal.sample.starter.QueryBillingInfo --args="Id-0"
```
Here we pass in an Id of an existing subscription customer such as "Id-0" for example.

##### Update billing cycle cost:

You can also update the billing cycle cost for a specific customer while the workflow is running:

```text
./gradlew -q execute -PmainClass=io.temporal.sample.starter.UpdateBillingCycleCharge --args="Id-0 300"
```
Here we update the billing cycle cost for customer with id "Id-0" to 300. 

##### Cancel subscription (signal)

You can cancel a subscription for a specific customer while the workflows are running:

```text
./gradlew -q execute -PmainClass=io.temporal.sample.starter.CancelSubscription --args="Id-0"
```

Here we cancel the subscription for customer with id "Id-0"