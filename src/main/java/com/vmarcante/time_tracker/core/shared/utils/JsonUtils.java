package com.vmarcante.time_tracker.core.shared.utils;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Deserialize JSON string to Map<String, Object>
     */
    public static Map<String, Object> deserializeToMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.error("[JSON Utils] Error deserializing JSON to Map", e);
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

    /**
     * Serialize object to JSON string
     */
    public static String serialize(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("[JSON Utils] Error serializing object to JSON", e);
            throw new RuntimeException("Failed to serialize object", e);
        }
    }
}
