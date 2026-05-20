package com.sge.platforme_etude.service;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.dto.MatiereDto;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.exceptions.ForbiddenException;
import com.sge.platforme_etude.helper.exceptions.NotFoundException;
import com.sge.platforme_etude.repository.MatiereRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class MatiereServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MatiereService matiereService;
    @Autowired private UserRepo userRepo;
    @Autowired private MatiereRepo matiereRepo;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        matiereRepo.deleteAll();
        userRepo.deleteAll();
        user = userRepo.save(TestDataFactory.createUser("Dupont", "Jean", "jean@test.com"));
        otherUser = userRepo.save(TestDataFactory.createUser("Martin", "Paul", "paul@test.com"));
    }

    @Test
    @DisplayName("Créer une matière avec succès")
    void shouldCreateMatiere() {
        MatiereDto dto = new MatiereDto();
        dto.setNom("Mathématiques");
        dto.setPriorite(3);

        MatiereDto result = matiereService.createMatiere(dto, user.getId());

        assertThat(result.getId()).isNotNull();
        assertThat(result.getNom()).isEqualTo("Mathématiques");
        assertThat(result.getPriorite()).isEqualTo(3);
        assertThat(result.getUserId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Lancer NotFoundException si userId invalide lors de la création")
    void shouldThrowNotFoundWhenUserNotExists() {
        MatiereDto dto = new MatiereDto();
        dto.setNom("Test");
        dto.setPriorite(1);

        assertThatThrownBy(() -> matiereService.createMatiere(dto, 99999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Trouver une matière par ID")
    void shouldFindMatiereById() {
        MatiereDto created = matiereService.createMatiere(createDto("Physique", 2), user.getId());
        MatiereDto found = matiereService.findMatiereById(created.getId());

        assertThat(found.getNom()).isEqualTo("Physique");
    }

    @Test
    @DisplayName("Trouver toutes les matières")
    void shouldFindAllMatieres() {
        matiereService.createMatiere(createDto("M1", 1), user.getId());
        matiereService.createMatiere(createDto("M2", 2), otherUser.getId());

        List<MatiereDto> all = matiereService.findAllMatieres();
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Trouver les matières par userId")
    void shouldFindMatieresByUserId() {
        matiereService.createMatiere(createDto("M1", 1), user.getId());
        matiereService.createMatiere(createDto("M2", 2), user.getId());
        matiereService.createMatiere(createDto("M3", 3), otherUser.getId());

        List<MatiereDto> result = matiereService.findAllMatieresByUserId(user.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Mettre à jour une matière par le propriétaire")
    void shouldUpdateMatiereByOwner() {
        MatiereDto created = matiereService.createMatiere(createDto("Ancien", 1), user.getId());

        MatiereDto updateDto = new MatiereDto();
        updateDto.setNom("Nouveau");
        updateDto.setPriorite(5);

        MatiereDto updated = matiereService.updateMatiereById(updateDto, created.getId(), user.getId());
        assertThat(updated.getNom()).isEqualTo("Nouveau");
        assertThat(updated.getPriorite()).isEqualTo(5);
    }

    @Test
    @DisplayName("Interdire la mise à jour par un non-propriétaire")
    void shouldForbidUpdateByNonOwner() {
        MatiereDto created = matiereService.createMatiere(createDto("Test", 1), user.getId());

        MatiereDto updateDto = new MatiereDto();
        updateDto.setNom("Hack");

        assertThatThrownBy(() -> matiereService.updateMatiereById(updateDto, created.getId(), otherUser.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Supprimer une matière par le propriétaire")
    void shouldDeleteMatiereByOwner() {
        MatiereDto created = matiereService.createMatiere(createDto("A supprimer", 1), user.getId());
        matiereService.deleteMatiereById(created.getId(), user.getId());

        assertThatThrownBy(() -> matiereService.findMatiereById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Interdire la suppression par un non-propriétaire non-admin")
    void shouldForbidDeleteByNonOwner() {
        MatiereDto created = matiereService.createMatiere(createDto("Test", 1), user.getId());

        assertThatThrownBy(() -> matiereService.deleteMatiereById(created.getId(), otherUser.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    private MatiereDto createDto(String nom, int priorite) {
        MatiereDto dto = new MatiereDto();
        dto.setNom(nom);
        dto.setPriorite(priorite);
        return dto;
    }
}
