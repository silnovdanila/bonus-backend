package com.artix.bonus.service;

import com.artix.bonus.dto.BonusTransactionDetailResponse;
import com.artix.bonus.dto.BonusTransactionResponse;
import com.artix.bonus.model.BonusTransaction;
import com.artix.bonus.model.User;
import com.artix.bonus.repository.BonusTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final BonusTransactionRepository bonusTransactionRepository;
    private final UserService userService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Integer getBalance(Long userId) {
        return bonusTransactionRepository.calculateBalance(userId);
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

    public Page<BonusTransactionResponse> getTransactionHistory(
            Long userId,
            int page,
            int size,
            String type,
            String dateFrom,
            String dateTo) {

        User user = userService.findById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        LocalDateTime from = null;
        LocalDateTime to = null;

        if (dateFrom != null && !dateFrom.isEmpty()) {
            from = LocalDate.parse(dateFrom).atStartOfDay();
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            to = LocalDate.parse(dateTo).atTime(23, 59, 59);
        }

        boolean hasType = type != null && !type.isEmpty() && !type.equals("ALL");
        boolean hasDateFrom = from != null;
        boolean hasDateTo = to != null;

        Page<BonusTransaction> transactionPage;

        if (hasType && hasDateFrom && hasDateTo) {
            transactionPage = bonusTransactionRepository
                    .findByUserAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
                            user, type, from, to, pageable
                    );
        } else if (hasType) {
            transactionPage = bonusTransactionRepository
                    .findByUserAndTransactionTypeOrderByCreatedAtDesc(user, type, pageable);
        } else if (hasDateFrom && hasDateTo) {
            transactionPage = bonusTransactionRepository
                    .findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, from, to, pageable);
        } else {
            transactionPage = bonusTransactionRepository
                    .findByUserOrderByCreatedAtDesc(user, pageable);
        }

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
}