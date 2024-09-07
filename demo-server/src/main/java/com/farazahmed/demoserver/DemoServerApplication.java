package com.farazahmed.demoserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoServerApplication {
    private final String applicationName;

    public DemoServerApplication(@Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("ping")
    public Message ping() {
        log("ping api called");
        return new Message("%s: welcome".formatted(applicationName));
    }

    @GetMapping("status/{statusCode}")
    public ResponseEntity<?> status(@PathVariable Integer statusCode) {
        log("error api called with parameter status code: " + statusCode);

        if (statusCode <= 399 || statusCode > 999) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        return ResponseEntity
                .status(HttpStatusCode.valueOf(statusCode))
                .build();
    }

    private void log(String message) {
        System.out.printf("[%s %s] %s.%n", applicationName, Thread.currentThread().getId(), message);
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoServerApplication.class, args);
    }

    public record Message(String message) {
    }
}