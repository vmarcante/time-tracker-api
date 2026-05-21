package com.vmarcante.time_tracker.core.shared.utils;

public final class UserAgentUtils {

    private UserAgentUtils() {
    }

    public static String extractDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        String lowerUserAgent = userAgent.toLowerCase();

        if (lowerUserAgent.contains("mobile")) {
            return "Mobile";
        }

        if (lowerUserAgent.contains("tablet") || lowerUserAgent.contains("ipad")) {
            return "Tablet";
        }

        if (lowerUserAgent.contains("bot") || lowerUserAgent.contains("crawler") || lowerUserAgent.contains("spider")) {
            return "Bot";
        }

        return "Web";
    }

    public static String extractBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        String lowerUserAgent = userAgent.toLowerCase();

        if (lowerUserAgent.contains("edg")) {
            return "Edge";
        }
        if (lowerUserAgent.contains("chrome")) {
            return "Chrome";
        }
        if (lowerUserAgent.contains("firefox")) {
            return "Firefox";
        }
        if (lowerUserAgent.contains("safari")) {
            return "Safari";
        }
        if (lowerUserAgent.contains("opera") || lowerUserAgent.contains("opr")) {
            return "Opera";
        }

        return "Other";
    }

    public static String extractOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        String lowerUserAgent = userAgent.toLowerCase();

        if (lowerUserAgent.contains("windows")) {
            return "Windows";
        }
        if (lowerUserAgent.contains("mac")) {
            return "macOS";
        }
        if (lowerUserAgent.contains("linux")) {
            return "Linux";
        }
        if (lowerUserAgent.contains("android")) {
            return "Android";
        }
        if (lowerUserAgent.contains("iphone") || lowerUserAgent.contains("ipad")) {
            return "iOS";
        }

        return "Other";
    }
}
