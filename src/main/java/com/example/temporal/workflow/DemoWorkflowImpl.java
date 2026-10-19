package com.example.temporal.workflow;

import com.example.temporal.activity.LoggingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class DemoWorkflowImpl implements DemoWorkflow {

    // Use Workflow.getLogger so logs are suppressed during replay
    private static final Logger log = Workflow.getLogger(DemoWorkflowImpl.class);

    private final LoggingActivities activities = Workflow.newActivityStub(
            LoggingActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    private boolean signalReceived = false;
    private String  signalMessage  = null;
    private String  status         = "STARTED";

    @Override
    public String run(String input) {
        log.info("Workflow starting with input: {}", input);

        status = "RUNNING_ACTIVITY_A";
        activities.activityA();

        status = "RUNNING_ACTIVITY_B";
        activities.activityB();

        status = "WAITING_FOR_SIGNAL";
        log.info("Both activities done — waiting for 'proceed' signal...");

        // Block here indefinitely until the signal arrives
        Workflow.await(() -> signalReceived);

        status = "COMPLETED";
        log.info("Signal received: '{}' — workflow completing", signalMessage);
        return "Done. Signal was: " + signalMessage;
    }

    @Override
    public void proceed(String message) {
        this.signalMessage  = message;
        this.signalReceived = true;
    }

    @Override
    public String getStatus() {
        return status;
    }
}
