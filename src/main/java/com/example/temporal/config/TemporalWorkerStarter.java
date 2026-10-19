package com.example.temporal.config;

import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TemporalWorkerStarter {

    private static final Logger log = LoggerFactory.getLogger(TemporalWorkerStarter.class);

    private final WorkerFactory factory;

    public TemporalWorkerStarter(WorkerFactory factory) {
        this.factory = factory;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void start() {
        if (!factory.isStarted()) {
            factory.start();
            log.info("Temporal WorkerFactory started — polling '{}'", TemporalConfig.TASK_QUEUE);
        }
    }

    @PreDestroy
    public void stop() {
        factory.shutdown();
        log.info("Temporal WorkerFactory shut down");
    }
}
