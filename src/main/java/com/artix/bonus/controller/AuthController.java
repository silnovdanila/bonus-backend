package com.artix.bonus.controller;

import com.artix.bonus.dto.*;
import com.artix.bonus.model.RefreshToken;
import com.artix.bonus.model.User;
import com.artix.bonus.service.RefreshTokenService;
import com.artix.bonus.service.UserService;
import com.artix.bonus.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseWithTokens> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);

        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new AuthResponseWithTokens(
                accessToken,
                refreshToken.getToken(),
                user.getEmail(),
                user.getFullName()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseWithTokens> login(@RequestBody LoginRequest request) {
        User user = userService.login(request);

        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new AuthResponseWithTokens(
                accessToken,
                refreshToken.getToken(),
                user.getEmail(),
                user.getFullName()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
            User user = refreshToken.getUser();
            String newAccessToken = jwtUtils.generateAccessToken(user.getEmail());
            return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Ошибка: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
            refreshToken.setRevoked(true);
            refreshTokenService.save(refreshToken);

            return ResponseEntity.ok("Выход выполнен. Refresh Token отозван.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Ошибка: " + e.getMessage());
        }
    }
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        try {
            User user = userService.findByEmail(request.getEmail());
            String resetCode = String.format("%06d", new java.util.Random().nextInt(999999));
            return ResponseEntity.ok("Ссылка для восстановления пароля отправлена на " + request.getEmail() +
                    " (код: " + resetCode + ")");

        } catch (RuntimeException e) {
            return ResponseEntity.ok("Если пользователь с таким email существует, ссылка для восстановления отправлена");
        }
    }
}