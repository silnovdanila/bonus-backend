package com.artix.bonus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private String email;
    private String fullName;
    private String phone;
    private String birthDate;
}