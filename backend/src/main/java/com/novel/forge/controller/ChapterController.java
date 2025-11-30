package com.novel.forge.controller;

import com.novel.forge.entity.Chapter;
import com.novel.forge.mapper.ChapterMapper;
import com.novel.forge.mapper.NovelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChapterController {

    private final ChapterMapper chapterMapper;
    private final NovelMapper novelMapper;

    public ChapterController(ChapterMapper chapterMapper, NovelMapper novelMapper) {
        this.chapterMapper = chapterMapper;
        this.novelMapper = novelMapper;
    }

    @GetMapping("/novels/{novelId}/chapters")
    public List<Chapter> getChaptersByNovelId(@PathVariable Long novelId) {
        return chapterMapper.findByNovelId(novelId);
    }

    @PostMapping("/novels/{novelId}/chapters")
    public ResponseEntity<Chapter> createChapter(@PathVariable Long novelId, @RequestBody Chapter chapter) {
        if (novelMapper.findById(novelId) == null) {
            return ResponseEntity.notFound().build();
        }
        chapter.setNovelId(novelId);
        chapterMapper.insert(chapter);
        return ResponseEntity.ok(chapter);
    }

    @GetMapping("/chapters/{id}")
    public ResponseEntity<Chapter> getChapterById(@PathVariable Long id) {
        Chapter chapter = chapterMapper.findById(id);
        return chapter != null ? ResponseEntity.ok(chapter) : ResponseEntity.notFound().build();
    }

    @PutMapping("/chapters/{id}")
    public ResponseEntity<Chapter> updateChapter(@PathVariable Long id, @RequestBody Chapter chapterDetails) {
        Chapter chapter = chapterMapper.findById(id);
        if (chapter == null) {
            return ResponseEntity.notFound().build();
        }
        
        chapter.setTitle(chapterDetails.getTitle());
        chapter.setContent(chapterDetails.getContent());
        chapterMapper.update(chapter);
        
        return ResponseEntity.ok(chapter);
    }

    @DeleteMapping("/chapters/{id}")
    public ResponseEntity<?> deleteChapter(@PathVariable Long id) {
        Chapter chapter = chapterMapper.findById(id);
        if (chapter == null) {
            return ResponseEntity.notFound().build();
        }
        chapterMapper.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
