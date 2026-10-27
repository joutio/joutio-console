package com.joutio.console;

import com.joutio.console.infrastructure.config.ConsoleServicesProperties;
import com.joutio.console.infrastructure.config.JoutioConsolePrometheusProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ConsoleServicesProperties.class, JoutioConsolePrometheusProperties.class})
public class ConsoleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsoleServiceApplication.class, args);
    }
}
