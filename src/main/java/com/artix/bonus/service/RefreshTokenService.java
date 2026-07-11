package com.artix.bonus.service;

import com.artix.bonus.model.RefreshToken;
import com.artix.bonus.model.User;
import com.artix.bonus.repository.RefreshTokenRepository;
import com.artix.bonus.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;

    public RefreshToken createRefreshToken(User user) {
        String token = jwtUtils.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = new RefreshToken(
                token,
                user,
                LocalDateTime.now().plusDays(7)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh Token не найден"));
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh Token отозван");
        }

        if (refreshToken.isExpired()) {
            throw new RuntimeException("Refresh Token истёк");
        }

        if (!jwtUtils.isTokenValid(token)) {
            throw new RuntimeException("Недействительный Refresh Token");
        }

        return refreshToken;
    }

    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }
}