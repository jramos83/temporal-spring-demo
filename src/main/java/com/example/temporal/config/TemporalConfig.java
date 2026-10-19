package com.example.temporal.config;

import com.example.temporal.activity.LoggingActivitiesImpl;
import com.example.temporal.workflow.DemoWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

    private static final Logger log = LoggerFactory.getLogger(TemporalConfig.class);

    public static final String TASK_QUEUE = "demo-task-queue";

    // Connects to `temporal server start-dev` on localhost:7233
    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newLocalServiceStubs();
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs stubs) {
        return WorkflowClient.newInstance(stubs);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client) {
        return WorkerFactory.newInstance(client);
    }

    @Bean
    public Worker demoWorker(WorkerFactory factory, LoggingActivitiesImpl activities) {
        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(DemoWorkflowImpl.class);
        worker.registerActivitiesImplementations(activities);
        log.info("Worker registered on task queue: {}", TASK_QUEUE);
        return worker;
    }
}
