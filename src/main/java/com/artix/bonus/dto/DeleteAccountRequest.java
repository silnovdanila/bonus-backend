package com.artix.bonus.dto;

import lombok.Data;

@Data
public class DeleteAccountRequest {
    private String password;
    private boolean confirm;
}