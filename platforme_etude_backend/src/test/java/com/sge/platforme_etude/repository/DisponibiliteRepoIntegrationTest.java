package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class DisponibiliteRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired private DisponibiliteRepo disponibiliteRepo;
    @Autowired private UserRepo userRepo;

    private User user;

    @BeforeEach
    void setUp() {
        disponibiliteRepo.deleteAll();
        userRepo.deleteAll();
        user = userRepo.save(TestDataFactory.createUser("Dupont", "Jean", "jean@test.com"));
    }

    @Test
    @DisplayName("Sauvegarder et retrouver une Disponibilite")
    void shouldSaveAndFind() {
        Disponibilite d = disponibiliteRepo.save(
                TestDataFactory.createDisponibilite(1, LocalTime.of(9, 0), LocalTime.of(12, 0), user));
        assertThat(disponibiliteRepo.findById(d.getId())).isPresent();
    }

    @Test
    @DisplayName("Trouver par userId")
    void shouldFindByUserId() {
        disponibiliteRepo.save(
                TestDataFactory.createDisponibilite(1, LocalTime.of(9, 0), LocalTime.of(12, 0), user));
        disponibiliteRepo.save(
                TestDataFactory.createDisponibilite(3, LocalTime.of(14, 0), LocalTime.of(17, 0), user));
        assertThat(disponibiliteRepo.findDisponibiliteByUserId(user.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Supprimer une Disponibilite")
    void shouldDelete() {
        Disponibilite d = disponibiliteRepo.save(
                TestDataFactory.createDisponibilite(5, LocalTime.of(8, 0), LocalTime.of(10, 0), user));
        disponibiliteRepo.delete(d);
        assertThat(disponibiliteRepo.findById(d.getId())).isEmpty();
    }
}
