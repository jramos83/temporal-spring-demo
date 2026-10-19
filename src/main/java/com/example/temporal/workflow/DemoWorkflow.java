package com.example.temporal.workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DemoWorkflow {

    @WorkflowMethod
    String run(String input);

    @SignalMethod
    void proceed(String message);

    @QueryMethod
    String getStatus();
}
