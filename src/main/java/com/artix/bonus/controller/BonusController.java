package com.artix.bonus.controller;

import com.artix.bonus.dto.BonusBalanceResponse;
import com.artix.bonus.dto.BonusTransactionDetailResponse;
import com.artix.bonus.dto.BonusTransactionResponse;
import com.artix.bonus.dto.PaginationResponse;
import com.artix.bonus.model.User;
import com.artix.bonus.service.BonusService;
import com.artix.bonus.service.UserService;
import com.artix.bonus.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bonus")
@RequiredArgsConstructor
public class BonusController {

    private final BonusService bonusService;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestHeader("Authorization") String authHeader) {
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

            Integer balance = bonusService.getBalance(user.getId());
            String lastUpdated = bonusService.getLastUpdated(user.getId());

            Double balanceRub = balance * 0.2;

            return ResponseEntity.ok(new BonusBalanceResponse(
                    balance,
                    balanceRub,
                    lastUpdated
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
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

            Page<BonusTransactionResponse> transactions = bonusService.getTransactionHistory(user.getId(), page, size);

            PaginationResponse<BonusTransactionResponse> response = new PaginationResponse<>(
                    transactions.getContent(),
                    transactions.getNumber(),
                    transactions.getSize(),
                    transactions.getTotalElements(),
                    transactions.getTotalPages(),
                    transactions.isFirst(),
                    transactions.isLast()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionDetail(
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

            BonusTransactionDetailResponse transaction = bonusService.getTransactionDetail(user.getId(), id);

            return ResponseEntity.ok(transaction);

        } catch (Exception e) {
            return ResponseEntity.status(404).body("Ошибка: " + e.getMessage());
        }
    }
}