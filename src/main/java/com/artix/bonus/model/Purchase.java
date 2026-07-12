package com.artix.bonus.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@Data
@NoArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "total_sum", nullable = false)
    private BigDecimal totalSum;

    @Column(name = "bonus_earned")
    private Integer bonusEarned;

    @Column(name = "bonus_burned")
    private Integer bonusBurned;

    @Column(name = "status", nullable = false)
    private String status;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseItem> items = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "bonus_transaction_id")
    private BonusTransaction bonusTransaction;
}