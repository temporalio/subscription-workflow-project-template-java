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

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.sample.activities.SubscriptionActivitiesImpl;
import io.temporal.sample.model.Customer;
import io.temporal.sample.model.Subscription;
import io.temporal.sample.workflow.SubscriptionWorkflow;
import io.temporal.sample.workflow.SubscriptionWorkflowImpl;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Subscription workflow starter */
public class SubscriptionWorkflowStarter {

  // Task queue name
  public static final String TASK_QUEUE = "SubscriptionsTaskQueue";
  // Base id for all subscription workflow
  public static final String WORKFLOW_ID_BASE = "SubscriptionsWorkflow";

  /*
   * Define our Subscription
   * Let's say we have a trial period of 10 seconds and a billing period of 10 seconds
   * In real life this would be much longer
   * We also set the max billing periods to 24, and the billing cycle charge to 120
   */
  public static Subscription subscription =
      new Subscription(Duration.ofSeconds(10), Duration.ofSeconds(10), 24, 120);

  public static void main(String[] args) {

    /*
     * Define the workflow service. It is a gRPC stubs wrapper which talks to the docker instance of
     * our locally running Temporal service.
     * Defined here as reused by other starters
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

    // List of our example customers
    List<Customer> customers = new ArrayList<>();

    // Create example customers
    for (int i = 0; i < 5; i++) {
      Customer customer =
          new Customer("First Name" + i, "Last Name" + i, "Id-" + i, "Email" + i, subscription);
      customers.add(customer);
    }

    /*
     * Create and start a new subscription workflow
     * for each of the example customers
     */
    customers.forEach(
        customer -> {
          // Create our workflow client stub. It is used to start our workflow execution.
          // For sake of the example we set the total workflow run timeout to 5 minutes
          SubscriptionWorkflow workflow =
              client.newWorkflowStub(
                  SubscriptionWorkflow.class,
                  WorkflowOptions.newBuilder()
                      .setTaskQueue(TASK_QUEUE)
                      .setWorkflowId(WORKFLOW_ID_BASE + customer.getId())
                      .setWorkflowRunTimeout(Duration.ofMinutes(5))
                      .build());

          // Start workflow execution (async)
          WorkflowClient.start(workflow::startSubscription, customer);
        });
  }
}
