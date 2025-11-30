package com.novel.forge.controller;

import com.novel.forge.entity.Novel;
import com.novel.forge.repository.NovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/novels")
@CrossOrigin(origins = "*")
public class NovelController {

    @Autowired
    private NovelRepository novelRepository;

    @GetMapping
    public List<Novel> getAllNovels() {
        return novelRepository.findAll();
    }

    @PostMapping
    public Novel createNovel(@RequestBody Novel novel) {
        return novelRepository.save(novel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Novel> getNovelById(@PathVariable Long id) {
        return novelRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Novel> updateNovel(@PathVariable Long id, @RequestBody Novel novelDetails) {
        return novelRepository.findById(id)
                .map(novel -> {
                    novel.setTitle(novelDetails.getTitle());
                    novel.setDescription(novelDetails.getDescription());
                    return ResponseEntity.ok(novelRepository.save(novel));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNovel(@PathVariable Long id) {
        return novelRepository.findById(id)
                .map(novel -> {
                    novelRepository.delete(novel);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
