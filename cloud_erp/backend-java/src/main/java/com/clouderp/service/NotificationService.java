package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.NotificationDto;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.Notification;
import com.clouderp.model.Payment;
import com.clouderp.model.Product;
import com.clouderp.model.Purchase;
import com.clouderp.model.Sale;
import com.clouderp.repository.NotificationRepository;
import com.clouderp.repository.PaymentRepository;
import com.clouderp.repository.ProductRepository;
import com.clouderp.repository.PurchaseRepository;
import com.clouderp.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               ProductRepository productRepository,
                               SaleRepository saleRepository,
                               PurchaseRepository purchaseRepository) {
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
    }

    // ==========================================
    // NOTIFICATION RETRIEVAL & ACTIONS
    // ==========================================

    @Transactional(readOnly = true)
    public List<NotificationDto.Response> getNotifications(Boolean unreadOnly, String type, String search) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        List<Notification> list;
        if (Boolean.TRUE.equals(unreadOnly)) {
            list = notificationRepository.findByCompanyIdAndIsReadOrderByCreatedAtDesc(companyId, false);
        } else if (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type.trim())) {
            list = notificationRepository.findByCompanyIdAndTypeOrderByCreatedAtDesc(companyId, type.trim().toUpperCase());
        } else {
            list = notificationRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            list = list.stream()
                    .filter(n -> (n.getTitle() != null && n.getTitle().toLowerCase().contains(q)) ||
                                 (n.getMessage() != null && n.getMessage().toLowerCase().contains(q)) ||
                                 (n.getType() != null && n.getType().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificationDto.UnreadCountResponse getUnreadCount() {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        long unread = notificationRepository.countByCompanyIdAndIsReadFalse(companyId);
        return new NotificationDto.UnreadCountResponse(unread);
    }

    public NotificationDto.Response markAsRead(Long id) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Notification notification = notificationRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(OffsetDateTime.now(ZoneOffset.UTC));
            notification = notificationRepository.save(notification);
        }

        return mapToResponse(notification);
    }

    public int markAllAsRead() {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        return notificationRepository.markAllAsReadForCompany(companyId, OffsetDateTime.now(ZoneOffset.UTC));
    }

    public void deleteNotification(Long id) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Notification notification = notificationRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notificationRepository.delete(notification);
    }

    public int clearReadNotifications() {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        return notificationRepository.deleteReadForCompany(companyId);
    }

    // ==========================================
    // NOTIFICATION CREATION & DEDUPLICATION
    // ==========================================

    public Notification createNotification(Long companyId, String title, String message,
                                          String type, String severity, String dedupKey, String actionUrl) {
        if (companyId == null || title == null || title.trim().isEmpty()) {
            return null;
        }

        // Deduplication Check
        if (dedupKey != null && !dedupKey.trim().isEmpty()) {
            String cleanDedupKey = dedupKey.trim();

            // Transactional/One-time notifications: never duplicate once generated
            if (cleanDedupKey.startsWith("SALE_") || cleanDedupKey.startsWith("PAYMENT_")) {
                if (notificationRepository.existsByCompanyIdAndDedupKey(companyId, cleanDedupKey)) {
                    log.debug("Skipping duplicate transactional notification for company {} dedupKey {}", companyId, cleanDedupKey);
                    return null;
                }
            } else {
                // State-based alerts (e.g. STOCK_OUT, STOCK_LOW, OVERDUE, PENDING):
                // Do not duplicate if an unread alert already exists!
                if (notificationRepository.existsByCompanyIdAndDedupKeyAndIsReadFalse(companyId, cleanDedupKey)) {
                    log.debug("Skipping duplicate active state alert for company {} dedupKey {}", companyId, cleanDedupKey);
                    return null;
                }
            }
        }

        Notification notification = new Notification(
                companyId,
                title.trim(),
                message != null ? message.trim() : "",
                type != null ? type.trim().toUpperCase() : "SYSTEM",
                severity != null ? severity.trim().toUpperCase() : "INFO",
                dedupKey != null ? dedupKey.trim() : null,
                actionUrl != null ? actionUrl.trim() : null
        );

        Notification saved = notificationRepository.save(notification);
        log.info("[NOTIFICATION] Created notification id={} company={} type={} title='{}'",
                saved.getId(), companyId, saved.getType(), saved.getTitle());
        return saved;
    }

    // ==========================================
    // SYSTEM SCAN & AUDIT (LOW STOCK, OVERDUE, PENDING)
    // ==========================================

    public NotificationDto.ScanResponse scanAndGenerateSystemAlerts(Long companyId) {
        if (companyId == null) {
            companyId = SecurityUtils.requireCurrentCompanyId();
        }

        int generatedCount = 0;
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // 1. Scan Products for Low-Stock and Out-of-Stock
        List<Product> products = productRepository.findByCompanyId(companyId);
        for (Product product : products) {
            int qty = product.getQuantity() != null ? product.getQuantity() : 0;
            int reorder = product.getReorderLevel() != null ? product.getReorderLevel() : 5;

            if (qty <= 0) {
                // Out of stock
                String dedup = "STOCK_OUT_" + product.getId();
                Notification n = createNotification(
                        companyId,
                        "Out of Stock: " + product.getName(),
                        product.getName() + " is completely out of stock (0 units remaining). Immediate reorder required.",
                        "STOCK_ALERT",
                        "DANGER",
                        dedup,
                        "/inventory"
                );
                if (n != null) generatedCount++;
            } else if (qty <= reorder) {
                // Low stock
                String dedup = "STOCK_LOW_" + product.getId();
                Notification n = createNotification(
                        companyId,
                        "Low Stock Alert: " + product.getName(),
                        product.getName() + " has only " + qty + " units remaining, which is at or below the reorder level (" + reorder + ").",
                        "STOCK_ALERT",
                        "WARNING",
                        dedup,
                        "/inventory"
                );
                if (n != null) generatedCount++;
            } else {
                // Stock is healthy; auto-resolve any unread low/out alerts
                notificationRepository.markDedupKeyAsReadForCompany(companyId, "STOCK_OUT_" + product.getId(), now);
                notificationRepository.markDedupKeyAsReadForCompany(companyId, "STOCK_LOW_" + product.getId(), now);
            }
        }

        // 2. Scan Sales for Overdue Invoices
        List<Sale> sales = saleRepository.findByCompanyId(companyId);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        for (Sale sale : sales) {
            Double balance = sale.getBalanceAmount() != null ? sale.getBalanceAmount() : 0.0;
            LocalDate dueDate = sale.getDueDate();

            if (balance > 0.01 && dueDate != null && dueDate.isBefore(today)) {
                String dedup = "OVERDUE_SALE_" + sale.getId();
                String invNum = sale.getInvoiceNumber() != null ? sale.getInvoiceNumber() : ("INV-" + sale.getId());
                long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);

                Notification n = createNotification(
                        companyId,
                        "Overdue Invoice: " + invNum,
                        "Invoice " + invNum + " has an unpaid balance of ₹" + String.format("%.2f", balance) +
                                " and is " + overdueDays + " day(s) overdue (Due date: " + dueDate + ").",
                        "INVOICE_OVERDUE",
                        "DANGER",
                        dedup,
                        "/receivables"
                );
                if (n != null) generatedCount++;
            } else if (balance <= 0.01) {
                // Fully paid; resolve overdue alert if present
                notificationRepository.markDedupKeyAsReadForCompany(companyId, "OVERDUE_SALE_" + sale.getId(), now);
            }
        }

        // 3. Scan Purchases for Pending Purchase Orders
        List<Purchase> purchases = purchaseRepository.findByCompanyId(companyId);
        for (Purchase purchase : purchases) {
            String status = purchase.getStatus() != null ? purchase.getStatus().toUpperCase() : "";
            if ("PENDING".equals(status)) {
                String dedup = "PURCHASE_PENDING_" + purchase.getId();
                String poNum = purchase.getPurchaseNumber() != null ? purchase.getPurchaseNumber() : ("PO-" + purchase.getId());
                double amount = purchase.getTotalAmount() != null ? purchase.getTotalAmount() : 0.0;

                Notification n = createNotification(
                        companyId,
                        "Pending Purchase Order: " + poNum,
                        "Purchase order " + poNum + " for ₹" + String.format("%.2f", amount) + " is awaiting fulfillment.",
                        "PURCHASE_PENDING",
                        "WARNING",
                        dedup,
                        "/purchases"
                );
                if (n != null) generatedCount++;
            } else {
                // If received or cancelled, resolve pending alert
                notificationRepository.markDedupKeyAsReadForCompany(companyId, "PURCHASE_PENDING_" + purchase.getId(), now);
            }
        }

        long totalUnread = notificationRepository.countByCompanyIdAndIsReadFalse(companyId);
        return new NotificationDto.ScanResponse(
                generatedCount,
                totalUnread,
                "Scan complete: " + generatedCount + " new alert(s) generated. Total unread: " + totalUnread
        );
    }

    // ==========================================
    // EVENT HOOK HANDLERS
    // ==========================================

    public void onSaleCreated(Sale sale, String productName) {
        if (sale == null || sale.getCompanyId() == null) return;
        String invNum = sale.getInvoiceNumber() != null ? sale.getInvoiceNumber() : ("INV-" + sale.getId());
        double total = sale.getTotalAmount() != null ? sale.getTotalAmount() : 0.0;

        createNotification(
                sale.getCompanyId(),
                "New Sale: " + invNum,
                "Recorded new sale invoice " + invNum + " for ₹" + String.format("%.2f", total) +
                        " (" + sale.getQuantity() + "x " + (productName != null ? productName : "Product") + ").",
                "NEW_SALE",
                "INFO",
                "SALE_" + sale.getId(),
                "/sales"
        );
    }

    public void onPaymentReceived(Payment payment, Sale sale, String customerName) {
        if (payment == null || payment.getCompanyId() == null) return;
        String payNum = payment.getPaymentNumber() != null ? payment.getPaymentNumber() : ("PAY-" + payment.getId());
        String invNum = sale != null && sale.getInvoiceNumber() != null ? sale.getInvoiceNumber() : "Invoice";
        double amount = payment.getAmount() != null ? payment.getAmount() : 0.0;
        String method = payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Payment";
        String client = customerName != null ? customerName : "Customer";

        createNotification(
                payment.getCompanyId(),
                "Payment Received: " + payNum,
                "Received " + method + " payment of ₹" + String.format("%.2f", amount) +
                        " for " + invNum + " from " + client + ".",
                "PAYMENT_RECEIVED",
                "SUCCESS",
                "PAYMENT_" + payment.getId(),
                "/receivables"
        );

        // If sale is now paid, auto-resolve any overdue notification
        if (sale != null && sale.getBalanceAmount() != null && sale.getBalanceAmount() <= 0.01) {
            notificationRepository.markDedupKeyAsReadForCompany(payment.getCompanyId(), "OVERDUE_SALE_" + sale.getId(), OffsetDateTime.now(ZoneOffset.UTC));
        }
    }

    public void onPurchaseCreated(Purchase purchase, String supplierName) {
        if (purchase == null || purchase.getCompanyId() == null) return;
        if ("PENDING".equalsIgnoreCase(purchase.getStatus())) {
            String poNum = purchase.getPurchaseNumber() != null ? purchase.getPurchaseNumber() : ("PO-" + purchase.getId());
            double total = purchase.getTotalAmount() != null ? purchase.getTotalAmount() : 0.0;

            createNotification(
                    purchase.getCompanyId(),
                    "Pending Purchase Order: " + poNum,
                    "New purchase order " + poNum + " for ₹" + String.format("%.2f", total) +
                            " from " + (supplierName != null ? supplierName : "Supplier") + " created and pending receipt.",
                    "PURCHASE_PENDING",
                    "WARNING",
                    "PURCHASE_PENDING_" + purchase.getId(),
                    "/purchases"
            );
        }
    }

    public void checkProductStockAlert(Product product) {
        if (product == null || product.getCompanyId() == null) return;
        int qty = product.getQuantity() != null ? product.getQuantity() : 0;
        int reorder = product.getReorderLevel() != null ? product.getReorderLevel() : 5;

        if (qty <= 0) {
            createNotification(
                    product.getCompanyId(),
                    "Out of Stock: " + product.getName(),
                    product.getName() + " is out of stock (0 units). Reorder immediately.",
                    "STOCK_ALERT",
                    "DANGER",
                    "STOCK_OUT_" + product.getId(),
                    "/inventory"
            );
        } else if (qty <= reorder) {
            createNotification(
                    product.getCompanyId(),
                    "Low Stock Alert: " + product.getName(),
                    product.getName() + " has " + qty + " units remaining (reorder level: " + reorder + ").",
                    "STOCK_ALERT",
                    "WARNING",
                    "STOCK_LOW_" + product.getId(),
                    "/inventory"
            );
        } else {
            // Replenished
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            notificationRepository.markDedupKeyAsReadForCompany(product.getCompanyId(), "STOCK_OUT_" + product.getId(), now);
            notificationRepository.markDedupKeyAsReadForCompany(product.getCompanyId(), "STOCK_LOW_" + product.getId(), now);
        }
    }

    // ==========================================
    // MAPPER HELPER
    // ==========================================

    private NotificationDto.Response mapToResponse(Notification n) {
        String createdStr = n.getCreatedAt() != null ? n.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null;
        String readStr = n.getReadAt() != null ? n.getReadAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null;
        String timeAgo = formatTimeAgo(n.getCreatedAt());

        return new NotificationDto.Response(
                n.getId(),
                n.getCompanyId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getSeverity(),
                n.getDedupKey(),
                n.getIsRead(),
                n.getActionUrl(),
                createdStr,
                readStr,
                timeAgo
        );
    }

    private String formatTimeAgo(OffsetDateTime dateTime) {
        if (dateTime == null) return "Just now";
        Duration duration = Duration.between(dateTime, OffsetDateTime.now(ZoneOffset.UTC));
        long seconds = duration.getSeconds();
        if (seconds < 60) return "Just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        if (days < 30) return days + "d ago";
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}
