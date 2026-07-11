package com.artix.bonus.controller;

import com.artix.bonus.dto.DeleteAccountRequest;
import com.artix.bonus.dto.PasswordChangeRequest;
import com.artix.bonus.dto.ProfileResponse;
import com.artix.bonus.dto.ProfileUpdateRequest;
import com.artix.bonus.model.User;
import com.artix.bonus.service.UserService;
import com.artix.bonus.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Токен не предоставлен");
            }
            String token = authHeader.substring(7);

            if (!jwtUtils.isTokenValid(token)) {
                return ResponseEntity.status(401).body("Недействительный или просроченный токен");
            }
            String email = jwtUtils.extractEmail(token);
            User user = userService.findByEmail(email);
            return ResponseEntity.ok(new ProfileResponse(
                    user.getEmail(),
                    user.getFullName(),
                    user.getPhone(),
                    user.getBirthDate()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Ошибка: " + e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ProfileUpdateRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Токен не предоставлен");
            }
            String token = authHeader.substring(7);
            if (!jwtUtils.isTokenValid(token)) {
                return ResponseEntity.status(401).body("Недействительный или просроченный токен");
            }
            String email = jwtUtils.extractEmail(token);
            User user = userService.findByEmail(email);
            User updatedUser = userService.updateProfile(user.getId(), request);
            return ResponseEntity.ok(new ProfileResponse(
                    updatedUser.getEmail(),
                    updatedUser.getFullName(),
                    updatedUser.getPhone(),
                    updatedUser.getBirthDate()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Ошибка: " + e.getMessage());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PasswordChangeRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Токен не предоставлен");
            }

            String token = authHeader.substring(7);

            if (!jwtUtils.isTokenValid(token)) {
                return ResponseEntity.status(401).body("Недействительный или просроченный токен");
            }

            String email = jwtUtils.extractEmail(token);
            User user = userService.findByEmail(email);

            userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());

            return ResponseEntity.ok("Пароль успешно изменён");

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Ошибка: " + e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAccount(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody DeleteAccountRequest request) {
        try {
            if (!request.isConfirm()) {
                return ResponseEntity.status(400).body("Необходимо подтверждение удаления аккаунта");
            }

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Токен не предоставлен");
            }

            String token = authHeader.substring(7);

            if (!jwtUtils.isTokenValid(token)) {
                return ResponseEntity.status(401).body("Недействительный или просроченный токен");
            }

            String email = jwtUtils.extractEmail(token);
            User user = userService.findByEmail(email);

            userService.deleteAccount(user.getId(), request.getPassword());

            return ResponseEntity.ok("Аккаунт успешно удалён");

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Ошибка: " + e.getMessage());
        }
    }
}