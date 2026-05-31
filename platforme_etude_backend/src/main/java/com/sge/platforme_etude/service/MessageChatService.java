package com.sge.platforme_etude.service;

import com.sge.platforme_etude.dto.MessageChatDto;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.MessageChat;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.exceptions.BadRequestException;
import com.sge.platforme_etude.helper.exceptions.ForbiddenException;
import com.sge.platforme_etude.helper.exceptions.NotFoundException;
import com.sge.platforme_etude.mapper.MessageChatMapper;
import com.sge.platforme_etude.repository.GroupeEtudeRepo;
import com.sge.platforme_etude.repository.MessageChatRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
            throw new BadRequestException("userId is required");
        }
        if (dto.getContenu() == null || dto.getContenu().isBlank()) {
            throw new BadRequestException("contenu is required");
        }
        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("User Not Found"));
        GroupeEtude groupeEtude = groupeEtudeRepo.findById(groupeId)
                .orElseThrow(() -> new NotFoundException("GroupeEtude Not Found"));

        assertGroupMember(groupeEtude, user.getId());
        dto.setDateEnvoi(LocalDateTime.now());

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
                .orElseThrow(() -> new NotFoundException("MessageChat Not Found"));
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

    public List<MessageChatDto> findAllMessagesChatByGroupeEtudeId(Long groupeEtudeId, Long currentUserId) {
        GroupeEtude groupeEtude = groupeEtudeRepo.findById(groupeEtudeId)
                .orElseThrow(() -> new NotFoundException("GroupeEtude Not Found"));
        assertGroupMember(groupeEtude, currentUserId);
        return findAllMessagesChatByGroupeEtudeId(groupeEtudeId);
    }

    @Transactional
    public MessageChatDto updateMessageChatById(MessageChatDto dto, Long id) {
        MessageChat messageChat = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("MessageChat Not Found"));

        User user = messageChat.getUser();
        if (dto.getUserId() != null) {
            user = userRepo.findById(dto.getUserId())
                    .orElseThrow(() -> new NotFoundException("User Not Found"));
        }

        GroupeEtude groupeEtude = messageChat.getGroupeEtude();
        if (dto.getGroupeEtudeId() != null) {
            groupeEtude = groupeEtudeRepo.findById(dto.getGroupeEtudeId())
                    .orElseThrow(() -> new NotFoundException("GroupeEtude Not Found"));
        }

        mapper.updateEntity(messageChat, dto, user, groupeEtude);
        return mapper.toDto(repo.save(messageChat));
    }

    @Transactional
    public MessageChatDto updateMessageChatById(MessageChatDto dto, Long id, Long currentUserId) {
        MessageChat messageChat = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("MessageChat Not Found"));
        if (messageChat.getUser() == null || !messageChat.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Message does not belong to current user");
        }
        dto.setUserId(currentUserId);
        return updateMessageChatById(dto, id);
    }

    @Transactional
    public void deleteMessageChatById(Long id) {
        MessageChat messageChat = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("MessageChat Not Found"));
        repo.delete(messageChat);
    }

    @Transactional
    public void deleteMessageChatById(Long id, Long currentUserId) {
        MessageChat messageChat = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("MessageChat Not Found"));
        if (messageChat.getUser() == null || !messageChat.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Message does not belong to current user");
        }
        repo.delete(messageChat);
    }

    private void assertGroupMember(GroupeEtude groupeEtude, Long userId) {
        if (groupeEtude.getAdmin() != null && groupeEtude.getAdmin().getId().equals(userId)) {
            return;
        }
        boolean member = groupeEtude.getUsers() != null && groupeEtude.getUsers().stream()
                .anyMatch(user -> user.getId().equals(userId));
        if (!member) {
            throw new ForbiddenException("Only group members can access this chat");
        }
    }
}

