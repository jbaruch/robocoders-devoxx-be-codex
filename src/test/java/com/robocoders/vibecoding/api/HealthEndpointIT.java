package com.robocoders.vibecoding.api;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Implements SC-BE-5 for GET /api/health contract. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "shelly.bulb.host=http://127.0.0.1")
class HealthEndpointIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("BE-5: Health endpoint reports version, timestamp, and bulbConfigured")
    void returnsHealthEnvelope() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.appVersion", notNullValue()))
            .andExpect(jsonPath("$.timestamp", notNullValue()))
            .andExpect(jsonPath("$.bulbConfigured", is(true)));
    }
}
