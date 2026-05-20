package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ObjectifHebdoRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired private ObjectifHebdoRepo objectifHebdoRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private MatiereRepo matiereRepo;

    private User user;
    private Matiere matiere;
    private final LocalDate semaine = LocalDate.of(2026, 5, 18);

    @BeforeEach
    void setUp() {
        objectifHebdoRepo.deleteAll();
        matiereRepo.deleteAll();
        userRepo.deleteAll();
        user = userRepo.save(TestDataFactory.createUser("Dupont", "Jean", "jean@test.com"));
        matiere = matiereRepo.save(TestDataFactory.createMatiere("Maths", 3, user));
    }

    @Test
    @DisplayName("Sauvegarder et retrouver un ObjectifHebdo")
    void shouldSaveAndFind() {
        ObjectifHebdo obj = objectifHebdoRepo.save(
                TestDataFactory.createObjectifHebdo(semaine, 10, user, matiere));
        assertThat(objectifHebdoRepo.findById(obj.getId())).isPresent();
    }

    @Test
    @DisplayName("Trouver par userId")
    void shouldFindByUserId() {
        objectifHebdoRepo.save(TestDataFactory.createObjectifHebdo(semaine, 10, user, matiere));
        assertThat(objectifHebdoRepo.findObjectifHebdoByUserId(user.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Trouver par matiereId")
    void shouldFindByMatiereId() {
        objectifHebdoRepo.save(TestDataFactory.createObjectifHebdo(semaine, 10, user, matiere));
        assertThat(objectifHebdoRepo.findObjectifHebdoByMatiereId(matiere.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Trouver par userId, matiereId et semaine")
    void shouldFindByUserIdAndMatiereIdAndSemaine() {
        objectifHebdoRepo.save(TestDataFactory.createObjectifHebdo(semaine, 10, user, matiere));
        Optional<ObjectifHebdo> found = objectifHebdoRepo
                .findByUserIdAndMatiereIdAndSemaine(user.getId(), matiere.getId(), semaine);
        assertThat(found).isPresent();
        assertThat(found.get().getHeuresCibles()).isEqualTo(10);
    }

    @Test
    @DisplayName("Vérifier l'existence par userId, matiereId et semaine")
    void shouldCheckExistence() {
        objectifHebdoRepo.save(TestDataFactory.createObjectifHebdo(semaine, 10, user, matiere));
        assertThat(objectifHebdoRepo.existsByUserIdAndMatiereIdAndSemaine(
                user.getId(), matiere.getId(), semaine)).isTrue();
        assertThat(objectifHebdoRepo.existsByUserIdAndMatiereIdAndSemaine(
                user.getId(), matiere.getId(), semaine.plusWeeks(1))).isFalse();
    }

    @Test
    @DisplayName("Trouver par semaine")
    void shouldFindBySemaine() {
        objectifHebdoRepo.save(TestDataFactory.createObjectifHebdo(semaine, 10, user, matiere));
        assertThat(objectifHebdoRepo.findObjectifHebdoBySemaine(semaine)).hasSize(1);
    }

    @Test
    @DisplayName("Trouver par userId et semaine")
    void shouldFindByUserIdAndSemaine() {
        objectifHebdoRepo.save(TestDataFactory.createObjectifHebdo(semaine, 10, user, matiere));
        List<ObjectifHebdo> result = objectifHebdoRepo.findByUserIdAndSemaine(user.getId(), semaine);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Contrainte d'unicité user+matiere+semaine")
    void shouldEnforceUniqueConstraint() {
        objectifHebdoRepo.save(TestDataFactory.createObjectifHebdo(semaine, 10, user, matiere));
        ObjectifHebdo dup = TestDataFactory.createObjectifHebdo(semaine, 20, user, matiere);
        Assertions.assertThrows(Exception.class, () -> objectifHebdoRepo.saveAndFlush(dup));
    }
}
