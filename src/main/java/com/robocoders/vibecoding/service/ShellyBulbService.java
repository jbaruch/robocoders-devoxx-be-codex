package com.robocoders.vibecoding.service;

import com.robocoders.vibecoding.config.ShellyProperties;
import com.robocoders.vibecoding.domain.ColorCommand;
import com.robocoders.vibecoding.service.exception.BulbDispatchException;
import com.robocoders.vibecoding.service.exception.BulbNotConfiguredException;
import java.net.URI;
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

    private static final int DEFAULT_COMPONENT_ID = 0;
    private static final String DEFAULT_COMMAND_PATH = "/color/" + DEFAULT_COMPONENT_ID;
    private static final int DEFAULT_BRIGHTNESS = 100;
    private static final int DEFAULT_TRANSITION_MS = 500;
    private static final int MIN_TRANSITION_MS = 500;
    private static final int MAX_TRANSITION_MS = 10_800_000;

    private final ShellyProperties properties;
    private final RestClient restClient;

    public ShellyBulbService(RestClient.Builder restClientBuilder, ShellyProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder.requestFactory(requestFactory()).build();
    }

    public void dispatch(ColorCommand command) {
        URI endpoint = buildCommandUri(command);

        try {
            restClient.get()
                .uri(endpoint)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new BulbDispatchException("Shelly bulb responded with status " + ex.getStatusCode(), ex);
        } catch (RestClientException ex) {
            throw new BulbDispatchException("Failed to reach Shelly bulb endpoint", ex);
        }
    }

    private URI buildCommandUri(ColorCommand command) {
        URI baseUri = resolveBaseUri();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUri);
        if (!StringUtils.hasText(baseUri.getPath()) || "/".equals(baseUri.getPath())) {
            builder.replacePath(DEFAULT_COMMAND_PATH);
        }
        builder.replaceQuery(null)
            .queryParam("turn", "on")
            .queryParam("brightness", DEFAULT_BRIGHTNESS)
            .queryParam("transition", clampTransition(command.transitionMs()))
            .queryParam("red", normalizedChannel(command.color().red()))
            .queryParam("green", normalizedChannel(command.color().green()))
            .queryParam("blue", normalizedChannel(command.color().blue()))
            .queryParam("source", command.mode().name())
            .queryParam("source_timestamp", command.sourceTimestamp());

        Integer white = command.color().white();
        if (white != null) {
            builder.queryParam("white", normalizedChannel(white));
        }

        return builder.build(true).toUri();
    }

    private URI resolveBaseUri() {
        String configured = properties.resolvedHost()
            .orElseThrow(BulbNotConfiguredException::new);

        String normalized = configured.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "http://" + normalized;
        }

        return UriComponentsBuilder.fromUriString(normalized).build().toUri();
    }

    private int clampTransition(int transitionMs) {
        int value = transitionMs > 0 ? transitionMs : DEFAULT_TRANSITION_MS;
        return Math.max(MIN_TRANSITION_MS, Math.min(value, MAX_TRANSITION_MS));
    }

    private int normalizedChannel(int channel) {
        return Math.max(0, Math.min(channel, 255));
    }

    private ClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1500);
        factory.setReadTimeout(1500);
        return factory;
    }
}
