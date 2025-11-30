package com.novel.forge.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeepSeekResponse {
    private String id;
    private List<Choice> choices;

    @Data
    public static class Choice {
        private Message message;
        private String finish_reason;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
