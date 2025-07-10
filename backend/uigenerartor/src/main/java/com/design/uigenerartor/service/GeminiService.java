package com.design.uigenerartor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;

    public GeminiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> generateCss(String designPrompt) {
        // Craft a precise prompt for the AI to ensure it outputs only CSS
        String fullPrompt = "You are an expert web developer.\n" +
                "Given a design prompt, generate clean, minimal HTML and CSS.\n" +
                "Return the result using these exact tags:\n\n" +
                "<html>\n" +
                "[HTML content here]\n" +
                "</html>\n\n" +
                "<css>\n" +
                "[CSS content here]\n" +
                "</css>\n\n" +
                "Do NOT include markdown, triple backticks, or explanations.\n" +
                "ONLY return valid HTML inside <html> and CSS inside <css>.\n\n" +
                "Design description: " + designPrompt;

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", fullPrompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(textPart)); // Use textPart here

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(content));

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", geminiApiKey).build())
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response != null && response.containsKey("candidates")) {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (!candidates.isEmpty()) {
                            Map<String, Object> candidate = candidates.get(0);
                            if (candidate.containsKey("content")) {
                                Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                                if (contentMap.containsKey("parts")) {
                                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                                    if (!parts.isEmpty()) {
                                        Map<String, Object> partMap = parts.get(0);
                                        if (partMap.containsKey("text")) {
                                            return (String) partMap.get("text");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return "Error: Could not parse Gemini response or no content generated.";
                })
                .doOnError(throwable -> System.err.println("Error calling Gemini API: " + throwable.getMessage()));
    }
}
