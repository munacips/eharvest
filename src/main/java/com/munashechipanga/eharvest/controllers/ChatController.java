package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.chat.*;
import com.munashechipanga.eharvest.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessageWs(@Payload SendMessageRequest req) {
        chatService.sendMessage(req);
    }

    @PostMapping("/conversations")
    @ResponseBody
    public ResponseEntity<ConversationDTO> createConversation(@RequestBody CreateConversationRequest req) {
        return ResponseEntity.ok(chatService.createConversation(req));
    }

    @GetMapping("/conversations")
    @ResponseBody
    public ResponseEntity<List<ConversationDTO>> getUserConversations(@RequestParam Long userId) {
        return ResponseEntity.ok(chatService.getUserConversations(userId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @ResponseBody
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long conversationId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, userId));
    }

    @PostMapping("/conversations/{conversationId}/read")
    @ResponseBody
    public ResponseEntity<?> markRead(@PathVariable Long conversationId,
            @RequestParam Long userId) {
        chatService.markRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/messages/{messageId}")
    @ResponseBody
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId,
            @RequestParam Long requestingUserId) {
        chatService.deleteMessage(messageId, requestingUserId);
        return ResponseEntity.ok().build();
    }
}
