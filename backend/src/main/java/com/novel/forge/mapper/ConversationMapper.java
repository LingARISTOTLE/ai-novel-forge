package com.novel.forge.mapper;

import com.novel.forge.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConversationMapper {
    List<Conversation> findAll();
    Conversation findById(Long id);
    void insert(Conversation conversation);
    void update(Conversation conversation);
    void deleteById(Long id);
}
