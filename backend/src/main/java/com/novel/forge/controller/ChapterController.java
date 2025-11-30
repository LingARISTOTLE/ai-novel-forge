package com.novel.forge.controller;

import com.novel.forge.entity.Chapter;
import com.novel.forge.repository.ChapterRepository;
import com.novel.forge.repository.NovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChapterController {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private NovelRepository novelRepository;

    @GetMapping("/novels/{novelId}/chapters")
    public List<Chapter> getChaptersByNovelId(@PathVariable Long novelId) {
        return chapterRepository.findByNovelId(novelId);
    }

    @PostMapping("/novels/{novelId}/chapters")
    public ResponseEntity<Chapter> createChapter(@PathVariable Long novelId, @RequestBody Chapter chapter) {
        return novelRepository.findById(novelId)
                .map(novel -> {
                    chapter.setNovel(novel);
                    return ResponseEntity.ok(chapterRepository.save(chapter));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/chapters/{id}")
    public ResponseEntity<Chapter> getChapterById(@PathVariable Long id) {
        return chapterRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/chapters/{id}")
    public ResponseEntity<Chapter> updateChapter(@PathVariable Long id, @RequestBody Chapter chapterDetails) {
        return chapterRepository.findById(id)
                .map(chapter -> {
                    chapter.setTitle(chapterDetails.getTitle());
                    chapter.setContent(chapterDetails.getContent());
                    return ResponseEntity.ok(chapterRepository.save(chapter));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/chapters/{id}")
    public ResponseEntity<?> deleteChapter(@PathVariable Long id) {
        return chapterRepository.findById(id)
                .map(chapter -> {
                    chapterRepository.delete(chapter);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
