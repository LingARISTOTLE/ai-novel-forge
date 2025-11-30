package com.novel.forge.controller;

import com.novel.forge.entity.Conversation;
import com.novel.forge.entity.Message;
import com.novel.forge.mapper.ConversationMapper;
import com.novel.forge.mapper.MessageMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*")
public class ConversationController {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    public ConversationController(ConversationMapper conversationMapper, MessageMapper messageMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    @GetMapping
    public List<Conversation> getAllConversations() {
        return conversationMapper.findAll();
    }

    @GetMapping("/{id}/messages")
    public List<Message> getMessages(@PathVariable Long id) {
        return messageMapper.findByConversationId(id);
    }

    @PostMapping
    public Conversation createConversation(@RequestBody Conversation conversation) {
        if (conversation.getTitle() == null || conversation.getTitle().isEmpty()) {
            conversation.setTitle("New Conversation");
        }
        conversationMapper.insert(conversation);
        return conversation;
    }

    @PutMapping("/{id}")
    public Conversation updateConversation(@PathVariable Long id, @RequestBody Conversation conversationDetails) {
        Conversation conversation = conversationMapper.findById(id);
        if (conversation != null) {
            if (conversationDetails.getTitle() != null) {
                conversation.setTitle(conversationDetails.getTitle());
            }
            conversationMapper.update(conversation);
            return conversation;
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteConversation(@PathVariable Long id) {
        conversationMapper.deleteById(id);
    }
}
