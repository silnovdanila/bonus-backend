package com.artix.bonus.repository;

import com.artix.bonus.model.BonusTransaction;
import com.artix.bonus.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BonusTransactionRepository extends JpaRepository<BonusTransaction, Long> {

    Page<BonusTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<BonusTransaction> findByIdAndUser(Long id, User user);

    Page<BonusTransaction> findByUserAndTransactionTypeOrderByCreatedAtDesc(
            User user,
            String transactionType,
            Pageable pageable
    );

    Page<BonusTransaction> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            User user,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    );

    Page<BonusTransaction> findByUserAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            User user,
            String transactionType,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    );

    @Query(value = "SELECT COALESCE(SUM(CASE WHEN transaction_type = 'EARN' THEN amount ELSE 0 END) - " +
            "SUM(CASE WHEN transaction_type IN ('BURN', 'EXPIRE') THEN amount ELSE 0 END), 0) " +
            "FROM bonus_transactions WHERE user_id = :userId", nativeQuery = true)
    Integer calculateBalance(Long userId);
}