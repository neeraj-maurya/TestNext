package com.testnext.execution;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;

/**
 * Example executor that performs HTTP requests for 'HTTP Request' step definitions.
 * In production use a robust HTTP client and proper timeout/retry handling.
 */
public class DefaultHttpStepExecutor implements StepExecutor {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Override
    public StepResult execute(String stepDefinitionId, Map<String, Object> parameters) throws Exception {
        String url = (String) parameters.get("url");
        if (url == null) return new StepResult(false, null, "url parameter required");

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(30)).header("Accept", "application/json").GET().build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        Map<String,Object> output = new HashMap<>();
        output.put("status", resp.statusCode());
        output.put("body", resp.body());
        return new StepResult(resp.statusCode() >=200 && resp.statusCode() < 300, output, resp.statusCode() >=200 && resp.statusCode() < 300 ? null : "HTTP error");
    }
}
