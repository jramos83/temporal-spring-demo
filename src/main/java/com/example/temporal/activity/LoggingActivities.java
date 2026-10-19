package com.example.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface LoggingActivities {

    @ActivityMethod
    void activityA();

    @ActivityMethod
    void activityB();
}
