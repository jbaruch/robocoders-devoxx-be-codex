package com.robocoders.vibecoding.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Covers SC-BE-4: Unconfigured bulb host should surface as 503. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "shelly.bulb.host=")
class ColorControllerMissingHostIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("BE-4: Missing Shelly host returns 503 with bulbConfigured=false")
    void missingConfigurationReturnsServiceUnavailable() throws Exception {
        mockMvc.perform(post("/api/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"mode\":\"MANUAL\"," +
                    "\"color\":{\"r\":10,\"g\":20,\"b\":30}," +
                    "\"sourceTimestamp\":\"2025-02-01T10:00:00Z\"}"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.error").value("BULB_NOT_CONFIGURED"));
    }
}
