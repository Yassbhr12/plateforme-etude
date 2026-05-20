package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.*;
import com.sge.platforme_etude.helper.enums.TypeNotif;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class NotificationRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired private NotificationRepo notificationRepo;
    @Autowired private UserRepo userRepo;

    private User user;

    @BeforeEach
    void setUp() {
        notificationRepo.deleteAll();
        userRepo.deleteAll();
        user = userRepo.save(TestDataFactory.createUser("Dupont", "Jean", "jean@test.com"));
    }

    @Test
    @DisplayName("Sauvegarder et retrouver une Notification")
    void shouldSaveAndFind() {
        Notification n = notificationRepo.save(
                TestDataFactory.createNotification(TypeNotif.RAPPEL_SESSION, "Rappel!", user));
        assertThat(notificationRepo.findById(n.getId())).isPresent();
    }

    @Test
    @DisplayName("Trouver par type")
    void shouldFindByType() {
        notificationRepo.save(TestDataFactory.createNotification(TypeNotif.RAPPEL_SESSION, "R1", user));
        notificationRepo.save(TestDataFactory.createNotification(TypeNotif.INVITATION_GROUPE, "I1", user));
        assertThat(notificationRepo.findNotificationByType(TypeNotif.RAPPEL_SESSION)).hasSize(1);
    }

    @Test
    @DisplayName("Trouver par user")
    void shouldFindByUser() {
        notificationRepo.save(TestDataFactory.createNotification(TypeNotif.RAPPEL_SESSION, "R1", user));
        List<Notification> result = notificationRepo.findNotificationByUser(user);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Trouver par userId")
    void shouldFindByUserId() {
        notificationRepo.save(TestDataFactory.createNotification(TypeNotif.OBJECTIF_ATTEINT, "Bravo!", user));
        assertThat(notificationRepo.findNotificationByUserId(user.getId())).hasSize(1);
    }
}
