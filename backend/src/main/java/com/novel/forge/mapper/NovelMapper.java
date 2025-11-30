package com.novel.forge.mapper;

import com.novel.forge.entity.Novel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NovelMapper {
    List<Novel> findAll();
    Novel findById(Long id);
    void insert(Novel novel);
    void update(Novel novel);
    void deleteById(Long id);
}
