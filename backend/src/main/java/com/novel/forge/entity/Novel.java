package com.novel.forge.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Novel {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<Chapter> chapters;
}
