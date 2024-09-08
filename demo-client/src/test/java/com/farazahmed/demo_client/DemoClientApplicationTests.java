package com.farazahmed.demo_client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.GracefulShutdownCallback;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoClientApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;
    private static final MockWebServer mockWebServer = new MockWebServer();

    static {
        mockWebServer.start();
    }

    @AfterAll
    static void destroy() {
        mockWebServer.stop();
    }

    @Test
    void ping_should_be_successful() {
        var expected = new DemoClientApplication.Message("demo-client: welcome");
        var response = restTemplate.getForEntity("/ping", DemoClientApplication.Message.class);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getStatusCode());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(expected, response.getBody());
    }

    @ParameterizedTest
    @ValueSource(ints = {200})
    void error_should_response_with_expected_status_code(Integer statusCode) {
        var response = restTemplate.getForEntity("/external/status/%s".formatted(statusCode), Void.class);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getStatusCode());
        Assertions.assertEquals(statusCode, response.getStatusCode().value());
    }

}

//TODO: need fixes and improvements
class MockWebServer implements WebServer {
    private final ServerSocket socket;

    public MockWebServer() {
        try {
            socket = new ServerSocket(8081);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws WebServerException {
        try {
            Runnable task = () -> {
                while (true)
                    try {
                        Socket connection = socket.accept();
                        new Thread(() -> new RequestHandler(connection))
                                .start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
            };

            new Thread(task, "test-mock-server-listener")
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() throws WebServerException {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPort() {
        return socket.getLocalPort();
    }

    @Override
    public void shutDownGracefully(GracefulShutdownCallback callback) {
        WebServer.super.shutDownGracefully(callback);
    }

    @Override
    public void destroy() {
        WebServer.super.destroy();
    }
}

class RequestHandler {
    private static final String OUTPUT_HEADERS = "HTTP/1.1 %s" +
            "Connection: close\r\n" +
            "Content-Length: ";
    private static final String OUTPUT_END_OF_HEADERS = "\r\n\r\n";

    private final Socket connection;

    public RequestHandler(Socket connection) {
        this.connection = connection;
        try {
            var request = readRequest();
            sendResponse(getResponseStatus(request));
        } finally {
            try {
                if (connection.getInputStream() != null || connection.getOutputStream() != null) {
                    connection.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }


    private HttpRequest readRequest() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            if ((inputLine = in.readLine()).isEmpty()) {
                return new HttpRequest(null, null);
            }

            var tmp = inputLine.split(" ");
            System.out.println(inputLine);
            return new HttpRequest(HttpMethod.valueOf(tmp[0]), tmp[1]);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void sendResponse(String status) {
        String response = """
                {
                "message": "hi"
                }
                """;
        response = "";

        response = OUTPUT_HEADERS.formatted(status) + response.length() + OUTPUT_END_OF_HEADERS + response;

        System.out.println("created response:");
        System.out.println(response);

        try {
            var os = connection.getOutputStream();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    record HttpRequest(HttpMethod method, String path) {
    }

    private String getResponseStatus(HttpRequest request) {
        String path = request.path();
        String error = "404 NOT_FOUND\r\n";
        if (path.equals("/status/") || !path.startsWith("/status/")) {
            return error;
        }
        String statusCode = path.replaceFirst("/status/", "");

        try {
            Integer number = Integer.parseInt(statusCode);

            if (number == 200) {
                return "200 OK\r\nContent-Type: application/json\r\n";
            }
            return error;
        } catch (NumberFormatException e) {
            return error;
        }
    }
}
