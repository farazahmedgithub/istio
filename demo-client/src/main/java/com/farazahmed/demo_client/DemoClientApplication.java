package com.farazahmed.demo_client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class DemoClientApplication {
    private final String applicationName;
    private final RestTemplate restTemplate;
    private final String externalEndpoint;

    public DemoClientApplication(@Value("${spring.application.name}") String applicationName,
                                 @Value("${external.endpoint}") String externalEndpoint,
                                 RestTemplate restTemplate) {
        this.applicationName = applicationName;
        this.restTemplate = restTemplate;
        this.externalEndpoint = externalEndpoint;
    }

    @GetMapping("ping")
    public Message ping() {
        log("ping api called");
        return new Message("%s: welcome".formatted(applicationName));
    }

    @GetMapping("external/status/{statusCode}")
    public ResponseEntity<Void> status(@PathVariable Integer statusCode) {
        log("external status api called with parameter status code: " + statusCode);
        return restTemplate.getForEntity(externalEndpoint + "/status/" + statusCode, Void.class);
    }

    private void log(String message) {
        System.out.printf("[%s %s] %s.%n", applicationName, Thread.currentThread().getId(), message);
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoClientApplication.class, args);
    }

    public record Message(String message) {
    }
}

@Configuration
class Config {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }
}