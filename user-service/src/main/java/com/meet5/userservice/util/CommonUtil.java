package com.meet5.userservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

public class CommonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(
                    map != null ? map : Collections.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialise extra_fields", e);
        }
    }

    public static Map<String, Object> fromJson(String json) {
        try {
            if (json == null || json.isBlank()) return Collections.emptyMap();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
