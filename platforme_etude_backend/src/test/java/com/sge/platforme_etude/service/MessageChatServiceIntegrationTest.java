package com.sge.platforme_etude.service;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.dto.MessageChatDto;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.exceptions.ForbiddenException;
import com.sge.platforme_etude.repository.GroupeEtudeRepo;
import com.sge.platforme_etude.repository.MessageChatRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class MessageChatServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MessageChatService messageChatService;
    @Autowired private MessageChatRepo messageChatRepo;
    @Autowired private GroupeEtudeRepo groupeEtudeRepo;
    @Autowired private UserRepo userRepo;

    private User admin;
    private User otherUser;
    private GroupeEtude groupe;

    @BeforeEach
    void setUp() {
        messageChatRepo.deleteAll();
        groupeEtudeRepo.deleteAll();
        userRepo.deleteAll();

        admin = userRepo.save(TestDataFactory.createUser("Leader", "Amine", "amine.chat@test.com"));
        otherUser = userRepo.save(TestDataFactory.createUser("Externe", "Nadia", "nadia.chat@test.com"));
        groupe = groupeEtudeRepo.save(TestDataFactory.createGroupeEtude("Groupe Chat Test", "desc", admin));
    }

    @Test
    @DisplayName("Creer un message depuis l'UI sans dateEnvoi")
    void shouldCreateMessageWithServerDate() {
        MessageChatDto dto = new MessageChatDto();
        dto.setContenu("Bonjour le groupe");

        MessageChatDto result = messageChatService.createMessageChat(dto, groupe.getId(), admin.getId());

        assertThat(result.getId()).isNotNull();
        assertThat(result.getContenu()).isEqualTo("Bonjour le groupe");
        assertThat(result.getDateEnvoi()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(admin.getId());
        assertThat(result.getGroupeEtudeId()).isEqualTo(groupe.getId());
    }

    @Test
    @DisplayName("Interdire le chat a un utilisateur non membre")
    void shouldForbidMessageFromNonMember() {
        MessageChatDto dto = new MessageChatDto();
        dto.setContenu("Je ne dois pas entrer");

        assertThatThrownBy(() -> messageChatService.createMessageChat(dto, groupe.getId(), otherUser.getId()))
                .isInstanceOf(ForbiddenException.class);
    }
}
