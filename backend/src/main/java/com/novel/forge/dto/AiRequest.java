package com.novel.forge.dto;

import lombok.Data;

@Data
public class AiRequest {
    private String prompt;
    private String context; // Optional context
    private Long conversationId; // Optional, if null, creates new conversation? Or maybe handled by frontend
}
