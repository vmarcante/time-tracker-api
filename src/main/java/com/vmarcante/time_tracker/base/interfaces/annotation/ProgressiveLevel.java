package com.vmarcante.time_tracker.base.interfaces.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ProgressiveLevel {
    int maxRequests() default 5;
    int windowSeconds() default 120;
    int banSeconds() default 0;
}
