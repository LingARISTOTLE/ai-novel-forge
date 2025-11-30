package com.novel.forge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Chapter {
    private Long id;
    private Long novelId; // MyBatis use ID usually
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonIgnore
    private Novel novel; // Optional if you need full object
}
