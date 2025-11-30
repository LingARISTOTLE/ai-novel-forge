package com.novel.forge.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Long id;
    private Long conversationId;
    private String role; // "user", "assistant"
    private String content;
    private LocalDateTime createdAt;
}
