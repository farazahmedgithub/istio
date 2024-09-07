package com.farazahmed.demoserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoServerApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void ping_should_be_successful() {
        var expected = new DemoServerApplication.Message("demo-server: welcome");
        var response = restTemplate.getForEntity("/ping", DemoServerApplication.Message.class);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getStatusCode());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(expected, response.getBody());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 500, 504, 503})
    void error_should_response_with_expected_status_code(Integer statusCode) {
        var response = restTemplate.getForEntity("/status/%s".formatted(statusCode), Void.class);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getStatusCode());
        Assertions.assertEquals(statusCode, response.getStatusCode().value());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1, 2, 3, 10, 100, 399, 1000, 10000, 100000})
    void error_should_give_bad_request_when_status_code_not_supported(Integer statusCode) {
        var response = restTemplate.getForEntity("/status/%s".formatted(statusCode), Void.class);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

}
