package com.artix.bonus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PurchaseItemResponse {
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal sum;
}