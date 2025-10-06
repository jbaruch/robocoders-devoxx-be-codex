package com.robocoders.vibecoding.service;

import com.robocoders.vibecoding.config.ShellyProperties;
import com.robocoders.vibecoding.domain.ColorCommand;
import com.robocoders.vibecoding.domain.RgbwColor;
import com.robocoders.vibecoding.service.exception.BulbDispatchException;
import com.robocoders.vibecoding.service.exception.BulbNotConfiguredException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ShellyBulbService {

    private static final String DEFAULT_COMMAND_PATH = "/rpc/Light/0/Set";
    private static final int DEFAULT_COMPONENT_ID = 0;
    private static final int DEFAULT_BRIGHTNESS = 100;

    private final ShellyProperties properties;
    private final RestClient restClient;

    public ShellyBulbService(RestClient.Builder restClientBuilder, ShellyProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder.requestFactory(requestFactory()).build();
    }

    public void dispatch(ColorCommand command) {
        URI endpoint = resolveEndpoint();
        Map<String, Object> payload = buildPayload(command);

        try {
            restClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new BulbDispatchException("Shelly bulb responded with status " + ex.getStatusCode(), ex);
        } catch (RestClientException ex) {
            throw new BulbDispatchException("Failed to reach Shelly bulb endpoint", ex);
        }
    }

    private URI resolveEndpoint() {
        String configured = properties.resolvedHost()
            .orElseThrow(BulbNotConfiguredException::new);

        String normalized = configured.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "http://" + normalized;
        }

        URI baseUri = UriComponentsBuilder.fromUriString(normalized).build().toUri();
        if (!StringUtils.hasText(baseUri.getPath()) || "/".equals(baseUri.getPath())) {
            return UriComponentsBuilder.fromUri(baseUri)
                .replacePath(DEFAULT_COMMAND_PATH)
                .build(true)
                .toUri();
        }
        return baseUri;
    }

    private Map<String, Object> buildPayload(ColorCommand command) {
        RgbwColor color = command.color();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", DEFAULT_COMPONENT_ID);
        body.put("on", true);
        body.put("mode", "color");
        body.put("red", color.red());
        body.put("green", color.green());
        body.put("blue", color.blue());
        if (color.white() != null) {
            body.put("white", color.white());
        }
        body.put("brightness", DEFAULT_BRIGHTNESS);
        body.put("transition_time", command.transitionMs());
        body.put("source", command.mode().name());
        body.put("source_timestamp", command.sourceTimestamp().toString());
        return body;
    }

    private ClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1500);
        factory.setReadTimeout(1500);
        return factory;
    }
}
