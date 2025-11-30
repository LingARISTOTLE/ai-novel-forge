package com.novel.forge.mapper;

import com.novel.forge.entity.Chapter;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChapterMapper {
    List<Chapter> findByNovelId(Long novelId);
    Chapter findById(Long id);
    void insert(Chapter chapter);
    void update(Chapter chapter);
    void deleteById(Long id);
}
