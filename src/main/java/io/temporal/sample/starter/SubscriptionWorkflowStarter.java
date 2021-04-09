/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.temporal.sample.starter;

import static io.temporal.sample.workflow.SubscriptionWorkflowImpl.TASK_QUEUE;
import static io.temporal.sample.workflow.SubscriptionWorkflowImpl.WORKFLOW_ID;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.sample.activities.SubscriptionActivitiesImpl;
import io.temporal.sample.workflow.SubscriptionWorkflow;
import io.temporal.sample.workflow.SubscriptionWorkflowImpl;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.time.Duration;

/** Subscription workflow starter */
public class SubscriptionWorkflowStarter {

  public static void main(String[] args) {

    /*
     * Define the workflow service. It is a gRPC stubs wrapper which talks to the docker instance of
     * our locally running Temporal service.
     */
    WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();

    /*
     * Define the workflow client. It is a Temporal service client used to start, signal, and query
     * workflows
     */
    WorkflowClient client = WorkflowClient.newInstance(service);

    /*
     * Define the workflow factory. It is used to create workflow workers for a specific task queue.
     */
    WorkerFactory factory = WorkerFactory.newInstance(client);

    /*
     * Define the workflow worker. Workflow workers listen to a defined task queue and process
     * workflows and activities.
     */
    Worker worker = factory.newWorker(TASK_QUEUE);

    /*
     * Register our workflow implementation with the worker. Since workflows are stateful in nature,
     * we need to register our workflow type.
     */
    worker.registerWorkflowImplementationTypes(SubscriptionWorkflowImpl.class);

    /*
     * Register our workflow activity implementation with the worker. Since workflow activities are
     * stateless and thread-safe, we need to register a shared instance.
     */
    worker.registerActivitiesImplementations(new SubscriptionActivitiesImpl());

    // Start all the workers registered for a specific task queue.
    factory.start();

    // Create our workflow client stub. It is used to start our workflow execution.
    // For sake of the example we set the total workflow run timeout to 5 minutes
    SubscriptionWorkflow workflow =
        client.newWorkflowStub(
            SubscriptionWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowRunTimeout(Duration.ofMinutes(5))
                .build());

    /*
     * Start executing our workflow
     * Let's say we have a trial period of 5 seconds and a billing period of 10 seconds
     * In real life this would be much longer
     */
    Duration trialPeriod = Duration.ofSeconds(5);
    Duration billingPeriod = Duration.ofSeconds(10);

    // Our new customer id
    String customerId = "newCustomer123";

    // Start workflow execution (async to not use another thread to signal)
    WorkflowClient.start(workflow::startSubscription, customerId, trialPeriod, billingPeriod);

    // After 1 minute send a cancellation signal
    try {
      Thread.sleep(60 * 1000);
      workflow.cancelSubscription();

      // wait to see workflow activity cancel message..for sake of example
      Thread.sleep(3 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // Exit
    System.exit(0);
  }
}
