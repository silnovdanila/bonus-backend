package com.artix.bonus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BonusBalanceResponse {
    private Integer balance;
    private Double balanceRub;
    private String lastUpdated;
}