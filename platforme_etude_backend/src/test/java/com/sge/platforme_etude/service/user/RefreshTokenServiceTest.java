package com.sge.platforme_etude.service.user;

import com.sge.platforme_etude.entite.RefreshToken;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.exceptions.TokenException;
import com.sge.platforme_etude.repository.RefreshTokenRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepo refreshTokenRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private RefreshTokenService service;

    @Test
    void createRefreshToken_createsTokenWithExpiry() {
        ReflectionTestUtils.setField(service, "refreshTokenDurationMs", 1000L);

        User user = new User();
        user.setId(7L);

        when(userRepo.findById(7L)).thenReturn(Optional.of(user));
        when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = service.createRefreshToken(7L);

        assertNotNull(token.getToken());
        assertEquals(user, token.getUser());
        assertFalse(token.isRevoked());
        assertTrue(token.getExpiryDate().isAfter(Instant.now().minusSeconds(1)));
        verify(refreshTokenRepo).save(any(RefreshToken.class));
    }

    @Test
    void verifyAndGet_throwsWhenRevoked() {
        User user = new User();
        RefreshToken token = RefreshToken.builder()
                .token("revoked")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(60))
                .revoked(true)
                .build();

        when(refreshTokenRepo.findByToken("revoked")).thenReturn(Optional.of(token));
        when(refreshTokenRepo.findAllByUser(user)).thenReturn(List.of(token));

        assertThrows(TokenException.class, () -> service.verifyAndGet("revoked"));
        verify(refreshTokenRepo).saveAll(any());
    }

    @Test
    void verifyAndGet_throwsWhenExpired() {
        RefreshToken token = RefreshToken.builder()
                .token("expired")
                .expiryDate(Instant.now().minusSeconds(10))
                .revoked(false)
                .build();

        when(refreshTokenRepo.findByToken("expired")).thenReturn(Optional.of(token));

        assertThrows(TokenException.class, () -> service.verifyAndGet("expired"));
        verify(refreshTokenRepo).delete(token);
    }
}

