package com.artix.bonus.controller;

import com.artix.bonus.config.JwtUtils;
import com.artix.bonus.dto.AuthResponse;
import com.artix.bonus.dto.LoginRequest;
import com.artix.bonus.dto.RegisterRequest;
import com.artix.bonus.model.User;
import com.artix.bonus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = userService.login(request);
        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Токен не предоставлен");
            }
            String token = authHeader.substring(7);
            String email = jwtUtils.extractEmail(token);
            User user = userService.findByEmail(email);

            return ResponseEntity.ok("Привет, " + user.getFullName() + "! Твой email: " + user.getEmail());

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Неверный или просроченный токен");
        }
    }
}