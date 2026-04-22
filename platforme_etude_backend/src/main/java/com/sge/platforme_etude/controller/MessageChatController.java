package com.sge.platforme_etude.controller;

import com.sge.platforme_etude.dto.MessageChatDto;
import com.sge.platforme_etude.service.MessageChatService;
import com.sge.platforme_etude.service.user.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageChatController {

    private final MessageChatService service;
    private final CurrentUserService currentUserService;

    public MessageChatController(MessageChatService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/groupes/{groupeId}/messages")
    public ResponseEntity<MessageChatDto> createGroupeMessageChat(@PathVariable Long groupeId , @Valid @RequestBody MessageChatDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMessageChat(dto, groupeId, currentUserService.getCurrentUserId()));
    }

    @GetMapping("/groupes/{groupeId}/messages")
    public ResponseEntity<List<MessageChatDto>> getAllGroupeMessagesChat(@PathVariable Long groupeId){
        return ResponseEntity.status(HttpStatus.OK).body(service.findAllMessagesChatByGroupeEtudeId(groupeId));
    }


    @GetMapping("/me/messages")
    public ResponseEntity<List<MessageChatDto>> getMyMessages() {
        return ResponseEntity.status(HttpStatus.OK).body(service.findAllMessagesChatByUserId(currentUserService.getCurrentUserId()));
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<MessageChatDto> updateMessageById(@PathVariable Long id, @Valid @RequestBody MessageChatDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(service.updateMessageChatById(dto, id, currentUserService.getCurrentUserId()));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<String> deleteMessageById(@PathVariable Long id) {
        service.deleteMessageChatById(id, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Message is deleted successfully");
    }
}
