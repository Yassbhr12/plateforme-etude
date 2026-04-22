package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.MessageChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageChatRepo extends JpaRepository<MessageChat , Long> {
    List<MessageChat> findMessageChatByGroupeEtudeId(Long groupeEtudeId);

    List<MessageChat> findMessageChatByUserId(Long userId);
}

