package com.novel.forge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel.forge.dto.AiRequest;
import com.novel.forge.dto.DeepSeekRequest;
import com.novel.forge.dto.DeepSeekResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AiService {

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // Blocking chat
    public String chat(AiRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        DeepSeekRequest deepSeekRequest = createRequest(request, false);
        HttpEntity<DeepSeekRequest> entity = new HttpEntity<>(deepSeekRequest, headers);

        try {
            DeepSeekResponse response = restTemplate.postForObject(apiUrl, entity, DeepSeekResponse.class);
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling DeepSeek API: " + e.getMessage();
        }

        return "No response from AI";
    }

    // Streaming chat
    public SseEmitter streamChat(AiRequest request) {
        SseEmitter emitter = new SseEmitter(60000L); // 1 minute timeout
        
        executorService.execute(() -> {
            try {
                DeepSeekRequest deepSeekRequest = createRequest(request, true);
                String jsonBody = objectMapper.writeValueAsString(deepSeekRequest);

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                        String line;
                        StringBuilder error = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            error.append(line);
                        }
                        System.err.println("API Error: " + error.toString());
                        emitter.completeWithError(new RuntimeException("API Error: " + responseCode));
                        return;
                    }
                }

                try (InputStream is = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data.trim())) {
                                break;
                            }
                            
                            try {
                                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(data);
                                if (node.has("choices") && node.get("choices").isArray() && node.get("choices").size() > 0) {
                                    com.fasterxml.jackson.databind.JsonNode choice = node.get("choices").get(0);
                                    if (choice.has("delta") && choice.get("delta").has("content")) {
                                        String content = choice.get("delta").get("content").asText();
                                        if (content != null && !content.isEmpty()) {
                                            emitter.send(content);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore parse errors for keep-alive lines etc
                            }
                        }
                    }
                    emitter.complete();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
                e.printStackTrace();
            }
        });

        return emitter;
    }

    private DeepSeekRequest createRequest(AiRequest request, boolean stream) {
        return DeepSeekRequest.builder()
                .model(model)
                .stream(stream)
                .messages(Collections.singletonList(
                        DeepSeekRequest.Message.builder()
                                .role("user")
                                .content(request.getPrompt())
                                .build()
                ))
                .build();
    }
}
