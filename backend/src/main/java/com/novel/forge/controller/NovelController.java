package com.novel.forge.controller;

import com.novel.forge.entity.Novel;
import com.novel.forge.mapper.NovelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/novels")
@CrossOrigin(origins = "*")
public class NovelController {

    private final NovelMapper novelMapper;

    public NovelController(NovelMapper novelMapper) {
        this.novelMapper = novelMapper;
    }

    @GetMapping
    public List<Novel> getAllNovels() {
        return novelMapper.findAll();
    }

    @PostMapping
    public Novel createNovel(@RequestBody Novel novel) {
        novelMapper.insert(novel);
        return novel;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Novel> getNovelById(@PathVariable Long id) {
        Novel novel = novelMapper.findById(id);
        return novel != null ? ResponseEntity.ok(novel) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Novel> updateNovel(@PathVariable Long id, @RequestBody Novel novelDetails) {
        Novel novel = novelMapper.findById(id);
        if (novel == null) {
            return ResponseEntity.notFound().build();
        }
        
        novel.setTitle(novelDetails.getTitle());
        novel.setDescription(novelDetails.getDescription());
        novelMapper.update(novel);
        
        return ResponseEntity.ok(novel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNovel(@PathVariable Long id) {
        Novel novel = novelMapper.findById(id);
        if (novel == null) {
            return ResponseEntity.notFound().build();
        }
        novelMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
