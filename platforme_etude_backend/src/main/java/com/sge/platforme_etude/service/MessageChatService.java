package com.sge.platforme_etude.service;

import com.sge.platforme_etude.dto.MessageChatDto;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.MessageChat;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.mapper.MessageChatMapper;
import com.sge.platforme_etude.repository.GroupeEtudeRepo;
import com.sge.platforme_etude.repository.MessageChatRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageChatService {

    private final MessageChatMapper mapper;
    private final MessageChatRepo repo;
    private final UserRepo userRepo;
    private final GroupeEtudeRepo groupeEtudeRepo;

    public MessageChatService(MessageChatMapper mapper, MessageChatRepo repo, UserRepo userRepo, GroupeEtudeRepo groupeEtudeRepo) {
        this.mapper = mapper;
        this.repo = repo;
        this.userRepo = userRepo;
        this.groupeEtudeRepo = groupeEtudeRepo;
    }

    @Transactional
    public MessageChatDto createMessageChat(MessageChatDto dto , Long groupeId) {
        if (dto.getUserId() == null) {
            throw new RuntimeException("userId is required");
        }
        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        GroupeEtude groupeEtude = groupeEtudeRepo.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("GroupeEtude Not Found"));


        MessageChat messageChat = mapper.toEntity(dto, user, groupeEtude);
        return mapper.toDto(repo.save(messageChat));
    }

    @Transactional
    public MessageChatDto createMessageChat(MessageChatDto dto, Long groupeId, Long currentUserId) {
        dto.setUserId(currentUserId);
        return createMessageChat(dto, groupeId);
    }

    public MessageChatDto findMessageChatById(Long id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("MessageChat Not Found"));
    }

    public List<MessageChatDto> findAllMessagesChat() {
        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<MessageChatDto> findAllMessagesChatByUserId(Long userId) {
        return repo.findMessageChatByUserId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<MessageChatDto> findAllMessagesChatByGroupeEtudeId(Long groupeEtudeId) {
        return repo.findMessageChatByGroupeEtudeId(groupeEtudeId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public MessageChatDto updateMessageChatById(MessageChatDto dto, Long id) {
        MessageChat messageChat = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("MessageChat Not Found"));

        User user = messageChat.getUser();
        if (dto.getUserId() != null) {
            user = userRepo.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User Not Found"));
        }

        GroupeEtude groupeEtude = messageChat.getGroupeEtude();
        if (dto.getGroupeEtudeId() != null) {
            groupeEtude = groupeEtudeRepo.findById(dto.getGroupeEtudeId())
                    .orElseThrow(() -> new RuntimeException("GroupeEtude Not Found"));
        }

        mapper.updateEntity(messageChat, dto, user, groupeEtude);
        return mapper.toDto(repo.save(messageChat));
    }

    @Transactional
    public MessageChatDto updateMessageChatById(MessageChatDto dto, Long id, Long currentUserId) {
        MessageChat messageChat = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("MessageChat Not Found"));
        if (messageChat.getUser() == null || !messageChat.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Message does not belong to current user");
        }
        dto.setUserId(currentUserId);
        return updateMessageChatById(dto, id);
    }

    @Transactional
    public void deleteMessageChatById(Long id) {
        MessageChat messageChat = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("MessageChat Not Found"));
        repo.delete(messageChat);
    }

    @Transactional
    public void deleteMessageChatById(Long id, Long currentUserId) {
        MessageChat messageChat = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("MessageChat Not Found"));
        if (messageChat.getUser() == null || !messageChat.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Message does not belong to current user");
        }
        repo.delete(messageChat);
    }
}

