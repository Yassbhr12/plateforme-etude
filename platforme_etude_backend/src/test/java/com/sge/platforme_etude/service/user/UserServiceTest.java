package com.sge.platforme_etude.service.user;

import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.dto.authentification.AuthRequest;
import com.sge.platforme_etude.dto.authentification.ValidationCodeRequest;
import com.sge.platforme_etude.entite.RefreshToken;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.Role;
import com.sge.platforme_etude.helper.exceptions.ConflictException;
import com.sge.platforme_etude.helper.exceptions.ForbiddenException;
import com.sge.platforme_etude.helper.security.jwt.JwtUtils;
import com.sge.platforme_etude.mapper.UserMapper;
import com.sge.platforme_etude.repository.UserRepo;
import com.sge.platforme_etude.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService service;

    @Test
    void createUser_throwsConflict_whenEmailExists() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");

        when(userRepo.findUserByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class, () -> service.createUser(new UserDto(), authRequest));
    }

    @Test
    void loginProcess_throwsForbidden_whenUserInactive() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("secret");

        User user = new User();
        user.setEmail("test@example.com");
        user.setActif(false);

        when(userRepo.findUserByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class, () -> service.loginProcess(authRequest));
    }

    @Test
    void loginProcess_setsValidationCode_andSendsEmail() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("user@example.com");
        authRequest.setPassword("secret");

        User user = new User();
        user.setEmail("user@example.com");
        user.setActif(true);
        user.setMotDePasse(new BCryptPasswordEncoder().encode("secret"));

        when(userRepo.findUserByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.loginProcess(authRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        User saved = captor.getValue();

        assertNotNull(saved.getValidationCode());
        assertNotNull(saved.getValidationCodeExpiration());
        verify(emailService).sendEmail(eq("user@example.com"), any(), any());
    }

    @Test
    void validateCode_returnsTokens_andClearsCode() {
        ValidationCodeRequest request = new ValidationCodeRequest();
        request.setEmail("user@example.com");
        request.setValidationCode("123456");

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.ROLE_USER);
        user.setValidationCode("123456");
        user.setValidationCodeExpiration(LocalDateTime.now().plusMinutes(5).truncatedTo(ChronoUnit.SECONDS));

        when(userRepo.findUserByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken("user@example.com", Role.ROLE_USER.name())).thenReturn("access-token");
        RefreshToken refreshToken = RefreshToken.builder().token("refresh-token").build();
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String[] tokens = service.validateCode(request);

        assertArrayEquals(new String[]{"access-token", "refresh-token"}, tokens);
        assertNull(user.getValidationCode());
        assertNull(user.getValidationCodeExpiration());
        verify(refreshTokenService).revokeAllUserTokens(user);
    }
}

