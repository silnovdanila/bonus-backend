package com.artix.bonus.service;

import com.artix.bonus.dto.PurchaseDetailResponse;
import com.artix.bonus.dto.PurchaseItemResponse;
import com.artix.bonus.dto.PurchaseResponse;
import com.artix.bonus.model.Purchase;
import com.artix.bonus.model.PurchaseItem;
import com.artix.bonus.model.User;
import com.artix.bonus.repository.PurchaseItemRepository;
import com.artix.bonus.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final UserService userService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Page<PurchaseResponse> getPurchaseHistory(Long userId, int page, int size) {
        User user = userService.findById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseDate").descending());
        Page<Purchase> purchasePage = purchaseRepository.findByUserOrderByPurchaseDateDesc(user, pageable);

        return purchasePage.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PurchaseDetailResponse getPurchaseDetail(Long userId, Long purchaseId) {
        User user = userService.findById(userId);
        Purchase purchase = purchaseRepository.findByIdAndUser(purchaseId, user)
                .orElseThrow(() -> new RuntimeException("Чек не найден"));

        List<PurchaseItem> items = purchaseItemRepository.findByPurchaseId(purchaseId);

        List<PurchaseItemResponse> itemResponses = items.stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());

        return mapToDetailResponse(purchase, itemResponses);
    }

    @Transactional
    public Purchase createPurchase(Long userId, String storeName, BigDecimal totalSum,
                                   Integer bonusEarned, Integer bonusBurned,
                                   List<PurchaseItem> items) {
        User user = userService.findById(userId);

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setPurchaseDate(java.time.LocalDateTime.now());
        purchase.setStoreName(storeName);
        purchase.setTotalSum(totalSum);
        purchase.setBonusEarned(bonusEarned);
        purchase.setBonusBurned(bonusBurned);
        purchase.setStatus("COMPLETED");

        Purchase savedPurchase = purchaseRepository.save(purchase);

        for (PurchaseItem item : items) {
            item.setPurchase(savedPurchase);
            purchaseItemRepository.save(item);
        }

        return savedPurchase;
    }

    private PurchaseResponse mapToResponse(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getPurchaseDate().format(FORMATTER),
                purchase.getStoreName(),
                purchase.getTotalSum(),
                purchase.getBonusEarned(),
                purchase.getBonusBurned(),
                purchase.getStatus()
        );
    }

    private PurchaseDetailResponse mapToDetailResponse(Purchase purchase, List<PurchaseItemResponse> items) {
        return new PurchaseDetailResponse(
                purchase.getId(),
                purchase.getPurchaseDate().format(FORMATTER),
                purchase.getStoreName(),
                purchase.getTotalSum(),
                purchase.getBonusEarned(),
                purchase.getBonusBurned(),
                purchase.getStatus(),
                items
        );
    }

    private PurchaseItemResponse mapToItemResponse(PurchaseItem item) {
        return new PurchaseItemResponse(
                item.getProductName(),
                item.getPrice(),
                item.getQuantity(),
                item.getSum()
        );
    }
}