package com.joutio.console.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * URL de l'API Prometheus in-cluster (`joutio.console.prometheus.url`).
 *
 * <p>Vide = fonctionnalités de métriques désactivées (les pages retournent des séries vides) —
 * pratique en local sans stack d'observabilité.</p>
 */
@ConfigurationProperties(prefix = "joutio.console.prometheus")
public record JoutioConsolePrometheusProperties(
        String url
) {

    public static final String DEFAULT_URL =
            "http://kube-prometheus-stack-prometheus.monitoring.svc.cluster.local:9090";

    public JoutioConsolePrometheusProperties {
        if (url == null) {
            url = DEFAULT_URL;
        }
    }

    public boolean enabled() {
        return url != null && !url.isBlank();
    }
}
