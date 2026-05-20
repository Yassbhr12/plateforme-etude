package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.Matiere;
import com.sge.platforme_etude.entite.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour MatiereRepo avec PostgreSQL.
 */
@Transactional
class MatiereRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MatiereRepo matiereRepo;

    @Autowired
    private UserRepo userRepo;

    private User user;

    @BeforeEach
    void setUp() {
        matiereRepo.deleteAll();
        userRepo.deleteAll();
        user = userRepo.save(TestDataFactory.createUser("Dupont", "Jean", "jean@test.com"));
    }

    @Test
    @DisplayName("Sauvegarder et retrouver une Matiere par ID")
    void shouldSaveAndFindById() {
        Matiere matiere = matiereRepo.save(TestDataFactory.createMatiere("Mathématiques", 3, user));

        Optional<Matiere> found = matiereRepo.findById(matiere.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Mathématiques");
        assertThat(found.get().getPriorite()).isEqualTo(3);
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Trouver une Matiere par nom")
    void shouldFindMatiereByNom() {
        matiereRepo.save(TestDataFactory.createMatiere("Physique", 2, user));

        Optional<Matiere> found = matiereRepo.findMatiereByNom("Physique");

        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Physique");
    }

    @Test
    @DisplayName("Trouver les Matieres par userId")
    void shouldFindMatieresByUserId() {
        matiereRepo.save(TestDataFactory.createMatiere("Maths", 3, user));
        matiereRepo.save(TestDataFactory.createMatiere("Physique", 2, user));

        User anotherUser = userRepo.save(
                TestDataFactory.createUser("Martin", "Paul", "paul@test.com")
        );
        matiereRepo.save(TestDataFactory.createMatiere("Chimie", 1, anotherUser));

        List<Matiere> matieres = matiereRepo.findMatiereByUserId(user.getId());

        assertThat(matieres).hasSize(2);
    }

    @Test
    @DisplayName("Retourner vide quand le nom de matière n'existe pas")
    void shouldReturnEmptyWhenNomNotFound() {
        Optional<Matiere> found = matiereRepo.findMatiereByNom("Inexistante");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Supprimer une Matiere")
    void shouldDeleteMatiere() {
        Matiere matiere = matiereRepo.save(TestDataFactory.createMatiere("A supprimer", 1, user));

        matiereRepo.delete(matiere);

        assertThat(matiereRepo.findById(matiere.getId())).isEmpty();
    }
}
