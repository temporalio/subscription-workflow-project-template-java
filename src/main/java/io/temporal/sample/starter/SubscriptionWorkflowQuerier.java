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
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import java.util.Optional;

// Allows you to query billing information for an existing customer
public class SubscriptionWorkflowQuerier {

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

    // Passed in customer id
    String customerId = args[0];

    // Create a workflow stub for existing workflow execution by the customer id
    WorkflowStub workflowStub =
        client.newUntypedWorkflowStub(
            SubscriptionWorkflowStarter.WORKFLOW_ID_BASE + customerId,
            Optional.empty(),
            Optional.empty());

    // Print the customer billing info (from workflow query methods)
    printCustomerBillingInfo(workflowStub);
  }

  private static void printCustomerBillingInfo(WorkflowStub workflowStub) {
    System.out.println("*****************");
    System.out.println("Customer Id: " + workflowStub.query("queryCustomerId", Integer.class));
    System.out.println(
        "Billing Period #: " + workflowStub.query("queryBillingPeriodNumber", Integer.class));
    System.out.println(
        "Charge Amount: " + workflowStub.query("queryBillingPeriodChargeAmount", Integer.class));
  }
}
