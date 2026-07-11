package com.artix.bonus.service;

import com.artix.bonus.dto.BonusTransactionResponse;
import com.artix.bonus.dto.BonusTransactionDetailResponse;
import com.artix.bonus.model.BonusTransaction;
import com.artix.bonus.model.User;
import com.artix.bonus.repository.BonusTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final BonusTransactionRepository bonusTransactionRepository;
    private final UserService userService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Integer getBalance(Long userId) {
        return bonusTransactionRepository.calculateBalance(userId);
    }

    public Page<BonusTransactionResponse> getTransactionHistory(Long userId, int page, int size) {
        User user = userService.findById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BonusTransaction> transactionPage = bonusTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return transactionPage.map(this::mapToResponse);
    }

    public BonusTransactionDetailResponse getTransactionDetail(Long userId, Long transactionId) {
        User user = userService.findById(userId);
        BonusTransaction transaction = bonusTransactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));

        return mapToDetailResponse(transaction);
    }

    public BonusTransaction createTransaction(Long userId, String type, Integer amount, String description, Long relatedPurchaseId) {
        User user = userService.findById(userId);

        BonusTransaction transaction = new BonusTransaction();
        transaction.setUser(user);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setRelatedPurchaseId(relatedPurchaseId);
        transaction.setCreatedAt(LocalDateTime.now());

        return bonusTransactionRepository.save(transaction);
    }

    private BonusTransactionResponse mapToResponse(BonusTransaction transaction) {
        return new BonusTransactionResponse(
                transaction.getId(),
                transaction.getCreatedAt().format(FORMATTER),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getRelatedPurchaseId()
        );
    }

    private BonusTransactionDetailResponse mapToDetailResponse(BonusTransaction transaction) {
        return new BonusTransactionDetailResponse(
                transaction.getId(),
                transaction.getCreatedAt().format(FORMATTER),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getUser().getEmail(),
                transaction.getUser().getFullName(),
                transaction.getRelatedPurchaseId()
        );
    }

    public String getLastUpdated(Long userId) {
        User user = userService.findById(userId);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("createdAt").descending());
        Page<BonusTransaction> lastTransaction = bonusTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        if (lastTransaction.hasContent()) {
            return lastTransaction.getContent().get(0).getCreatedAt().format(FORMATTER);
        }

        return user.getCreatedAt().format(FORMATTER);
    }
}