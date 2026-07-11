package com.artix.bonus.repository;

import com.artix.bonus.model.BonusTransaction;
import com.artix.bonus.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BonusTransactionRepository extends JpaRepository<BonusTransaction, Long> {
    Page<BonusTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Optional<BonusTransaction> findByIdAndUser(Long id, User user);
    @org.springframework.data.jpa.repository.Query(
            value = "SELECT COALESCE(SUM(CASE WHEN transaction_type = 'EARN' THEN amount ELSE 0 END) - " +
                    "SUM(CASE WHEN transaction_type IN ('BURN', 'EXPIRE') THEN amount ELSE 0 END), 0) " +
                    "FROM bonus_transactions WHERE user_id = :userId",
            nativeQuery = true
    )
    Integer calculateBalance(Long userId);
}