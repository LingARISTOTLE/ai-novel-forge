package com.novel.forge.service;

import com.novel.forge.entity.Conversation;
import com.novel.forge.entity.Message;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    
    private final com.novel.forge.mapper.ConversationMapper conversationMapper;
    private final com.novel.forge.mapper.MessageMapper messageMapper;

    public AiService(RestTemplate restTemplate, 
                     com.novel.forge.mapper.ConversationMapper conversationMapper,
                     com.novel.forge.mapper.MessageMapper messageMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    // Blocking chat (Legacy support)
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

    // Streaming chat with Persistence
    public SseEmitter streamChat(AiRequest request) {
        SseEmitter emitter = new SseEmitter(60000L); // 1 minute timeout
        
        executorService.execute(() -> {
            try {
                // 1. Handle Conversation Creation
                Long conversationId = request.getConversationId();
                if (conversationId == null) {
                    Conversation convo = new Conversation();
                    String title = request.getPrompt();
                    if (title != null && title.length() > 20) {
                        title = title.substring(0, 20) + "...";
                    } else if (title == null || title.trim().isEmpty()) {
                        title = "New Conversation";
                    }
                    System.out.println("Saving Conversation Title: " + title);
                    convo.setTitle(title);
                    conversationMapper.insert(convo);
                    conversationId = convo.getId();
                    System.out.println("Created new conversation: " + conversationId);
                    
                    // Send meta event to frontend with new conversation ID and Title
                    // Use ObjectMapper to ensure valid JSON
                    java.util.Map<String, Object> metaMap = new java.util.HashMap<>();
                    metaMap.put("conversationId", conversationId);
                    metaMap.put("title", title);
                    String jsonMeta = objectMapper.writeValueAsString(metaMap);
                    
                    emitter.send(SseEmitter.event().name("meta").data(jsonMeta));
                } else {
                    System.out.println("Continuing conversation: " + conversationId);
                }

                // 2. Save User Message
                Message userMsg = new Message();
                userMsg.setConversationId(conversationId);
                userMsg.setRole("user");
                userMsg.setContent(request.getPrompt());
                messageMapper.insert(userMsg);
                System.out.println("Saved user message for conversation: " + conversationId);

                // 3. Prepare Request with History
                List<Message> history = messageMapper.findByConversationId(conversationId);
                DeepSeekRequest deepSeekRequest = createRequestWithHistory(history, true);
                
                String jsonBody = objectMapper.writeValueAsString(deepSeekRequest);

                // 4. Call API
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

                StringBuilder aiResponseBuilder = new StringBuilder();

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
                                            aiResponseBuilder.append(content);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                    }
                    
                    // 5. Save AI Message
                    if (aiResponseBuilder.length() > 0) {
                        Message aiMsg = new Message();
                        aiMsg.setConversationId(conversationId);
                        aiMsg.setRole("assistant");
                        aiMsg.setContent(aiResponseBuilder.toString());
                        messageMapper.insert(aiMsg);
                        System.out.println("Saved AI response for conversation: " + conversationId + ", length: " + aiResponseBuilder.length());
                        
                        // Update conversation timestamp
                        Conversation convo = new Conversation();
                        convo.setId(conversationId);
                        conversationMapper.update(convo); 
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

    private DeepSeekRequest createRequestWithHistory(List<Message> history, boolean stream) {
        List<DeepSeekRequest.Message> messages = new ArrayList<>();
        
        // System prompt (optional)
        // messages.add(DeepSeekRequest.Message.builder().role("system").content("You are a helpful assistant.").build());

        for (Message msg : history) {
            messages.add(DeepSeekRequest.Message.builder()
                    .role(msg.getRole())
                    .content(msg.getContent())
                    .build());
        }

        return DeepSeekRequest.builder()
                .model(model)
                .stream(stream)
                .messages(messages)
                .build();
    }
}
