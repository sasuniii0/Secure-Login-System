package com.example.secure_login.repository;

import com.example.secure_login.entity.OtpToken;
import com.example.secure_login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, String> {
    Optional<OtpToken> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);

    Optional<OtpToken> findByUserAndOtpCodeAndUsedFalse(User user, String otpCode);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);

    @Modifying
    @Transactional
    void deleteAllByUser(User user);
}
