package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class MessageChatRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MessageChatRepo messageChatRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private GroupeEtudeRepo groupeEtudeRepo;

    private User user;
    private GroupeEtude groupe;

    @BeforeEach
    void setUp() {
        messageChatRepo.deleteAll();
        groupeEtudeRepo.deleteAll();
        userRepo.deleteAll();
        user = userRepo.save(TestDataFactory.createUser("Dupont", "Jean", "jean@test.com"));
        groupe = groupeEtudeRepo.save(TestDataFactory.createGroupeEtude("Groupe1", "desc", user));
    }

    @Test
    @DisplayName("Sauvegarder et retrouver un MessageChat")
    void shouldSaveAndFind() {
        MessageChat msg = messageChatRepo.save(TestDataFactory.createMessageChat("Hello!", user, groupe));
        assertThat(messageChatRepo.findById(msg.getId())).isPresent();
    }

    @Test
    @DisplayName("Trouver par groupeEtudeId")
    void shouldFindByGroupeEtudeId() {
        messageChatRepo.save(TestDataFactory.createMessageChat("Msg1", user, groupe));
        messageChatRepo.save(TestDataFactory.createMessageChat("Msg2", user, groupe));
        assertThat(messageChatRepo.findMessageChatByGroupeEtudeId(groupe.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Trouver par userId")
    void shouldFindByUserId() {
        messageChatRepo.save(TestDataFactory.createMessageChat("Msg1", user, groupe));
        assertThat(messageChatRepo.findMessageChatByUserId(user.getId())).hasSize(1);
    }
}
