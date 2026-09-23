package com.vmarcante.time_tracker.core.domain.user.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthSecure {

    String message() default "Unauthorized";

    UserRoleType[] acceptedRoles() default {};

    boolean appKeyAllowed() default false;

    boolean allowPendingOnboarding() default false;

}