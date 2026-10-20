package com.example.temporal.config;

import com.example.temporal.activity.LoggingActivitiesImpl;
import com.example.temporal.workflow.DemoWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.FileInputStream;

@Configuration
public class TemporalConfig {

    private static final Logger log = LoggerFactory.getLogger(TemporalConfig.class);

    public static final String TASK_QUEUE = "demo-task-queue";

    // ── Local (default) ──────────────────────────────────────────────────────

    @Bean
    @Profile("!cloud")
    public WorkflowServiceStubs localServiceStubs() {
        log.info("Connecting to local Temporal dev server (localhost:7233)");
        return WorkflowServiceStubs.newLocalServiceStubs();
    }

    @Bean
    @Profile("!cloud")
    public WorkflowClient localWorkflowClient(WorkflowServiceStubs stubs) {
        return WorkflowClient.newInstance(stubs);
    }

    // ── Temporal Cloud ────────────────────────────────────────────────────────

    @Bean
    @Profile("cloud")
    public WorkflowServiceStubs cloudServiceStubs(
            @Value("${temporal.target}") String target,
            @Value("${temporal.tls.cert-path}") String certPath,
            @Value("${temporal.tls.key-path}") String keyPath) throws Exception {

        log.info("Connecting to Temporal Cloud at {}", target);
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(target)
                        .setSslContext(SimpleSslContextBuilder
                                .forPKCS8(new FileInputStream(certPath), new FileInputStream(keyPath))
                                .build())
                        .build()
        );
    }

    @Bean
    @Profile("cloud")
    public WorkflowClient cloudWorkflowClient(
            WorkflowServiceStubs stubs,
            @Value("${temporal.namespace}") String namespace) {
        log.info("Using Temporal namespace: {}", namespace);
        return WorkflowClient.newInstance(stubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(namespace)
                        .build());
    }

    // ── Shared ────────────────────────────────────────────────────────────────

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
