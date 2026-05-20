package com.sge.platforme_etude.service;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.dto.GroupeEtudeDto;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.exceptions.ForbiddenException;
import com.sge.platforme_etude.helper.exceptions.NotFoundException;
import com.sge.platforme_etude.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class GroupeEtudeServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private GroupeEtudeService groupeEtudeService;
    @Autowired private UserRepo userRepo;
    @Autowired private GroupeEtudeRepo groupeEtudeRepo;
    @Autowired private InvitationRepo invitationRepo;
    @Autowired private NotificationRepo notificationRepo;

    private User admin;
    private User otherUser;

    @BeforeEach
    void setUp() {
        invitationRepo.deleteAll();
        notificationRepo.deleteAll();
        groupeEtudeRepo.deleteAll();
        userRepo.deleteAll();
        admin = userRepo.save(TestDataFactory.createAdmin("Admin", "Test", "admin@test.com"));
        otherUser = userRepo.save(TestDataFactory.createUser("User", "Test", "user@test.com"));
    }

    @Test
    @DisplayName("Créer un groupe d'étude avec succès")
    void shouldCreateGroupe() {
        GroupeEtudeDto dto = createDto("MonGroupe", "Description test");

        GroupeEtudeDto result = groupeEtudeService.createGroupeEtude(dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getNom()).isEqualTo("MonGroupe");
        assertThat(result.getAdminId()).isEqualTo(admin.getId());
    }

    @Test
    @DisplayName("Créer un groupe pour l'utilisateur courant")
    void shouldCreateGroupeForCurrentUser() {
        GroupeEtudeDto dto = new GroupeEtudeDto();
        dto.setNom("GroupeCurrent");
        dto.setDescription("Test");

        GroupeEtudeDto result = groupeEtudeService.createGroupeEtudeForCurrentUser(dto, admin.getId());

        assertThat(result.getAdminId()).isEqualTo(admin.getId());
    }

    @Test
    @DisplayName("Trouver un groupe par ID")
    void shouldFindById() {
        GroupeEtudeDto created = groupeEtudeService.createGroupeEtude(createDto("G1", "desc"));
        GroupeEtudeDto found = groupeEtudeService.findGroupeEtudeById(created.getId());
        assertThat(found.getNom()).isEqualTo("G1");
    }

    @Test
    @DisplayName("Lancer NotFoundException si groupe introuvable")
    void shouldThrowNotFound() {
        assertThatThrownBy(() -> groupeEtudeService.findGroupeEtudeById(99999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Trouver tous les groupes")
    void shouldFindAll() {
        groupeEtudeService.createGroupeEtude(createDto("G1", "d1"));
        groupeEtudeService.createGroupeEtude(createDto("G2", "d2"));
        List<GroupeEtudeDto> all = groupeEtudeService.findAllGroupesEtude();
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Trouver les groupes par adminId")
    void shouldFindByAdminId() {
        groupeEtudeService.createGroupeEtude(createDto("G1", "d1"));
        List<GroupeEtudeDto> result = groupeEtudeService.findAllGroupesEtudeByAdminId(admin.getId());
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Mettre à jour un groupe")
    void shouldUpdateGroupe() {
        GroupeEtudeDto created = groupeEtudeService.createGroupeEtude(createDto("Ancien", "old"));

        GroupeEtudeDto updateDto = new GroupeEtudeDto();
        updateDto.setNom("Nouveau");
        updateDto.setDescription("Nouvelle description");

        GroupeEtudeDto updated = groupeEtudeService.updateGroupeEtudeById(updateDto, created.getId());
        assertThat(updated.getNom()).isEqualTo("Nouveau");
    }

    @Test
    @DisplayName("Interdire la mise à jour par un non-admin du groupe")
    void shouldForbidUpdateByNonAdmin() {
        GroupeEtudeDto created = groupeEtudeService.createGroupeEtude(createDto("G1", "d"));
        GroupeEtudeDto updateDto = new GroupeEtudeDto();
        updateDto.setNom("Hack");

        assertThatThrownBy(() -> groupeEtudeService.updateGroupeEtudeByIdForCurrentUser(
                updateDto, created.getId(), otherUser.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Supprimer un groupe")
    void shouldDeleteGroupe() {
        GroupeEtudeDto created = groupeEtudeService.createGroupeEtude(createDto("G1", "d"));
        groupeEtudeService.deleteGroupeEtudeById(created.getId());

        assertThatThrownBy(() -> groupeEtudeService.findGroupeEtudeById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Interdire la suppression par un non-admin du groupe")
    void shouldForbidDeleteByNonAdmin() {
        GroupeEtudeDto created = groupeEtudeService.createGroupeEtude(createDto("G1", "d"));

        assertThatThrownBy(() -> groupeEtudeService.deleteGroupeEtudeByIdForCurrentUser(
                created.getId(), otherUser.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    private GroupeEtudeDto createDto(String nom, String description) {
        GroupeEtudeDto dto = new GroupeEtudeDto();
        dto.setNom(nom);
        dto.setDescription(description);
        dto.setAdminId(admin.getId());
        return dto;
    }
}
