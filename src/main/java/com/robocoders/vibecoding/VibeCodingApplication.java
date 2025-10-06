package com.robocoders.vibecoding;

import com.robocoders.vibecoding.config.ShellyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ShellyProperties.class)
public class VibeCodingApplication {

    public static void main(String[] args) {
        SpringApplication.run(VibeCodingApplication.class, args);
    }
}
