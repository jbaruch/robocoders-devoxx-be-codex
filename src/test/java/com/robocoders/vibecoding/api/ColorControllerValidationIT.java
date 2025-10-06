package com.robocoders.vibecoding.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Verifies SC-BE-2 validation scenarios for POST /api/colors. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "shelly.bulb.host=http://127.0.0.1")
class ColorControllerValidationIT {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class OutOfRangeChannel {
        @Test
        @DisplayName("BE-2: Reject RGB component above 255 with 400 error")
        void rejectsChannelAboveRange() throws Exception {
            mockMvc.perform(post("/api/colors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{" +
                        "\"mode\":\"MANUAL\"," +
                        "\"color\":{\"r\":300,\"g\":0,\"b\":0}," +
                        "\"sourceTimestamp\":\"2025-02-01T10:00:00Z\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
        }
    }

    @Nested
    class MissingFields {
        @Test
        @DisplayName("BE-2: Missing color payload yields 400 with validation marker")
        void rejectsMissingColor() throws Exception {
            mockMvc.perform(post("/api/colors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{" +
                        "\"mode\":\"MANUAL\"," +
                        "\"sourceTimestamp\":\"2025-02-01T10:00:00Z\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
        }
    }
}
