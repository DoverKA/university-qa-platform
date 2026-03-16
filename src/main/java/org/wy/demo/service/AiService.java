package org.wy.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.model}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate an AI answer for a university Q&A question.
     *
     * @param questionTitle   the question's title
     * @param questionContent the question's full content
     * @param courseName      the course this question belongs to
     * @return AI-generated answer text, or an error message if the call fails
     */
    public String generateAnswer(String questionTitle, String questionContent, String courseName) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your_anthropic_api_key_here")) {
            return "AI answer feature is not configured. Please set ai.api.key in application.properties.";
        }

        try {
            // Build request body
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", 1024);

            ArrayNode messages = body.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");

            String prompt = String.format(
                "You are a helpful teaching assistant for a university course called \"%s\".\n\n" +
                "A student has asked the following question:\n\n" +
                "Title: %s\n\n" +
                "Details: %s\n\n" +
                "Please provide a clear, educational, and helpful answer suitable for a university student. " +
                "Be concise but thorough. If relevant, mention any key concepts or further reading.",
                courseName, questionTitle, questionContent
            );
            message.put("content", prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(response.body());
                return responseJson
                        .path("content")
                        .get(0)
                        .path("text")
                        .asText("AI did not return a response.");
            } else {
                log.error("AI API error: status={}, body={}", response.statusCode(), response.body());
                return "AI service temporarily unavailable. Please try again later.";
            }

        } catch (Exception e) {
            log.error("Error calling AI API", e);
            return "Failed to generate AI answer: " + e.getMessage();
        }
    }
}
