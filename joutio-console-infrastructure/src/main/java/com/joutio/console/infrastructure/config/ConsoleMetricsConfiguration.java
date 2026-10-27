package com.joutio.console.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joutio.console.infrastructure.out.prometheus.PrometheusClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Client Prometheus dédié (sans OAuth2 : l'API Prometheus est interne au cluster et anonyme).
 */
@Configuration
public class ConsoleMetricsConfiguration {

    @Bean
    public PrometheusClient prometheusClient(RestTemplateBuilder builder,
                                             JoutioConsolePrometheusProperties properties,
                                             ObjectMapper objectMapper) {
        RestTemplate restTemplate = builder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(15))
                .build();
        return new PrometheusClient(restTemplate, properties, objectMapper);
    }
}
