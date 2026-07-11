package com.artix.bonus.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
    private String birthDate;
}