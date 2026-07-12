package com.artix.bonus.controller;

import com.artix.bonus.dto.PaginationResponse;
import com.artix.bonus.dto.PurchaseDetailResponse;
import com.artix.bonus.dto.PurchaseResponse;
import com.artix.bonus.model.User;
import com.artix.bonus.service.PurchaseService;
import com.artix.bonus.service.UserService;
import com.artix.bonus.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<?> getPurchases(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
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

            Page<PurchaseResponse> purchases = purchaseService.getPurchaseHistory(user.getId(), page, size);

            PaginationResponse<PurchaseResponse> response = new PaginationResponse<>(
                    purchases.getContent(),
                    purchases.getNumber(),
                    purchases.getSize(),
                    purchases.getTotalElements(),
                    purchases.getTotalPages(),
                    purchases.isFirst(),
                    purchases.isLast()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPurchaseDetail(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
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

            PurchaseDetailResponse purchase = purchaseService.getPurchaseDetail(user.getId(), id);

            return ResponseEntity.ok(purchase);

        } catch (Exception e) {
            return ResponseEntity.status(404).body("Ошибка: " + e.getMessage());
        }
    }
}