package com.sge.platforme_etude.mapper;

import com.sge.platforme_etude.dto.MessageChatDto;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.MessageChat;
import com.sge.platforme_etude.entite.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageChatMapper {

    public MessageChatDto toDto(MessageChat messageChat) {
        if (messageChat == null) {
            return null;
        }

        MessageChatDto dto = new MessageChatDto();
        dto.setId(messageChat.getId());
        dto.setContenu(messageChat.getContenu());
        dto.setDateEnvoi(messageChat.getDateEnvoi());

        if (messageChat.getUser() != null) {
            dto.setUserId(messageChat.getUser().getId());
            dto.setUserNom(messageChat.getUser().getNom());
            dto.setUserEmail(messageChat.getUser().getEmail());
        }

        if (messageChat.getGroupeEtude() != null) {
            dto.setGroupeEtudeId(messageChat.getGroupeEtude().getId());
            dto.setGroupeEtudeNom(messageChat.getGroupeEtude().getNom());
        }

        return dto;
    }

    public MessageChat toEntity(MessageChatDto dto, User user, GroupeEtude groupeEtude) {
        if (dto == null) {
            return null;
        }

        MessageChat messageChat = new MessageChat();
        messageChat.setId(dto.getId());
        messageChat.setContenu(dto.getContenu());
        messageChat.setDateEnvoi(dto.getDateEnvoi());
        messageChat.setUser(user);
        messageChat.setGroupeEtude(groupeEtude);

        return messageChat;
    }

    public void updateEntity(MessageChat messageChat, MessageChatDto dto, User user, GroupeEtude groupeEtude) {
        if (messageChat == null || dto == null) {
            return;
        }

        messageChat.setContenu(dto.getContenu());
        messageChat.setDateEnvoi(dto.getDateEnvoi());
        messageChat.setUser(user == null ? messageChat.getUser() : user);
        messageChat.setGroupeEtude(groupeEtude == null ? messageChat.getGroupeEtude() : groupeEtude);
    }

    public List<MessageChatDto> toDtoList(List<MessageChat> messagesChat) {
        if (messagesChat == null) {
            return List.of();
        }
        return messagesChat.stream()
                .map(this::toDto)
                .toList();
    }
}

