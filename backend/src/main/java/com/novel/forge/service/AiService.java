package com.novel.forge.service;

import com.novel.forge.dto.AiRequest;
import com.novel.forge.dto.DeepSeekRequest;
import com.novel.forge.dto.DeepSeekResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class AiService {

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.model}")
    private String model;

    private final RestTemplate restTemplate;

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String chat(AiRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        DeepSeekRequest deepSeekRequest = DeepSeekRequest.builder()
                .model(model)
                .stream(false)
                .messages(Collections.singletonList(
                        DeepSeekRequest.Message.builder()
                                .role("user")
                                .content(request.getPrompt())
                                .build()
                ))
                .build();

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
}
