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

package io.temporal.sample.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.sample.activities.SubscriptionActivities;
import io.temporal.workflow.Workflow;
import java.time.Duration;

/** Subscription workflow implementation. Note this is just a POJO. */
public class SubscriptionWorkflowImpl implements SubscriptionWorkflow {
  public static final String TASK_QUEUE = "SubscriptionsTaskQueue";
  public static final String WORKFLOW_ID = "SubscriptionsWorkflow";

  private int billingPeriodNum = 0;
  private boolean subscriptionCancelled = false;

  // Define subscription activities stub
  // setScheduleToCloseTimeout defines a timeout for an activity to complete
  private final SubscriptionActivities activities =
      Workflow.newActivityStub(
          SubscriptionActivities.class,
          ActivityOptions.newBuilder().setScheduleToCloseTimeout(Duration.ofSeconds(5)).build());

  @Override
  public void startSubscription(String customerId, Duration trialPeriod, Duration billingPeriod) {
    // Send welcome email to customer
    activities.sendWelcomeEmail(customerId);

    // Start the free trial period. User can still cancel subscription during this time
    Workflow.await(trialPeriod, () -> subscriptionCancelled);

    // If customer cancelled their subscription during trial period, send notification email
    if (subscriptionCancelled) {
      activities.sendCancellationEmailDuringTrialPeriod(customerId);
      // We have completed subscription for this customer.
      // Finishing workflow execution
      return;
    }

    // Trial period is over, start billing until cancelled
    while (true) {
      // Charge customer for the billing period
      activities.chargeCustomerForBillingPeriod(customerId, billingPeriodNum);

      // Wait 1 billing period to charge customer again or if they cancel subscription
      // whichever comes first
      Workflow.await(billingPeriod, () -> subscriptionCancelled);
      billingPeriodNum++;

      // If customer cancelled their subscription send notification email
      // Otherwise bill the customer and send billing notification
      if (subscriptionCancelled) {
        activities.sendCancellationEmailDuringActiveSubscription(customerId);

        // We have completed subscription for this customer.
        // Finishing workflow execution
        break;
      }
    }
  }

  @Override
  public void cancelSubscription() {
    subscriptionCancelled = true;
  }
}
