package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour UserRepo avec PostgreSQL via Testcontainers.
 */
@Transactional
class UserRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepo userRepo;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        savedUser = userRepo.save(
                TestDataFactory.createUser("Dupont", "Jean", "jean.dupont@test.com")
        );
    }

    @Test
    @DisplayName("Sauvegarder et retrouver un User par ID")
    void shouldSaveAndFindUserById() {
        Optional<User> found = userRepo.findById(savedUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Dupont");
        assertThat(found.get().getPrenom()).isEqualTo("Jean");
        assertThat(found.get().getEmail()).isEqualTo("jean.dupont@test.com");
        assertThat(found.get().getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    @DisplayName("Trouver un User par email")
    void shouldFindUserByEmail() {
        Optional<User> found = userRepo.findUserByEmail("jean.dupont@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Dupont");
    }

    @Test
    @DisplayName("Retourner vide quand l'email n'existe pas")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> found = userRepo.findUserByEmail("inexistant@test.com");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Trouver des Users par nom")
    void shouldFindUsersByNom() {
        userRepo.save(TestDataFactory.createUser("Dupont", "Marie", "marie.dupont@test.com"));

        List<User> users = userRepo.findUserByNom("Dupont");

        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("Trouver des Users par rôle")
    void shouldFindUsersByRole() {
        userRepo.save(TestDataFactory.createAdmin("Admin", "Test", "admin@test.com"));

        List<User> admins = userRepo.findUserByRole(Role.ROLE_ADMIN);
        List<User> users = userRepo.findUserByRole(Role.ROLE_USER);

        assertThat(admins).hasSize(1);
        assertThat(users).hasSize(1);
    }

    @Test
    @DisplayName("Mettre à jour un User existant")
    void shouldUpdateUser() {
        savedUser.setNom("DupontModifie");
        userRepo.save(savedUser);

        Optional<User> updated = userRepo.findById(savedUser.getId());

        assertThat(updated).isPresent();
        assertThat(updated.get().getNom()).isEqualTo("DupontModifie");
    }

    @Test
    @DisplayName("Supprimer un User")
    void shouldDeleteUser() {
        userRepo.delete(savedUser);

        Optional<User> deleted = userRepo.findById(savedUser.getId());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Vérifier la contrainte d'unicité sur l'email")
    void shouldEnforceUniqueEmailConstraint() {
        User duplicate = TestDataFactory.createUser("Autre", "Personne", "jean.dupont@test.com");

        Assertions.assertThrows(Exception.class, () -> {
            userRepo.saveAndFlush(duplicate);
        });
    }
}
