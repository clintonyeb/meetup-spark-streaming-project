package com.clinton;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class DI {
    public static final ObjectMapper OBJECT_MAPPER = objectMapper();

    private static ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
