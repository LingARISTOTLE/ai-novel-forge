package com.novel.forge.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Conversation {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Optional: messages list
    private List<Message> messages;
}
