package com.vmarcante.time_tracker.core.shared.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static String getStackTraceAsString(Exception exception) {
        if (exception == null) {
            return "";
        }

        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Error generating stack trace: " + e.getMessage();
        }
    }

    public static String getStackTraceAsString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Error generating stack trace: " + e.getMessage();
        }
    }

    public static String getRootCauseMessage(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        return rootCause.getMessage() != null ? rootCause.getMessage() : rootCause.getClass().getSimpleName();
    }
}
