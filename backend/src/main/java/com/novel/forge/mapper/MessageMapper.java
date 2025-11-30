package com.novel.forge.mapper;

import com.novel.forge.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    List<Message> findByConversationId(Long conversationId);
    void insert(Message message);
    void deleteByConversationId(Long conversationId);
}
