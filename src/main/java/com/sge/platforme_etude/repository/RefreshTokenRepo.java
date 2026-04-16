package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.RefreshToken;
import com.sge.platforme_etude.entite.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    List<RefreshToken> findAllByUser(User user);
}