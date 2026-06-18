package com.vaidyavatika.repository;

import com.vaidyavatika.model.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    Optional<PendingRegistration> findByToken(String token);
    Optional<PendingRegistration> findByEmail(String email);
    boolean existsByEmail(String email);
    @Modifying
    @Transactional
    void deleteByEmail(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM PendingRegistration p WHERE p.expiresAt < :now")
    void deleteExpired(LocalDateTime now);
}