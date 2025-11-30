package com.novel.forge.dto;

import lombok.Data;

@Data
public class AiRequest {
    private String prompt;
    private String context; // Optional context like current chapter content
}
