package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.*;
import com.sge.platforme_etude.helper.enums.StatutSession;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class SessionEtudeRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired private SessionEtudeRepo sessionEtudeRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private MatiereRepo matiereRepo;

    private User user;
    private Matiere matiere;

    @BeforeEach
    void setUp() {
        sessionEtudeRepo.deleteAll();
        matiereRepo.deleteAll();
        userRepo.deleteAll();
        user = userRepo.save(TestDataFactory.createUser("Dupont", "Jean", "jean@test.com"));
        matiere = matiereRepo.save(TestDataFactory.createMatiere("Maths", 3, user));
    }

    @Test
    @DisplayName("Sauvegarder et retrouver une SessionEtude")
    void shouldSaveAndFindById() {
        SessionEtude session = sessionEtudeRepo.save(
                TestDataFactory.createSessionEtude("Session Maths", user, matiere, StatutSession.PLANIFIEE));
        var found = sessionEtudeRepo.findById(session.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitre()).isEqualTo("Session Maths");
    }

    @Test
    @DisplayName("Trouver les Sessions par userId")
    void shouldFindByUserId() {
        sessionEtudeRepo.save(TestDataFactory.createSessionEtude("S1", user, matiere, StatutSession.PLANIFIEE));
        sessionEtudeRepo.save(TestDataFactory.createSessionEtude("S2", user, matiere, StatutSession.TERMINEE));
        assertThat(sessionEtudeRepo.findSessionEtudeByUserId(user.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Trouver les Sessions par matiereId")
    void shouldFindByMatiereId() {
        sessionEtudeRepo.save(TestDataFactory.createSessionEtude("SM", user, matiere, StatutSession.PLANIFIEE));
        assertThat(sessionEtudeRepo.findSessionEtudeByMatiereId(matiere.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Trouver les Sessions par statut")
    void shouldFindByStatut() {
        sessionEtudeRepo.save(TestDataFactory.createSessionEtude("S1", user, matiere, StatutSession.PLANIFIEE));
        sessionEtudeRepo.save(TestDataFactory.createSessionEtude("S2", user, matiere, StatutSession.TERMINEE));
        assertThat(sessionEtudeRepo.findSessionEtudeByStatut(StatutSession.PLANIFIEE)).hasSize(1);
        assertThat(sessionEtudeRepo.findSessionEtudeByStatut(StatutSession.TERMINEE)).hasSize(1);
    }

    @Test
    @DisplayName("Trouver les Sessions dans une plage de dates")
    void shouldFindByDateRange() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 18, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 25, 0, 0);

        SessionEtude s = TestDataFactory.createSessionEtude("InRange", user, matiere, StatutSession.PLANIFIEE);
        s.setDateDebut(LocalDateTime.of(2026, 5, 20, 9, 0));
        s.setDateFin(LocalDateTime.of(2026, 5, 20, 11, 0));
        sessionEtudeRepo.save(s);

        SessionEtude out = TestDataFactory.createSessionEtude("OutRange", user, matiere, StatutSession.PLANIFIEE);
        out.setDateDebut(LocalDateTime.of(2026, 5, 26, 9, 0));
        out.setDateFin(LocalDateTime.of(2026, 5, 26, 11, 0));
        sessionEtudeRepo.save(out);

        List<SessionEtude> result = sessionEtudeRepo
                .findByUserIdAndDateDebutGreaterThanEqualAndDateDebutLessThan(user.getId(), start, end);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitre()).isEqualTo("InRange");
    }
}
