package com.example.temporal.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LoggingActivitiesImpl implements LoggingActivities {

    private static final Logger log = LoggerFactory.getLogger(LoggingActivitiesImpl.class);

    @Override
    public void activityA() {
        log.info("========================================");
        log.info("  ACTIVITY A  executing at {}", Instant.now());
        log.info("========================================");
    }

    @Override
    public void activityB() {
        log.info("========================================");
        log.info("  ACTIVITY B  executing at {}", Instant.now());
        log.info("========================================");
    }
}
