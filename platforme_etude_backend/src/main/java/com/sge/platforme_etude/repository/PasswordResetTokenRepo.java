package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.PasswordResetToken;
import com.sge.platforme_etude.entite.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findFirstByUserAndUsedAtIsNullOrderByCreatedAtDesc(User user);

    List<PasswordResetToken> findAllByUserAndUsedAtIsNull(User user);
}
