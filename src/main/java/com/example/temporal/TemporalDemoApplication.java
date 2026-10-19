package com.example.temporal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class TemporalDemoApplication {

    private static final Logger log = LoggerFactory.getLogger(TemporalDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TemporalDemoApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("");
        log.info("==============================================");
        log.info("  Temporal Spring Demo is ready!");
        log.info("==============================================");
        log.info("  Start a workflow:");
        log.info("    POST http://localhost:8080/workflow/start");
        log.info("");
        log.info("  Check status (replace <id>):");
        log.info("    GET  http://localhost:8080/workflow/<id>/status");
        log.info("");
        log.info("  Send proceed signal (unblocks the workflow):");
        log.info("    POST http://localhost:8080/workflow/<id>/signal?message=hello");
        log.info("==============================================");
        log.info("");
    }
}
