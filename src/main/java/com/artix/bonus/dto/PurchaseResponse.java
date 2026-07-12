package com.artix.bonus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PurchaseResponse {
    private Long id;
    private String date;
    private String storeName;
    private BigDecimal totalSum;
    private Integer bonusEarned;
    private Integer bonusBurned;
    private String status;
}