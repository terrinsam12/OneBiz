package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.PurchaseDto;
import com.clouderp.exception.BadRequestException;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.Product;
import com.clouderp.model.Purchase;
import com.clouderp.model.PurchaseItem;
import com.clouderp.model.Supplier;
import com.clouderp.repository.ProductRepository;
import com.clouderp.repository.PurchaseItemRepository;
import com.clouderp.repository.PurchaseRepository;
import com.clouderp.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           PurchaseItemRepository purchaseItemRepository,
                           SupplierRepository supplierRepository,
                           ProductRepository productRepository,
                           NotificationService notificationService) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    public PurchaseDto.Response createPurchase(PurchaseDto.Create request) {
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        Supplier supplier = supplierRepository.findByIdAndCompanyId(request.getSupplierId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found for this company"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Purchase must contain at least one product item");
        }

        String initialStatus = StringUtils.hasText(request.getStatus())
                ? request.getStatus().trim().toUpperCase()
                : "PENDING";

        if (!"PENDING".equals(initialStatus) && !"RECEIVED".equals(initialStatus)) {
            throw new BadRequestException("Initial purchase status must be PENDING or RECEIVED");
        }

        long orderCount = purchaseRepository.countByCompanyId(companyId) + 1;
        String purchaseNumber = String.format("PO-%05d", orderCount);

        // Pre-validate products & calculate totals
        double grandTotal = 0.0;
        List<ValidatedItem> validatedItems = new ArrayList<>();

        for (PurchaseDto.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndCompanyId(itemReq.getProductId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + itemReq.getProductId() + " not found"));

            double lineTotal = itemReq.getQuantity() * itemReq.getUnitPrice();
            grandTotal += lineTotal;
            validatedItems.add(new ValidatedItem(product, itemReq.getQuantity(), itemReq.getUnitPrice(), lineTotal));
        }

        // Create Purchase header
        Purchase purchase = new Purchase(
                companyId,
                supplier.getId(),
                purchaseNumber,
                initialStatus,
                grandTotal,
                request.getNotes() != null ? request.getNotes().trim() : ""
        );
        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Save items & increase stock ONLY if initial status is RECEIVED
        List<PurchaseItem> savedItems = new ArrayList<>();
        boolean isReceived = "RECEIVED".equals(initialStatus);

        for (ValidatedItem vi : validatedItems) {
            PurchaseItem pi = new PurchaseItem(
                    savedPurchase.getId(),
                    vi.product.getId(),
                    vi.product.getName(),
                    vi.quantity,
                    vi.unitPrice,
                    vi.lineTotal
            );
            savedItems.add(purchaseItemRepository.save(pi));

            if (isReceived) {
                // Increase stock
                vi.product.setQuantity(vi.product.getQuantity() + vi.quantity);
                vi.product.setCostPrice(vi.unitPrice);
                productRepository.save(vi.product);
            }
        }

        // Trigger notification if pending
        notificationService.onPurchaseCreated(savedPurchase, supplier.getName());

        return toResponse(savedPurchase, supplier.getName(), savedItems);
    }

    @Transactional(readOnly = true)
    public List<PurchaseDto.Response> getPurchases(Long supplierId, String status, String search) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        List<Purchase> purchases = purchaseRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);

        Map<Long, String> supplierNames = supplierRepository.findByCompanyId(companyId).stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getName, (a, b) -> a));

        return purchases.stream()
                .filter(p -> {
                    if (supplierId != null && !supplierId.equals(p.getSupplierId())) {
                        return false;
                    }
                    if (StringUtils.hasText(status) && !status.equalsIgnoreCase(p.getStatus())) {
                        return false;
                    }
                    if (StringUtils.hasText(search)) {
                        String q = search.trim().toLowerCase();
                        String sName = supplierNames.getOrDefault(p.getSupplierId(), "").toLowerCase();
                        boolean matchNum = p.getPurchaseNumber() != null && p.getPurchaseNumber().toLowerCase().contains(q);
                        boolean matchNotes = p.getNotes() != null && p.getNotes().toLowerCase().contains(q);
                        boolean matchSupplier = sName.contains(q);
                        if (!matchNum && !matchNotes && !matchSupplier) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(p -> {
                    String supplierName = supplierNames.getOrDefault(p.getSupplierId(), "Unknown Supplier");
                    List<PurchaseItem> items = purchaseItemRepository.findByPurchaseId(p.getId());
                    return toResponse(p, supplierName, items);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PurchaseDto.Response getPurchaseById(Long id) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Purchase purchase = purchaseRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));

        String supplierName = supplierRepository.findByIdAndCompanyId(purchase.getSupplierId(), companyId)
                .map(Supplier::getName)
                .orElse("Unknown Supplier");

        List<PurchaseItem> items = purchaseItemRepository.findByPurchaseId(purchase.getId());
        return toResponse(purchase, supplierName, items);
    }

    public PurchaseDto.Response updatePurchaseStatus(Long id, String newStatusInput) {
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        if (!StringUtils.hasText(newStatusInput)) {
            throw new BadRequestException("Status is required");
        }

        String targetStatus = newStatusInput.trim().toUpperCase();
        if (!Arrays.asList("PENDING", "RECEIVED", "CANCELLED").contains(targetStatus)) {
            throw new BadRequestException("Invalid status: " + targetStatus + ". Allowed statuses: PENDING, RECEIVED, CANCELLED");
        }

        Purchase purchase = purchaseRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));

        String oldStatus = purchase.getStatus();

        // Prevent duplicate updates if already in target status
        if (oldStatus.equalsIgnoreCase(targetStatus)) {
            String supplierName = supplierRepository.findByIdAndCompanyId(purchase.getSupplierId(), companyId)
                    .map(Supplier::getName).orElse("Unknown Supplier");
            List<PurchaseItem> items = purchaseItemRepository.findByPurchaseId(purchase.getId());
            return toResponse(purchase, supplierName, items);
        }

        List<PurchaseItem> items = purchaseItemRepository.findByPurchaseId(purchase.getId());

        // Stock Transitions:
        // 1. Moving to RECEIVED from non-RECEIVED -> Increase stock
        if ("RECEIVED".equals(targetStatus) && !"RECEIVED".equals(oldStatus)) {
            for (PurchaseItem pi : items) {
                productRepository.findByIdAndCompanyId(pi.getProductId(), companyId).ifPresent(prod -> {
                    prod.setQuantity(prod.getQuantity() + pi.getQuantity());
                    prod.setCostPrice(pi.getUnitPrice());
                    productRepository.save(prod);
                });
            }
            purchase.setReceivedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
        // 2. Moving from RECEIVED to CANCELLED -> Revert stock
        else if ("RECEIVED".equals(oldStatus) && "CANCELLED".equals(targetStatus)) {
            for (PurchaseItem pi : items) {
                productRepository.findByIdAndCompanyId(pi.getProductId(), companyId).ifPresent(prod -> {
                    int newQty = Math.max(0, prod.getQuantity() - pi.getQuantity());
                    prod.setQuantity(newQty);
                    productRepository.save(prod);
                });
            }
        }

        purchase.setStatus(targetStatus);
        Purchase updated = purchaseRepository.save(purchase);

        // Update alerts for this company
        notificationService.scanAndGenerateSystemAlerts(companyId);

        String supplierName = supplierRepository.findByIdAndCompanyId(updated.getSupplierId(), companyId)
                .map(Supplier::getName).orElse("Unknown Supplier");

        return toResponse(updated, supplierName, items);
    }

    @Transactional(readOnly = true)
    public PurchaseDto.SummaryResponse getSummary() {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        List<Purchase> purchases = purchaseRepository.findByCompanyId(companyId);

        long totalOrders = purchases.size();
        long pendingOrders = purchases.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getStatus())).count();
        long receivedOrders = purchases.stream().filter(p -> "RECEIVED".equalsIgnoreCase(p.getStatus())).count();
        long cancelledOrders = purchases.stream().filter(p -> "CANCELLED".equalsIgnoreCase(p.getStatus())).count();
        double totalSpend = purchases.stream()
                .filter(p -> "RECEIVED".equalsIgnoreCase(p.getStatus()))
                .mapToDouble(Purchase::getTotalAmount)
                .sum();

        return new PurchaseDto.SummaryResponse(totalOrders, pendingOrders, receivedOrders, cancelledOrders, totalSpend);
    }

    private PurchaseDto.Response toResponse(Purchase p, String supplierName, List<PurchaseItem> items) {
        PurchaseDto.Response resp = new PurchaseDto.Response();
        resp.setId(p.getId());
        resp.setCompanyId(p.getCompanyId());
        resp.setSupplierId(p.getSupplierId());
        resp.setSupplierName(supplierName);
        resp.setPurchaseNumber(p.getPurchaseNumber());
        resp.setStatus(p.getStatus());
        resp.setTotalAmount(p.getTotalAmount());
        resp.setNotes(p.getNotes());
        resp.setCreatedAt(p.getCreatedAt());
        resp.setReceivedAt(p.getReceivedAt());

        List<PurchaseDto.ItemResponse> itemDtos = items.stream().map(it -> new PurchaseDto.ItemResponse(
                it.getId(),
                it.getPurchaseId(),
                it.getProductId(),
                it.getProductName(),
                it.getQuantity(),
                it.getUnitPrice(),
                it.getTotalPrice()
        )).collect(Collectors.toList());

        resp.setItems(itemDtos);
        return resp;
    }

    private static class ValidatedItem {
        final Product product;
        final int quantity;
        final double unitPrice;
        final double lineTotal;

        ValidatedItem(Product product, int quantity, double unitPrice, double lineTotal) {
            this.product = product;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }
    }
}
