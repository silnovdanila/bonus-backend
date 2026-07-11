package com.artix.bonus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BonusTransactionDetailResponse {
    private Long id;
    private String date;
    private String type;
    private Integer amount;
    private String description;
    private String userEmail;
    private String userFullName;
    private Long relatedPurchaseId;
}