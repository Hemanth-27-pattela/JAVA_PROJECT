package com.hotel.desktop.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUtilsTest {

    @Test
    @DisplayName("ObjectMapper is properly initialized and handles Java 8 time types")
    void testObjectMapperInitialization() throws Exception {
        assertNotNull(JsonUtils.getMapper());

        Instant now = Instant.now();
        String json = JsonUtils.getMapper().writeValueAsString(now);
        assertNotNull(json);
        assertTrue(json.contains("T"));
    }
}

