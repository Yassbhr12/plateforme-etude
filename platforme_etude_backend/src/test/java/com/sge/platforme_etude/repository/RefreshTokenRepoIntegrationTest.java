package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class RefreshTokenRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired private RefreshTokenRepo refreshTokenRepo;
    @Autowired private UserRepo userRepo;

    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenRepo.deleteAll();
        userRepo.deleteAll();
        user = userRepo.save(TestDataFactory.createUser("Dupont", "Jean", "jean@test.com"));
    }

    @Test
    @DisplayName("Sauvegarder et retrouver par token")
    void shouldSaveAndFindByToken() {
        refreshTokenRepo.save(TestDataFactory.createRefreshToken("abc-123-token", user));
        Optional<RefreshToken> found = refreshTokenRepo.findByToken("abc-123-token");
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Trouver tous les tokens par user")
    void shouldFindAllByUser() {
        refreshTokenRepo.save(TestDataFactory.createRefreshToken("token-1", user));
        refreshTokenRepo.save(TestDataFactory.createRefreshToken("token-2", user));
        List<RefreshToken> tokens = refreshTokenRepo.findAllByUser(user);
        assertThat(tokens).hasSize(2);
    }

    @Test
    @DisplayName("Supprimer par user")
    void shouldDeleteByUser() {
        refreshTokenRepo.save(TestDataFactory.createRefreshToken("token-del", user));
        refreshTokenRepo.deleteByUser(user);
        assertThat(refreshTokenRepo.findAllByUser(user)).isEmpty();
    }

    @Test
    @DisplayName("Vérifier isExpired")
    void shouldCheckExpiry() {
        RefreshToken token = refreshTokenRepo.save(TestDataFactory.createRefreshToken("tk", user));
        assertThat(token.isExpired()).isFalse();
    }
}
