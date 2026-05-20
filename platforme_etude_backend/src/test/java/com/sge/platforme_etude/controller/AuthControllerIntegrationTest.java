package com.sge.platforme_etude.controller;

import tools.jackson.databind.ObjectMapper;
import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.dto.authentification.AuthRequest;
import com.sge.platforme_etude.dto.authentification.CreateUserRequest;
import com.sge.platforme_etude.helper.enums.Role;
import com.sge.platforme_etude.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - Créer un utilisateur avec succès")
    void shouldRegisterUser() throws Exception {
        CreateUserRequest request = buildCreateRequest(
                "Dupont",
                "Jean",
                "jean@test.com",
                "password123",
                Role.ROLE_USER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.prenom").value("Jean"))
                .andExpect(jsonPath("$.email").value("jean@test.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Rejeter un email en double (409)")
    void shouldRejectDuplicateEmail() throws Exception {
        CreateUserRequest req1 = buildCreateRequest(
                "U1",
                "T1",
                "dup@test.com",
                "password123",
                Role.ROLE_USER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        CreateUserRequest req2 = buildCreateRequest(
                "U2",
                "T2",
                "dup@test.com",
                "password456",
                Role.ROLE_USER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/register - Rejeter les données invalides (400)")
    void shouldRejectInvalidData() throws Exception {
        CreateUserRequest request = buildCreateRequest(
                "",
                "",
                "",
                "short",
                Role.ROLE_USER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Lancer erreur si user n'existe pas")
    void shouldReturn404OnLoginUnknownUser() throws Exception {
        AuthRequest auth = new AuthRequest("unknown@test.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(auth)))
                .andExpect(status().isNotFound());
    }

    private CreateUserRequest buildCreateRequest(
            String nom,
            String prenom,
            String email,
            String password,
            Role role
    ) {
        UserDto userDto = new UserDto();
        userDto.setNom(nom);
        userDto.setPrenom(prenom);
        userDto.setRole(role);
        userDto.setActif(true);

        AuthRequest auth = new AuthRequest(email, password);

        CreateUserRequest request = new CreateUserRequest();
        request.setUser(userDto);
        request.setAuth(auth);

        return request;
    }
}