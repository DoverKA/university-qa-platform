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

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.api.url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    @Value("${ai.system-prompt:你是一名耐心、准确、适合高校教学场景的助教。请用简洁、清晰、结构化的中文回答，优先帮助学生理解概念、定位问题并给出下一步建议。}")
    private String systemPrompt;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateAnswer(String questionTitle, String questionContent, String courseName) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your_deepseek_api_key_here")) {
            return "AI answer feature is not configured. Please set ai.api.key.";
        }

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", 1024);

            ArrayNode messages = body.putArray("messages");

            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", buildPrompt(questionTitle, questionContent, courseName));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode responseJson = objectMapper.readTree(response.body());
                JsonNode message = responseJson.path("choices").path(0).path("message");
                String content = message.path("content").asText("");
                if (content != null && !content.isBlank()) {
                    return content.trim();
                }
                return "AI did not return a response.";
            }

            log.error("DeepSeek API error: status={}, body={}", response.statusCode(), response.body());
            return "AI service temporarily unavailable. Please try again later.";
        } catch (Exception e) {
            log.error("Error calling DeepSeek API", e);
            return "Failed to generate AI answer: " + e.getMessage();
        }
    }

    private String buildPrompt(String questionTitle, String questionContent, String courseName) {
        return String.format(
                "课程：%s%n%n" +
                        "学生问题标题：%s%n%n" +
                        "学生问题详情：%s%n%n" +
                        "请完成以下任务：%n" +
                        "1. 先用通俗语言解释问题核心。%n" +
                        "2. 给出清晰、可操作的解答步骤。%n" +
                        "3. 如果有常见误区，请单独提醒。%n" +
                        "4. 如果信息不足，请明确指出还需要哪些补充信息。",
                courseName,
                questionTitle,
                questionContent
        );
    }
}
