package com.sge.platforme_etude.service;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.dto.authentification.AuthRequest;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.Role;
import com.sge.platforme_etude.helper.exceptions.ConflictException;
import com.sge.platforme_etude.helper.exceptions.NotFoundException;
import com.sge.platforme_etude.repository.UserRepo;
import com.sge.platforme_etude.service.user.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class UserServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private UserRepo userRepo;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("Créer un utilisateur avec succès")
    void shouldCreateUser() {
        UserDto userDto = new UserDto();
        userDto.setNom("Dupont");
        userDto.setPrenom("Jean");
        userDto.setRole(Role.ROLE_USER);
        userDto.setActif(true);

        AuthRequest auth = new AuthRequest("jean@test.com", "password123");

        UserDto result = userService.createUser(userDto, auth);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getNom()).isEqualTo("Dupont");
        assertThat(result.getEmail()).isEqualTo("jean@test.com");
    }

    @Test
    @DisplayName("Lancer ConflictException si email déjà existant")
    void shouldThrowConflictOnDuplicateEmail() {
        UserDto dto = new UserDto();
        dto.setNom("User1");
        dto.setPrenom("Test");
        dto.setRole(Role.ROLE_USER);
        dto.setActif(true);
        userService.createUser(dto, new AuthRequest("dup@test.com", "password123"));

        UserDto dto2 = new UserDto();
        dto2.setNom("User2");
        dto2.setPrenom("Test");
        dto2.setRole(Role.ROLE_USER);
        dto2.setActif(true);

        assertThatThrownBy(() -> userService.createUser(dto2, new AuthRequest("dup@test.com", "password456")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Trouver un utilisateur par ID")
    void shouldFindById() {
        User saved = userRepo.save(TestDataFactory.createUser("Test", "User", "test@test.com"));
        UserDto found = userService.findUserById(saved.getId());
        assertThat(found.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("Lancer NotFoundException si ID inexistant")
    void shouldThrowNotFoundById() {
        assertThatThrownBy(() -> userService.findUserById(99999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Trouver tous les utilisateurs")
    void shouldFindAll() {
        userRepo.save(TestDataFactory.createUser("U1", "T1", "u1@test.com"));
        userRepo.save(TestDataFactory.createUser("U2", "T2", "u2@test.com"));
        List<UserDto> all = userService.findAllUsers();
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Trouver un utilisateur par email")
    void shouldFindByEmail() {
        userRepo.save(TestDataFactory.createUser("Test", "User", "find@test.com"));
        UserDto found = userService.findUserByEmail("find@test.com");
        assertThat(found.getNom()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Mettre à jour un utilisateur")
    void shouldUpdateUser() {
        User saved = userRepo.save(TestDataFactory.createUser("Ancien", "Nom", "upd@test.com"));

        UserDto updateDto = new UserDto();
        updateDto.setNom("Nouveau");
        updateDto.setPrenom("Prenom");

        UserDto updated = userService.updateUserById(updateDto, saved.getId());
        assertThat(updated.getNom()).isEqualTo("Nouveau");
    }

    @Test
    @DisplayName("Supprimer un utilisateur")
    void shouldDeleteUser() {
        User saved = userRepo.save(TestDataFactory.createUser("Del", "User", "del@test.com"));
        userService.deleteUserById(saved.getId());

        assertThatThrownBy(() -> userService.findUserById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
