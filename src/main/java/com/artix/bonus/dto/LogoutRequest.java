package com.artix.bonus.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}