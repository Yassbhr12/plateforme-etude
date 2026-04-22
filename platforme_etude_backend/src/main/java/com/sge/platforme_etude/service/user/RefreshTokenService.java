package com.sge.platforme_etude.service.user;


import com.sge.platforme_etude.entite.RefreshToken;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.exceptions.TokenException;
import com.sge.platforme_etude.repository.RefreshTokenRepo;
import com.sge.platforme_etude.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepo refreshTokenRepo;
    private final UserRepo userRepo;

    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepo.save(token);
    }

    public RefreshToken verifyAndGet(String tokenValue) {
        RefreshToken token = refreshTokenRepo.findByToken(tokenValue)
                .orElseThrow(() -> new TokenException("Refresh token introuvable"));

        if (token.isRevoked()) {
            revokeAllUserTokens(token.getUser());
            throw new TokenException("Token révoqué — reconnectez-vous");
        }

        if (token.isExpired()) {
            refreshTokenRepo.delete(token);
            throw new TokenException("Token expiré — reconnectez-vous");
        }

        return token;
    }

    public RefreshToken rotate(RefreshToken oldToken) {
        oldToken.setRevoked(true);
        refreshTokenRepo.save(oldToken);
        return createRefreshToken(oldToken.getUser().getId());
    }

    public void revokeAllUserTokens(User user) {
        List<RefreshToken> tokens = refreshTokenRepo.findAllByUser(user);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepo.saveAll(tokens);
    }
}
