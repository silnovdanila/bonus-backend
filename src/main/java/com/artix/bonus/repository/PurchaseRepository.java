package com.artix.bonus.repository;

import com.artix.bonus.model.Purchase;
import com.artix.bonus.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Page<Purchase> findByUserOrderByPurchaseDateDesc(User user, Pageable pageable);
    Optional<Purchase> findByIdAndUser(Long id, User user);
}