package com.ticket.classifier.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
public class GeminiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String classify(String title, String description) {
        String prompt = """
                You are a support ticket classifier. Classify the following ticket and respond ONLY in JSON format with no explanation or markdown.
                
                Ticket Title: %s
                Ticket Description: %s
                
                Respond with exactly this JSON structure:
                {
                  "category": "BILLING or TECHNICAL or ACCOUNT or REFUND",
                  "priority": "LOW or MEDIUM or HIGH or URGENT",
                  "assignedTeam": "BILLING or TECHNICAL or ACCOUNT or REFUND",
                  "suggestedResponse": "one line response",
                  "confidenceScore": 85,
                  "flaggedForReview": false
                }
                """.formatted(title, description);

        try {
            String requestBody = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("model", model);
                    put("messages", new Object[]{
                        new java.util.HashMap<>() {{
                            put("role", "user");
                            put("content", prompt);
                        }}
                    });
                    put("temperature", 0.1);
                    put("max_tokens", 300);
                }}
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
            );

            String responseBody = response.body();
            log.info("=== GROQ RAW RESPONSE ===\n{}\n===========================", responseBody);

            JsonNode root = objectMapper.readTree(responseBody);

            if (root.has("error")) {
                throw new RuntimeException("Groq API error: " +
                    root.path("error").path("message").asText());
            }

            return root.path("choices").get(0)
                .path("message").path("content").asText();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Groq API call failed: " + e.getMessage(), e);
        }
    }
}