package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour GroupeEtudeRepo avec PostgreSQL.
 */
@Transactional
class GroupeEtudeRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private GroupeEtudeRepo groupeEtudeRepo;

    @Autowired
    private UserRepo userRepo;

    private User admin;

    @BeforeEach
    void setUp() {
        groupeEtudeRepo.deleteAll();
        userRepo.deleteAll();
        admin = userRepo.save(TestDataFactory.createAdmin("Admin", "Test", "admin@test.com"));
    }

    @Test
    @DisplayName("Sauvegarder et retrouver un GroupeEtude par ID")
    void shouldSaveAndFindById() {
        GroupeEtude groupe = groupeEtudeRepo.save(
                TestDataFactory.createGroupeEtude("Groupe Maths", "Étude des maths", admin)
        );

        Optional<GroupeEtude> found = groupeEtudeRepo.findById(groupe.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Groupe Maths");
        assertThat(found.get().getDescription()).isEqualTo("Étude des maths");
        assertThat(found.get().getAdmin().getId()).isEqualTo(admin.getId());
    }

    @Test
    @DisplayName("Trouver un GroupeEtude par nom")
    void shouldFindGroupeEtudeByNom() {
        groupeEtudeRepo.save(TestDataFactory.createGroupeEtude("Physique Avancée", "Groupe physique", admin));

        Optional<GroupeEtude> found = groupeEtudeRepo.findGroupeEtudeByNom("Physique Avancée");

        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Physique Avancée");
    }

    @Test
    @DisplayName("Vérifier l'existence d'un GroupeEtude par nom")
    void shouldCheckExistsByNom() {
        groupeEtudeRepo.save(TestDataFactory.createGroupeEtude("MonGroupe", "desc", admin));

        assertThat(groupeEtudeRepo.existsGroupeEtudeByNom("MonGroupe")).isTrue();
        assertThat(groupeEtudeRepo.existsGroupeEtudeByNom("Inexistant")).isFalse();
    }

    @Test
    @DisplayName("Trouver les GroupeEtude par admin")
    void shouldFindGroupeEtudeByAdmin() {
        groupeEtudeRepo.save(TestDataFactory.createGroupeEtude("Groupe1", "desc1", admin));
        groupeEtudeRepo.save(TestDataFactory.createGroupeEtude("Groupe2", "desc2", admin));

        User anotherAdmin = userRepo.save(
                TestDataFactory.createAdmin("AutreAdmin", "Test", "autre.admin@test.com")
        );
        groupeEtudeRepo.save(TestDataFactory.createGroupeEtude("Groupe3", "desc3", anotherAdmin));

        List<GroupeEtude> groupes = groupeEtudeRepo.findGroupeEtudeByAdmin(admin);

        assertThat(groupes).hasSize(2);
    }

    @Test
    @DisplayName("Supprimer un GroupeEtude")
    void shouldDeleteGroupeEtude() {
        GroupeEtude groupe = groupeEtudeRepo.save(
                TestDataFactory.createGroupeEtude("A supprimer", "desc", admin)
        );

        groupeEtudeRepo.delete(groupe);

        assertThat(groupeEtudeRepo.findById(groupe.getId())).isEmpty();
    }

    @Test
    @DisplayName("Vérifier la contrainte d'unicité sur le nom du groupe")
    void shouldEnforceUniqueNomConstraint() {
        groupeEtudeRepo.save(TestDataFactory.createGroupeEtude("NomUnique", "desc", admin));

        GroupeEtude duplicate = TestDataFactory.createGroupeEtude("NomUnique", "autre desc", admin);

        Assertions.assertThrows(Exception.class, () -> {
            groupeEtudeRepo.saveAndFlush(duplicate);
        });
    }
}
