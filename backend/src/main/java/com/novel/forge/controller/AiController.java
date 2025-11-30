package com.novel.forge.controller;

import com.novel.forge.dto.AiRequest;
import com.novel.forge.service.AiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") // Allow frontend to access
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody AiRequest request) {
        return aiService.chat(request);
    }
}
