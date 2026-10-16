package com.vmarcante.time_tracker.base.interfaces.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    int maxRequests() default 10;

    int windowSeconds() default 60;

    String key() default "";

    boolean progressive() default false;

    ProgressiveLevel[] progressiveLevels() default {
        @ProgressiveLevel(maxRequests = 5, windowSeconds = 120),
        @ProgressiveLevel(maxRequests = 10, windowSeconds = 600),
        @ProgressiveLevel(maxRequests = 15, windowSeconds = 1800, banSeconds = 3600)
    };

}
