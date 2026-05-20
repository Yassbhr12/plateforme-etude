package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.*;
import com.sge.platforme_etude.helper.enums.StatutSession;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CommentaireRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired private CommentaireRepo commentaireRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private MatiereRepo matiereRepo;
    @Autowired private SessionEtudeRepo sessionEtudeRepo;

    private User user;
    private SessionEtude session;

    @BeforeEach
    void setUp() {
        commentaireRepo.deleteAll();
        sessionEtudeRepo.deleteAll();
        matiereRepo.deleteAll();
        userRepo.deleteAll();
        user = userRepo.save(TestDataFactory.createUser("Dupont", "Jean", "jean@test.com"));
        Matiere matiere = matiereRepo.save(TestDataFactory.createMatiere("Maths", 3, user));
        session = sessionEtudeRepo.save(
                TestDataFactory.createSessionEtude("Session Test", user, matiere, StatutSession.PLANIFIEE));
    }

    @Test
    @DisplayName("Sauvegarder et retrouver un Commentaire")
    void shouldSaveAndFind() {
        Commentaire c = commentaireRepo.save(TestDataFactory.createCommentaire("Bon travail!", user, session));
        assertThat(commentaireRepo.findById(c.getId())).isPresent();
    }

    @Test
    @DisplayName("Trouver les Commentaires par userId")
    void shouldFindByUserId() {
        commentaireRepo.save(TestDataFactory.createCommentaire("C1", user, session));
        commentaireRepo.save(TestDataFactory.createCommentaire("C2", user, session));
        List<Commentaire> result = commentaireRepo.findCommentaireByUserId(user.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Trouver les Commentaires par sessionEtudeId")
    void shouldFindBySessionEtudeId() {
        commentaireRepo.save(TestDataFactory.createCommentaire("C1", user, session));
        List<Commentaire> result = commentaireRepo.findCommentaireBySessionEtudeId(session.getId());
        assertThat(result).hasSize(1);
    }
}
