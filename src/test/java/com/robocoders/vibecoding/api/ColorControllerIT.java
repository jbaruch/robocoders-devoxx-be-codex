package com.robocoders.vibecoding.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Covers SC-BE-1 (happy path), SC-BE-3 (bulb offline), and SC-CC-1 latency budget from docs/test_scenarios.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ColorControllerIT {

    private static final MockWebServer mockShellyServer;

    static {
        try {
            mockShellyServer = new MockWebServer();
            mockShellyServer.start();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start mock Shelly server", e);
        }
    }

    @DynamicPropertySource
    static void configureShellyHost(DynamicPropertyRegistry registry) {
        registry.add("shelly.bulb.host", () -> mockShellyServer.url("/").toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterAll
    static void shutdownServer() throws IOException {
        mockShellyServer.shutdown();
    }

    @Test
    @DisplayName("BE-1: POST /api/colors returns 204 and dispatches to Shelly under 2 seconds")
    void postColorCommandDispatchesWithinLatencyBudget() throws Exception {
        mockShellyServer.enqueue(new MockResponse().setResponseCode(200));
        String payload = objectMapper.writeValueAsString(new ColorRequest("MANUAL", new Rgbw(12, 34, 56, null), null, "2025-02-01T10:00:00Z"));

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> mockMvc.perform(post("/api/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isNoContent()));

        RecordedRequest recordedRequest = mockShellyServer.takeRequest();
        assertThat(recordedRequest).as("Shelly API should receive a command").isNotNull();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        HttpUrl requestUrl = recordedRequest.getRequestUrl();
        assertThat(requestUrl).isNotNull();
        assertThat(requestUrl.encodedPath()).isEqualTo("/color/0");
        assertThat(requestUrl.queryParameter("red")).isEqualTo("12");
        assertThat(requestUrl.queryParameter("green")).isEqualTo("34");
        assertThat(requestUrl.queryParameter("blue")).isEqualTo("56");
        assertThat(requestUrl.queryParameter("white")).isNull();
        assertThat(requestUrl.queryParameter("transition")).isEqualTo("500");
        assertThat(requestUrl.queryParameter("source")).isEqualTo("MANUAL");
        assertThat(requestUrl.queryParameter("source_timestamp")).isEqualTo("2025-02-01T10:00:00Z");
    }

    @Test
    @DisplayName("BE-3: Downstream Shelly failure surfaces as 424 with BULB_OFFLINE payload")
    void downstreamFailureSurfacedAsFailedDependency() throws Exception {
        mockShellyServer.enqueue(new MockResponse().setResponseCode(503));
        String payload = objectMapper.writeValueAsString(new ColorRequest("AUTO", new Rgbw(200, 180, 160, 22), 700, "2025-02-01T10:00:05Z"));

        mockMvc.perform(post("/api/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isFailedDependency())
            .andExpect(content().json("{\"error\":\"BULB_OFFLINE\"}", true));

        RecordedRequest recordedRequest = mockShellyServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        HttpUrl requestUrl = recordedRequest.getRequestUrl();
        assertThat(requestUrl.queryParameter("white")).isEqualTo("22");
        assertThat(requestUrl.queryParameter("transition")).isEqualTo("700");
        assertThat(requestUrl.queryParameter("source")).isEqualTo("AUTO");
    }

    private record ColorRequest(String mode, Rgbw color, Integer transitionMs, String sourceTimestamp) {}

    private record Rgbw(int r, int g, int b, Integer w) {}
}
