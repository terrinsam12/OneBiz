package com.clouderp.service;

import com.clouderp.dto.ReportDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportExportService {

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // =========================================================================
    // EXCEL / SPREADSHEET (SpreadsheetML XML .xls)
    // =========================================================================

    public byte[] generateExcel(String reportType, Object reportData) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<?mso-application progid=\"Excel.Sheet\"?>\n");
        xml.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n");
        xml.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n");
        xml.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n");
        xml.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");

        // Styles
        xml.append(" <Styles>\n");
        xml.append("  <Style ss:ID=\"Default\" ss:Name=\"Normal\"><Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Color=\"#000000\"/></Style>\n");
        xml.append("  <Style ss:ID=\"Title\"><Font ss:FontName=\"Calibri\" ss:Size=\"16\" ss:Bold=\"1\" ss:Color=\"#1E3A8A\"/></Style>\n");
        xml.append("  <Style ss:ID=\"SubTitle\"><Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Italic=\"1\" ss:Color=\"#4B5563\"/></Style>\n");
        xml.append("  <Style ss:ID=\"Header\"><Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/><Interior ss:Color=\"#1E293B\" ss:Pattern=\"Solid\"/><Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#000000\"/></Borders></Style>\n");
        xml.append("  <Style ss:ID=\"SectionHeader\"><Font ss:FontName=\"Calibri\" ss:Size=\"12\" ss:Bold=\"1\" ss:Color=\"#1E3A8A\"/><Interior ss:Color=\"#E2E8F0\" ss:Pattern=\"Solid\"/></Style>\n");
        xml.append("  <Style ss:ID=\"KpiLabel\"><Font ss:FontName=\"Calibri\" ss:Size=\"10\" ss:Color=\"#64748B\"/><Interior ss:Color=\"#F1F5F9\" ss:Pattern=\"Solid\"/></Style>\n");
        xml.append("  <Style ss:ID=\"KpiValue\"><Font ss:FontName=\"Calibri\" ss:Size=\"12\" ss:Bold=\"1\" ss:Color=\"#0F172A\"/><Interior ss:Color=\"#F1F5F9\" ss:Pattern=\"Solid\"/></Style>\n");
        xml.append("  <Style ss:ID=\"TotalRow\"><Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Bold=\"1\" ss:Color=\"#000000\"/><Interior ss:Color=\"#E2E8F0\" ss:Pattern=\"Solid\"/><Borders><Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#000000\"/><Border ss:Position=\"Bottom\" ss:LineStyle=\"Double\" ss:Weight=\"3\" ss:Color=\"#000000\"/></Borders></Style>\n");
        xml.append("  <Style ss:ID=\"Currency\"><NumberFormat ss:Format=\"#,##0.00\"/></Style>\n");
        xml.append("  <Style ss:ID=\"CurrencyTotal\"><Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Bold=\"1\" ss:Color=\"#000000\"/><Interior ss:Color=\"#E2E8F0\" ss:Pattern=\"Solid\"/><NumberFormat ss:Format=\"#,##0.00\"/><Borders><Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#000000\"/><Border ss:Position=\"Bottom\" ss:LineStyle=\"Double\" ss:Weight=\"3\" ss:Color=\"#000000\"/></Borders></Style>\n");
        xml.append(" </Styles>\n");

        if (reportData instanceof ReportDto.SalesReport) {
            renderSalesExcel(xml, (ReportDto.SalesReport) reportData);
        } else if (reportData instanceof ReportDto.PurchaseReport) {
            renderPurchaseExcel(xml, (ReportDto.PurchaseReport) reportData);
        } else if (reportData instanceof ReportDto.InventoryReport) {
            renderInventoryExcel(xml, (ReportDto.InventoryReport) reportData);
        } else if (reportData instanceof ReportDto.ExpenseReport) {
            renderExpenseExcel(xml, (ReportDto.ExpenseReport) reportData);
        } else if (reportData instanceof ReportDto.ProfitLossReport) {
            renderProfitLossExcel(xml, (ReportDto.ProfitLossReport) reportData);
        } else if (reportData instanceof ReportDto.ReceivablesReport) {
            renderReceivablesExcel(xml, (ReportDto.ReceivablesReport) reportData);
        }

        xml.append("</Workbook>\n");
        return xml.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void renderSalesExcel(StringBuilder xml, ReportDto.SalesReport report) {
        xml.append(" <Worksheet ss:Name=\"Sales Report\">\n  <Table>\n");
        appendExcelRow(xml, "Title", report.getCompanyName() + " - SALES REPORT");
        appendExcelRow(xml, "SubTitle", "Period: " + (report.getFilter() != null ? report.getFilter().getPeriodLabel() : "All Time") + " | Generated: " + LocalDateTime.now().format(TIMESTAMP_FMT));
        appendEmptyRow(xml);

        // KPI Summary Block
        appendExcelRow(xml, "SectionHeader", "EXECUTIVE KPI SUMMARY");
        appendExcel2Col(xml, "KpiLabel", "Total Revenue", "KpiValue", "Rs. " + formatCurrency(report.getTotalRevenue()));
        appendExcel2Col(xml, "KpiLabel", "Total Transactions", "KpiValue", String.valueOf(report.getTotalTransactions()));
        appendExcel2Col(xml, "KpiLabel", "Total Units Sold", "KpiValue", String.valueOf(report.getTotalUnitsSold()));
        appendExcel2Col(xml, "KpiLabel", "Average Order Value", "KpiValue", "Rs. " + formatCurrency(report.getAverageOrderValue()));
        appendEmptyRow(xml);

        // Top Products
        if (report.getTopProducts() != null && !report.getTopProducts().isEmpty()) {
            appendExcelRow(xml, "SectionHeader", "TOP SELLING PRODUCTS");
            appendExcelCells(xml, "Header", List.of("Product Name", "Units Sold", "Total Revenue (Rs.)", "Avg Selling Price (Rs.)"));
            for (ReportDto.TopProductItem item : report.getTopProducts()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(item.getProductName()),
                        String.valueOf(item.getUnitsSold()),
                        formatCurrency(item.getTotalRevenue()),
                        formatCurrency(item.getAvgSellingPrice())
                ));
            }
            appendEmptyRow(xml);
        }

        // Transactions Table
        appendExcelRow(xml, "SectionHeader", "DETAILED SALES TRANSACTIONS");
        appendExcelCells(xml, "Header", List.of("Invoice #", "Date", "Customer", "Product", "Qty", "Unit Price (Rs.)", "Total Amount (Rs.)", "Paid (Rs.)", "Balance (Rs.)", "Status"));
        if (report.getTransactions() != null) {
            for (ReportDto.SaleTransactionItem s : report.getTransactions()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(s.getInvoiceNumber()),
                        escapeXml(s.getSaleDate()),
                        escapeXml(s.getCustomerName()),
                        escapeXml(s.getProductName()),
                        String.valueOf(s.getQuantity()),
                        formatCurrency(s.getUnitPrice()),
                        formatCurrency(s.getTotalAmount()),
                        formatCurrency(s.getPaidAmount()),
                        formatCurrency(s.getBalanceAmount()),
                        escapeXml(s.getPaymentStatus())
                ));
            }
            // Totals row
            appendExcelCells(xml, "TotalRow", List.of(
                    "TOTALS", "", "", "",
                    String.valueOf(report.getTotalUnitsSold()),
                    "",
                    formatCurrency(report.getTotalRevenue()),
                    "", "", ""
            ));
        }

        xml.append("  </Table>\n </Worksheet>\n");
    }

    private void renderPurchaseExcel(StringBuilder xml, ReportDto.PurchaseReport report) {
        xml.append(" <Worksheet ss:Name=\"Purchase Report\">\n  <Table>\n");
        appendExcelRow(xml, "Title", report.getCompanyName() + " - PROCUREMENT & PURCHASE REPORT");
        appendExcelRow(xml, "SubTitle", "Period: " + (report.getFilter() != null ? report.getFilter().getPeriodLabel() : "All Time") + " | Generated: " + LocalDateTime.now().format(TIMESTAMP_FMT));
        appendEmptyRow(xml);

        // KPI Summary Block
        appendExcelRow(xml, "SectionHeader", "EXECUTIVE PROCUREMENT SUMMARY");
        appendExcel2Col(xml, "KpiLabel", "Total Procurement Spending", "KpiValue", "Rs. " + formatCurrency(report.getTotalProcurementSpending()));
        appendExcel2Col(xml, "KpiLabel", "Total Purchase Orders", "KpiValue", String.valueOf(report.getTotalPurchasesCount()));
        appendExcel2Col(xml, "KpiLabel", "Received Orders Count", "KpiValue", String.valueOf(report.getReceivedPurchasesCount()));
        appendExcel2Col(xml, "KpiLabel", "Pending Orders Count", "KpiValue", String.valueOf(report.getPendingPurchasesCount()) + " (Rs. " + formatCurrency(report.getPendingAmount()) + ")");
        appendEmptyRow(xml);

        // Supplier Breakdown
        if (report.getSupplierBreakdown() != null && !report.getSupplierBreakdown().isEmpty()) {
            appendExcelRow(xml, "SectionHeader", "SUPPLIER-WISE PROCUREMENT BREAKDOWN");
            appendExcelCells(xml, "Header", List.of("Supplier Name", "Contact Person", "Phone", "Total Orders", "Received Orders", "Total Spending (Rs.)", "Last Order Date"));
            for (ReportDto.SupplierPurchaseItem item : report.getSupplierBreakdown()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(item.getSupplierName()),
                        escapeXml(item.getContactPerson()),
                        escapeXml(item.getPhone()),
                        String.valueOf(item.getTotalOrders()),
                        String.valueOf(item.getReceivedOrders()),
                        formatCurrency(item.getTotalSpending()),
                        escapeXml(item.getLastOrderDate())
                ));
            }
            appendEmptyRow(xml);
        }

        // Purchases Table
        appendExcelRow(xml, "SectionHeader", "PURCHASE ORDERS DIRECTORY");
        appendExcelCells(xml, "Header", List.of("PO #", "Created Date", "Supplier Name", "Status", "Total Amount (Rs.)", "Received Date"));
        if (report.getPurchases() != null) {
            for (ReportDto.PurchaseReportItem p : report.getPurchases()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(p.getPurchaseNumber()),
                        escapeXml(p.getCreatedAt()),
                        escapeXml(p.getSupplierName()),
                        escapeXml(p.getStatus()),
                        formatCurrency(p.getTotalAmount()),
                        escapeXml(p.getReceivedAt())
                ));
            }
            appendExcelCells(xml, "TotalRow", List.of("TOTAL RECEIVED SPENDING", "", "", "", formatCurrency(report.getTotalProcurementSpending()), ""));
        }

        xml.append("  </Table>\n </Worksheet>\n");
    }

    private void renderInventoryExcel(StringBuilder xml, ReportDto.InventoryReport report) {
        xml.append(" <Worksheet ss:Name=\"Inventory Report\">\n  <Table>\n");
        appendExcelRow(xml, "Title", report.getCompanyName() + " - INVENTORY & VALUATION REPORT");
        appendExcelRow(xml, "SubTitle", "As of Date: " + escapeXml(report.getGeneratedDate()));
        appendEmptyRow(xml);

        // KPI Summary Block
        appendExcelRow(xml, "SectionHeader", "INVENTORY SUMMARY");
        appendExcel2Col(xml, "KpiLabel", "Total Tracked Products", "KpiValue", String.valueOf(report.getTotalProducts()));
        appendExcel2Col(xml, "KpiLabel", "Total Stock Units on Hand", "KpiValue", String.valueOf(report.getTotalStockUnits()));
        appendExcel2Col(xml, "KpiLabel", "Inventory Valuation (Cost)", "KpiValue", "Rs. " + formatCurrency(report.getTotalValuationCost()));
        appendExcel2Col(xml, "KpiLabel", "Inventory Valuation (Retail)", "KpiValue", "Rs. " + formatCurrency(report.getTotalValuationRetail()));
        appendExcel2Col(xml, "KpiLabel", "Potential Gross Margin", "KpiValue", "Rs. " + formatCurrency(report.getPotentialGrossMargin()));
        appendExcel2Col(xml, "KpiLabel", "Low Stock Items Alert", "KpiValue", String.valueOf(report.getLowStockCount()));
        appendEmptyRow(xml);

        // Low stock alerts
        if (report.getLowStockAlerts() != null && !report.getLowStockAlerts().isEmpty()) {
            appendExcelRow(xml, "SectionHeader", "CRITICAL STOCK REORDER ALERTS");
            appendExcelCells(xml, "Header", List.of("Product Name", "Current Stock", "Reorder Level", "Cost Price (Rs.)", "Stock Status"));
            for (ReportDto.ProductInventoryItem item : report.getLowStockAlerts()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(item.getName()),
                        String.valueOf(item.getQuantity()),
                        String.valueOf(item.getReorderLevel()),
                        formatCurrency(item.getCostPrice()),
                        escapeXml(item.getStockStatus())
                ));
            }
            appendEmptyRow(xml);
        }

        // Full inventory
        appendExcelRow(xml, "SectionHeader", "PRODUCT INVENTORY DIRECTORY");
        appendExcelCells(xml, "Header", List.of("Product Name", "Cost Price (Rs.)", "Selling Price (Rs.)", "Current Stock", "Reorder Level", "Valuation Cost (Rs.)", "Valuation Retail (Rs.)", "Status"));
        if (report.getProducts() != null) {
            for (ReportDto.ProductInventoryItem p : report.getProducts()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(p.getName()),
                        formatCurrency(p.getCostPrice()),
                        formatCurrency(p.getSellingPrice()),
                        String.valueOf(p.getQuantity()),
                        String.valueOf(p.getReorderLevel()),
                        formatCurrency(p.getValuationCost()),
                        formatCurrency(p.getValuationRetail()),
                        escapeXml(p.getStockStatus())
                ));
            }
            appendExcelCells(xml, "TotalRow", List.of(
                    "TOTALS", "", "",
                    String.valueOf(report.getTotalStockUnits()),
                    "",
                    formatCurrency(report.getTotalValuationCost()),
                    formatCurrency(report.getTotalValuationRetail()),
                    ""
            ));
        }

        xml.append("  </Table>\n </Worksheet>\n");
    }

    private void renderExpenseExcel(StringBuilder xml, ReportDto.ExpenseReport report) {
        xml.append(" <Worksheet ss:Name=\"Expense Report\">\n  <Table>\n");
        appendExcelRow(xml, "Title", report.getCompanyName() + " - OPERATING EXPENSES REPORT");
        appendExcelRow(xml, "SubTitle", "Period: " + (report.getFilter() != null ? report.getFilter().getPeriodLabel() : "All Time") + " | Generated: " + LocalDateTime.now().format(TIMESTAMP_FMT));
        appendEmptyRow(xml);

        // KPI Summary Block
        appendExcelRow(xml, "SectionHeader", "EXPENSE SUMMARY");
        appendExcel2Col(xml, "KpiLabel", "Total Operating Expenses", "KpiValue", "Rs. " + formatCurrency(report.getTotalExpenses()));
        appendExcel2Col(xml, "KpiLabel", "Total Transactions Count", "KpiValue", String.valueOf(report.getTotalTransactions()));
        appendExcel2Col(xml, "KpiLabel", "Average Expense Amount", "KpiValue", "Rs. " + formatCurrency(report.getAverageExpense()));
        appendEmptyRow(xml);

        // Category Breakdown
        if (report.getCategoryBreakdown() != null && !report.getCategoryBreakdown().isEmpty()) {
            appendExcelRow(xml, "SectionHeader", "CATEGORY-WISE EXPENSES BREAKDOWN");
            appendExcelCells(xml, "Header", List.of("Category", "Total Spending (Rs.)", "Share of Total (%)", "Transactions Count"));
            for (ReportDto.CategoryExpenseItem cat : report.getCategoryBreakdown()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(cat.getCategory()),
                        formatCurrency(cat.getAmount()),
                        formatCurrency(cat.getPercentageOfTotal()) + "%",
                        String.valueOf(cat.getCount())
                ));
            }
            appendEmptyRow(xml);
        }

        // Expenses List
        appendExcelRow(xml, "SectionHeader", "EXPENSES RECORD LOG");
        appendExcelCells(xml, "Header", List.of("Date", "Expense Name", "Category", "Payment Method", "Amount (Rs.)", "Description"));
        if (report.getExpenses() != null) {
            for (ReportDto.ExpenseReportItem e : report.getExpenses()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(e.getExpenseDate()),
                        escapeXml(e.getName()),
                        escapeXml(e.getCategory()),
                        escapeXml(e.getPaymentMethod()),
                        formatCurrency(e.getAmount()),
                        escapeXml(e.getDescription())
                ));
            }
            appendExcelCells(xml, "TotalRow", List.of("TOTAL OPERATING EXPENSES", "", "", "", formatCurrency(report.getTotalExpenses()), ""));
        }

        xml.append("  </Table>\n </Worksheet>\n");
    }

    private void renderProfitLossExcel(StringBuilder xml, ReportDto.ProfitLossReport report) {
        xml.append(" <Worksheet ss:Name=\"P&amp;L Statement\">\n  <Table>\n");
        appendExcelRow(xml, "Title", report.getCompanyName() + " - PROFIT &amp; LOSS STATEMENT");
        appendExcelRow(xml, "SubTitle", "Period: " + (report.getFilter() != null ? report.getFilter().getPeriodLabel() : "All Time") + " | Generated: " + LocalDateTime.now().format(TIMESTAMP_FMT));
        appendEmptyRow(xml);

        // Core Financial Overview Table
        appendExcelRow(xml, "SectionHeader", "INCOME &amp; PROFITABILITY SUMMARY");
        appendExcelCells(xml, "Header", List.of("Financial Line Item", "Amount (Rs.)", "% of Revenue"));
        appendExcelCells(xml, null, List.of("Gross Sales Revenue", formatCurrency(report.getGrossSalesRevenue()), "100.0%"));
        appendExcelCells(xml, null, List.of("Cost of Goods Sold (COGS)", "-" + formatCurrency(report.getCostOfGoodsSold()), formatPercent(report.getCostOfGoodsSold(), report.getGrossSalesRevenue())));
        appendExcelCells(xml, "TotalRow", List.of("GROSS PROFIT", formatCurrency(report.getGrossProfit()), formatCurrency(report.getGrossProfitMarginPct()) + "%"));
        appendEmptyRow(xml);

        appendExcelRow(xml, "SectionHeader", "OPERATING EXPENSES BREAKDOWN");
        appendExcelCells(xml, "Header", List.of("Expense Category", "Amount (Rs.)", "% of Revenue"));
        if (report.getOperatingExpensesByCategory() != null) {
            for (ReportDto.CategoryExpenseItem cat : report.getOperatingExpensesByCategory()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(cat.getCategory()),
                        "-" + formatCurrency(cat.getAmount()),
                        formatPercent(cat.getAmount(), report.getGrossSalesRevenue())
                ));
            }
        }
        appendExcelCells(xml, "TotalRow", List.of("TOTAL OPERATING EXPENSES", "-" + formatCurrency(report.getTotalOperatingExpenses()), formatPercent(report.getTotalOperatingExpenses(), report.getGrossSalesRevenue())));
        appendEmptyRow(xml);

        appendExcelCells(xml, "TotalRow", List.of("NET OPERATING PROFIT", formatCurrency(report.getNetOperatingProfit()), formatCurrency(report.getNetOperatingProfitMarginPct()) + "%"));
        appendEmptyRow(xml);

        // Monthly Comparison
        if (report.getMonthlyComparison() != null && !report.getMonthlyComparison().isEmpty()) {
            appendExcelRow(xml, "SectionHeader", "MONTHLY FINANCIAL COMPARISON");
            appendExcelCells(xml, "Header", List.of("Month", "Revenue (Rs.)", "COGS (Rs.)", "Gross Profit (Rs.)", "Operating Exp (Rs.)", "Net Profit (Rs.)", "Net Margin (%)"));
            for (ReportDto.MonthlyProfitLossItem m : report.getMonthlyComparison()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(m.getMonthLabel()),
                        formatCurrency(m.getRevenue()),
                        formatCurrency(m.getCogs()),
                        formatCurrency(m.getGrossProfit()),
                        formatCurrency(m.getOperatingExpenses()),
                        formatCurrency(m.getNetOperatingProfit()),
                        formatCurrency(m.getNetMarginPct()) + "%"
                ));
            }
        }

        xml.append("  </Table>\n </Worksheet>\n");
    }

    private void renderReceivablesExcel(StringBuilder xml, ReportDto.ReceivablesReport report) {
        xml.append(" <Worksheet ss:Name=\"Receivables Report\">\n  <Table>\n");
        appendExcelRow(xml, "Title", report.getCompanyName() + " - CUSTOMER RECEIVABLES &amp; AGING REPORT");
        appendExcelRow(xml, "SubTitle", "Period: " + (report.getFilter() != null ? report.getFilter().getPeriodLabel() : "All Time") + " | Generated: " + LocalDateTime.now().format(TIMESTAMP_FMT));
        appendEmptyRow(xml);

        // KPI Summary Block
        appendExcelRow(xml, "SectionHeader", "RECEIVABLES RECOVERY SUMMARY");
        appendExcel2Col(xml, "KpiLabel", "Total Invoiced Amount", "KpiValue", "Rs. " + formatCurrency(report.getTotalInvoiced()));
        appendExcel2Col(xml, "KpiLabel", "Total Collected Amount", "KpiValue", "Rs. " + formatCurrency(report.getTotalPaid()) + " (" + formatCurrency(report.getCollectionRatePct()) + "% rate)");
        appendExcel2Col(xml, "KpiLabel", "Outstanding Receivables", "KpiValue", "Rs. " + formatCurrency(report.getTotalReceivables()));
        appendExcel2Col(xml, "KpiLabel", "Overdue Invoices Balance", "KpiValue", "Rs. " + formatCurrency(report.getTotalOverdue()) + " (" + report.getOverdueCount() + " overdue invoices)");
        appendEmptyRow(xml);

        // Customer Ledger
        if (report.getCustomerBreakdown() != null && !report.getCustomerBreakdown().isEmpty()) {
            appendExcelRow(xml, "SectionHeader", "CUSTOMER-WISE OUTSTANDING BALANCES");
            appendExcelCells(xml, "Header", List.of("Customer Name", "Phone", "Total Invoiced (Rs.)", "Total Paid (Rs.)", "Outstanding Balance (Rs.)", "Overdue Balance (Rs.)", "Status"));
            for (ReportDto.CustomerReceivableItem c : report.getCustomerBreakdown()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(c.getCustomerName()),
                        escapeXml(c.getPhone()),
                        formatCurrency(c.getTotalInvoiced()),
                        formatCurrency(c.getTotalPaid()),
                        formatCurrency(c.getOutstandingBalance()),
                        formatCurrency(c.getOverdueBalance()),
                        escapeXml(c.getStatus())
                ));
            }
            appendEmptyRow(xml);
        }

        // Invoices Detail
        appendExcelRow(xml, "SectionHeader", "INVOICE RECEIVABLES PIPELINE");
        appendExcelCells(xml, "Header", List.of("Invoice #", "Invoice Date", "Due Date", "Customer Name", "Total (Rs.)", "Paid (Rs.)", "Balance (Rs.)", "Status", "Overdue?"));
        if (report.getInvoices() != null) {
            for (ReportDto.InvoiceReceivableItem inv : report.getInvoices()) {
                appendExcelCells(xml, null, List.of(
                        escapeXml(inv.getInvoiceNumber()),
                        escapeXml(inv.getSaleDate()),
                        escapeXml(inv.getDueDate()),
                        escapeXml(inv.getCustomerName()),
                        formatCurrency(inv.getTotalAmount()),
                        formatCurrency(inv.getPaidAmount()),
                        formatCurrency(inv.getBalanceAmount()),
                        escapeXml(inv.getPaymentStatus()),
                        Boolean.TRUE.equals(inv.getIsOverdue()) ? "OVERDUE" : "On Track"
                ));
            }
            appendExcelCells(xml, "TotalRow", List.of(
                    "TOTALS", "", "", "",
                    formatCurrency(report.getTotalInvoiced()),
                    formatCurrency(report.getTotalPaid()),
                    formatCurrency(report.getTotalReceivables()),
                    "", ""
            ));
        }

        xml.append("  </Table>\n </Worksheet>\n");
    }

    private void appendExcelRow(StringBuilder xml, String styleId, String text) {
        xml.append("   <Row>\n");
        xml.append("    <Cell");
        if (styleId != null) xml.append(" ss:StyleID=\"").append(styleId).append("\"");
        xml.append("><Data ss:Type=\"String\">").append(escapeXml(text)).append("</Data></Cell>\n");
        xml.append("   </Row>\n");
    }

    private void appendExcel2Col(StringBuilder xml, String style1, String text1, String style2, String text2) {
        xml.append("   <Row>\n");
        xml.append("    <Cell");
        if (style1 != null) xml.append(" ss:StyleID=\"").append(style1).append("\"");
        xml.append("><Data ss:Type=\"String\">").append(escapeXml(text1)).append("</Data></Cell>\n");
        xml.append("    <Cell");
        if (style2 != null) xml.append(" ss:StyleID=\"").append(style2).append("\"");
        xml.append("><Data ss:Type=\"String\">").append(escapeXml(text2)).append("</Data></Cell>\n");
        xml.append("   </Row>\n");
    }

    private void appendExcelCells(StringBuilder xml, String styleId, List<String> cells) {
        xml.append("   <Row>\n");
        for (String cell : cells) {
            xml.append("    <Cell");
            if (styleId != null) xml.append(" ss:StyleID=\"").append(styleId).append("\"");
            xml.append("><Data ss:Type=\"String\">").append(escapeXml(cell)).append("</Data></Cell>\n");
        }
        xml.append("   </Row>\n");
    }

    private void appendEmptyRow(StringBuilder xml) {
        xml.append("   <Row></Row>\n");
    }

    private String escapeXml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String formatCurrency(Double val) {
        if (val == null) return "0.00";
        return CURRENCY_FMT.format(val);
    }

    private String formatPercent(Double part, Double whole) {
        if (whole == null || whole <= 0.0 || part == null) return "0.0%";
        return CURRENCY_FMT.format((part / whole) * 100.0) + "%";
    }

    // =========================================================================
    // STANDARD COMPLIANT PDF 1.4 ENGINE
    // =========================================================================

    public byte[] generatePdf(String reportType, Object reportData) {
        PdfDocument doc = new PdfDocument();

        String companyName = "MSME Cloud ERP";
        String title = "Business Report";
        String period = "All Time";

        if (reportData instanceof ReportDto.SalesReport) {
            ReportDto.SalesReport r = (ReportDto.SalesReport) reportData;
            companyName = r.getCompanyName();
            title = "SALES PERFORMANCE REPORT";
            period = r.getFilter() != null ? r.getFilter().getPeriodLabel() : "All Time";
            renderSalesPdf(doc, r, companyName, title, period);
        } else if (reportData instanceof ReportDto.PurchaseReport) {
            ReportDto.PurchaseReport r = (ReportDto.PurchaseReport) reportData;
            companyName = r.getCompanyName();
            title = "PROCUREMENT & PURCHASE REPORT";
            period = r.getFilter() != null ? r.getFilter().getPeriodLabel() : "All Time";
            renderPurchasePdf(doc, r, companyName, title, period);
        } else if (reportData instanceof ReportDto.InventoryReport) {
            ReportDto.InventoryReport r = (ReportDto.InventoryReport) reportData;
            companyName = r.getCompanyName();
            title = "INVENTORY VALUATION & STOCK REPORT";
            period = "As of " + r.getGeneratedDate();
            renderInventoryPdf(doc, r, companyName, title, period);
        } else if (reportData instanceof ReportDto.ExpenseReport) {
            ReportDto.ExpenseReport r = (ReportDto.ExpenseReport) reportData;
            companyName = r.getCompanyName();
            title = "OPERATING EXPENSES REPORT";
            period = r.getFilter() != null ? r.getFilter().getPeriodLabel() : "All Time";
            renderExpensePdf(doc, r, companyName, title, period);
        } else if (reportData instanceof ReportDto.ProfitLossReport) {
            ReportDto.ProfitLossReport r = (ReportDto.ProfitLossReport) reportData;
            companyName = r.getCompanyName();
            title = "PROFIT & LOSS STATEMENT";
            period = r.getFilter() != null ? r.getFilter().getPeriodLabel() : "All Time";
            renderProfitLossPdf(doc, r, companyName, title, period);
        } else if (reportData instanceof ReportDto.ReceivablesReport) {
            ReportDto.ReceivablesReport r = (ReportDto.ReceivablesReport) reportData;
            companyName = r.getCompanyName();
            title = "CUSTOMER RECEIVABLES & AGING REPORT";
            period = r.getFilter() != null ? r.getFilter().getPeriodLabel() : "All Time";
            renderReceivablesPdf(doc, r, companyName, title, period);
        }

        return doc.build();
    }

    private void renderSalesPdf(PdfDocument doc, ReportDto.SalesReport r, String company, String title, String period) {
        doc.newPage(company, title, period);

        // KPI Box
        doc.drawKpiBox("Total Revenue", "Rs. " + formatCurrency(r.getTotalRevenue()),
                "Transactions", String.valueOf(r.getTotalTransactions()),
                "Units Sold", String.valueOf(r.getTotalUnitsSold()),
                "Avg Order", "Rs. " + formatCurrency(r.getAverageOrderValue()));

        // Table
        List<String> headers = List.of("Invoice #", "Date", "Customer", "Product", "Qty", "Total (Rs.)", "Paid (Rs.)", "Status");
        float[] widths = {60f, 60f, 95f, 100f, 35f, 65f, 60f, 65f}; // Total 540

        List<List<String>> rows = new ArrayList<>();
        if (r.getTransactions() != null) {
            for (ReportDto.SaleTransactionItem s : r.getTransactions()) {
                rows.add(List.of(
                        cleanStr(s.getInvoiceNumber()),
                        cleanStr(s.getSaleDate()),
                        cleanStr(s.getCustomerName()),
                        cleanStr(s.getProductName()),
                        String.valueOf(s.getQuantity()),
                        formatCurrency(s.getTotalAmount()),
                        formatCurrency(s.getPaidAmount()),
                        cleanStr(s.getPaymentStatus())
                ));
            }
        }
        doc.drawTable(headers, widths, rows, "TOTAL REVENUE: Rs. " + formatCurrency(r.getTotalRevenue()) + " | Total Units: " + r.getTotalUnitsSold());
    }

    private void renderPurchasePdf(PdfDocument doc, ReportDto.PurchaseReport r, String company, String title, String period) {
        doc.newPage(company, title, period);

        doc.drawKpiBox("Procurement Spending", "Rs. " + formatCurrency(r.getTotalProcurementSpending()),
                "Total POs", String.valueOf(r.getTotalPurchasesCount()),
                "Received Orders", String.valueOf(r.getReceivedPurchasesCount()),
                "Pending Orders", String.valueOf(r.getPendingPurchasesCount()) + " (Rs. " + formatCurrency(r.getPendingAmount()) + ")");

        List<String> headers = List.of("PO #", "Date", "Supplier Name", "Status", "Total Amount (Rs.)", "Received Date");
        float[] widths = {80f, 75f, 150f, 75f, 85f, 75f};

        List<List<String>> rows = new ArrayList<>();
        if (r.getPurchases() != null) {
            for (ReportDto.PurchaseReportItem p : r.getPurchases()) {
                rows.add(List.of(
                        cleanStr(p.getPurchaseNumber()),
                        cleanStr(p.getCreatedAt()),
                        cleanStr(p.getSupplierName()),
                        cleanStr(p.getStatus()),
                        formatCurrency(p.getTotalAmount()),
                        cleanStr(p.getReceivedAt())
                ));
            }
        }
        doc.drawTable(headers, widths, rows, "TOTAL RECEIVED PROCUREMENT: Rs. " + formatCurrency(r.getTotalProcurementSpending()));
    }

    private void renderInventoryPdf(PdfDocument doc, ReportDto.InventoryReport r, String company, String title, String period) {
        doc.newPage(company, title, period);

        doc.drawKpiBox("Valuation (Cost)", "Rs. " + formatCurrency(r.getTotalValuationCost()),
                "Valuation (Retail)", "Rs. " + formatCurrency(r.getTotalValuationRetail()),
                "Total Stock Units", String.valueOf(r.getTotalStockUnits()),
                "Low Stock Alerts", String.valueOf(r.getLowStockCount()));

        List<String> headers = List.of("Product Name", "Cost (Rs.)", "Price (Rs.)", "Stock", "Reorder", "Valuation Cost", "Status");
        float[] widths = {140f, 65f, 65f, 50f, 50f, 90f, 80f};

        List<List<String>> rows = new ArrayList<>();
        if (r.getProducts() != null) {
            for (ReportDto.ProductInventoryItem p : r.getProducts()) {
                rows.add(List.of(
                        cleanStr(p.getName()),
                        formatCurrency(p.getCostPrice()),
                        formatCurrency(p.getSellingPrice()),
                        String.valueOf(p.getQuantity()),
                        String.valueOf(p.getReorderLevel()),
                        formatCurrency(p.getValuationCost()),
                        cleanStr(p.getStockStatus())
                ));
            }
        }
        doc.drawTable(headers, widths, rows, "TOTAL VALUATION (COST): Rs. " + formatCurrency(r.getTotalValuationCost()) + " | Total Units: " + r.getTotalStockUnits());
    }

    private void renderExpensePdf(PdfDocument doc, ReportDto.ExpenseReport r, String company, String title, String period) {
        doc.newPage(company, title, period);

        doc.drawKpiBox("Total Expenses", "Rs. " + formatCurrency(r.getTotalExpenses()),
                "Transactions", String.valueOf(r.getTotalTransactions()),
                "Average Expense", "Rs. " + formatCurrency(r.getAverageExpense()),
                "Top Category", (r.getCategoryBreakdown() != null && !r.getCategoryBreakdown().isEmpty()) ? r.getCategoryBreakdown().get(0).getCategory() : "N/A");

        List<String> headers = List.of("Date", "Expense Name", "Category", "Payment Method", "Amount (Rs.)", "Description");
        float[] widths = {65f, 115f, 85f, 85f, 75f, 115f};

        List<List<String>> rows = new ArrayList<>();
        if (r.getExpenses() != null) {
            for (ReportDto.ExpenseReportItem e : r.getExpenses()) {
                rows.add(List.of(
                        cleanStr(e.getExpenseDate()),
                        cleanStr(e.getName()),
                        cleanStr(e.getCategory()),
                        cleanStr(e.getPaymentMethod()),
                        formatCurrency(e.getAmount()),
                        cleanStr(e.getDescription())
                ));
            }
        }
        doc.drawTable(headers, widths, rows, "TOTAL OPERATING EXPENSES: Rs. " + formatCurrency(r.getTotalExpenses()));
    }

    private void renderProfitLossPdf(PdfDocument doc, ReportDto.ProfitLossReport r, String company, String title, String period) {
        doc.newPage(company, title, period);

        doc.drawKpiBox("Sales Revenue", "Rs. " + formatCurrency(r.getGrossSalesRevenue()),
                "Gross Profit", "Rs. " + formatCurrency(r.getGrossProfit()) + " (" + formatCurrency(r.getGrossProfitMarginPct()) + "%)",
                "Operating Expenses", "Rs. " + formatCurrency(r.getTotalOperatingExpenses()),
                "Net Operating Profit", "Rs. " + formatCurrency(r.getNetOperatingProfit()) + " (" + formatCurrency(r.getNetOperatingProfitMarginPct()) + "%)");

        // Financial statement breakdown
        List<String> headers = List.of("Financial Line Item", "Category / Detail", "Amount (Rs.)", "% of Revenue");
        float[] widths = {160f, 140f, 130f, 110f};

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("1. Gross Sales Revenue", "Total Invoiced Sales", formatCurrency(r.getGrossSalesRevenue()), "100.0%"));
        rows.add(List.of("2. Cost of Goods Sold (COGS)", "Procurement & Cost of Sold Goods", "-" + formatCurrency(r.getCostOfGoodsSold()), formatPercent(r.getCostOfGoodsSold(), r.getGrossSalesRevenue())));
        rows.add(List.of("3. GROSS PROFIT", "Revenue - COGS", formatCurrency(r.getGrossProfit()), formatCurrency(r.getGrossProfitMarginPct()) + "%"));

        if (r.getOperatingExpensesByCategory() != null) {
            for (ReportDto.CategoryExpenseItem cat : r.getOperatingExpensesByCategory()) {
                rows.add(List.of("  - Operating Expense", cat.getCategory(), "-" + formatCurrency(cat.getAmount()), formatPercent(cat.getAmount(), r.getGrossSalesRevenue())));
            }
        }
        rows.add(List.of("4. Total Operating Expenses", "Sum of all operating expenses", "-" + formatCurrency(r.getTotalOperatingExpenses()), formatPercent(r.getTotalOperatingExpenses(), r.getGrossSalesRevenue())));
        rows.add(List.of("5. NET OPERATING PROFIT", "Gross Profit - Operating Expenses", formatCurrency(r.getNetOperatingProfit()), formatCurrency(r.getNetOperatingProfitMarginPct()) + "%"));

        doc.drawTable(headers, widths, rows, "NET PROFIT: Rs. " + formatCurrency(r.getNetOperatingProfit()) + " (" + formatCurrency(r.getNetOperatingProfitMarginPct()) + "% Margin)");
    }

    private void renderReceivablesPdf(PdfDocument doc, ReportDto.ReceivablesReport r, String company, String title, String period) {
        doc.newPage(company, title, period);

        doc.drawKpiBox("Total Receivables", "Rs. " + formatCurrency(r.getTotalReceivables()),
                "Overdue Amount", "Rs. " + formatCurrency(r.getTotalOverdue()) + " (" + r.getOverdueCount() + " inv)",
                "Total Collected", "Rs. " + formatCurrency(r.getTotalPaid()) + " (" + formatCurrency(r.getCollectionRatePct()) + "%)",
                "Total Invoiced", "Rs. " + formatCurrency(r.getTotalInvoiced()));

        List<String> headers = List.of("Invoice #", "Date", "Due Date", "Customer", "Total (Rs.)", "Paid (Rs.)", "Balance (Rs.)", "Status");
        float[] widths = {65f, 60f, 60f, 105f, 65f, 60f, 65f, 60f};

        List<List<String>> rows = new ArrayList<>();
        if (r.getInvoices() != null) {
            for (ReportDto.InvoiceReceivableItem inv : r.getInvoices()) {
                rows.add(List.of(
                        cleanStr(inv.getInvoiceNumber()),
                        cleanStr(inv.getSaleDate()),
                        cleanStr(inv.getDueDate()),
                        cleanStr(inv.getCustomerName()),
                        formatCurrency(inv.getTotalAmount()),
                        formatCurrency(inv.getPaidAmount()),
                        formatCurrency(inv.getBalanceAmount()),
                        Boolean.TRUE.equals(inv.getIsOverdue()) ? "OVERDUE" : cleanStr(inv.getPaymentStatus())
                ));
            }
        }
        doc.drawTable(headers, widths, rows, "TOTAL RECEIVABLES BALANCE: Rs. " + formatCurrency(r.getTotalReceivables()) + " | Overdue: Rs. " + formatCurrency(r.getTotalOverdue()));
    }

    private String cleanStr(String str) {
        if (str == null) return "-";
        // ASCII only for standard Helvetica PDF fonts
        return str.replaceAll("[^\\x20-\\x7E]", " ").trim();
    }

    // =========================================================================
    // LIGHTWEIGHT PURE-JAVA PDF 1.4 BUILDER
    // =========================================================================

    private static class PdfDocument {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private final List<Long> xrefOffsets = new ArrayList<>();
        private final List<Integer> pageObjNums = new ArrayList<>();
        private final List<PdfPage> pages = new ArrayList<>();
        private int currentObjNum = 0;

        private static class PdfPage {
            String company;
            String title;
            String period;
            StringBuilder stream = new StringBuilder();
            float currentY = 700; // Top margin start
        }

        public void newPage(String company, String title, String period) {
            PdfPage page = new PdfPage();
            page.company = company != null ? company : "MSME Cloud ERP";
            page.title = title != null ? title : "REPORT";
            page.period = period != null ? period : "";
            pages.add(page);

            // Draw Header Banner on page creation
            drawHeaderBanner(page);
        }

        private void drawHeaderBanner(PdfPage p) {
            float margin = 28f;
            float width = 595.28f - (margin * 2);

            // 1. Header Banner Box (Navy fill)
            p.stream.append("0.12 0.23 0.42 rg\n"); // #1E3B6B
            p.stream.append(margin).append(" 785 ").append(width).append(" 42 re f\n");

            // 2. Company Name and Title
            p.stream.append("1 1 1 rg\n"); // White
            p.stream.append("BT /F2 13 Tf ").append(margin + 12).append(" 808 Td (").append(escapePdf(p.company.toUpperCase())).append(") Tj ET\n");
            p.stream.append("BT /F2 11 Tf ").append(margin + 12).append(" 793 Td (").append(escapePdf(p.title)).append(") Tj ET\n");

            // Subheader line
            p.stream.append("0.35 0.35 0.35 rg\n");
            p.stream.append("BT /F1 8 Tf ").append(margin).append(" 770 Td (Period: ").append(escapePdf(p.period))
                    .append(" | Generated: ").append(LocalDateTime.now().format(TIMESTAMP_FMT))
                    .append(" UTC | Confidential MSME ERP Report) Tj ET\n");

            // Divider line
            p.stream.append("0.8 0.82 0.85 RG 1 w\n");
            p.stream.append(margin).append(" 762 m ").append(margin + width).append(" 762 l S\n");

            p.currentY = 750f;
        }

        public void drawKpiBox(String label1, String val1, String label2, String val2, String label3, String val3, String label4, String val4) {
            if (pages.isEmpty()) return;
            PdfPage p = pages.get(pages.size() - 1);

            float margin = 28f;
            float totalWidth = 595.28f - (margin * 2);
            float cardWidth = (totalWidth - 24f) / 4f;
            float cardHeight = 44f;
            float cardY = p.currentY - cardHeight;

            String[] labels = {label1, label2, label3, label4};
            String[] vals = {val1, val2, val3, val4};

            for (int i = 0; i < 4; i++) {
                float cardX = margin + i * (cardWidth + 8f);

                // Card background
                p.stream.append("0.96 0.97 0.99 rg\n");
                p.stream.append(cardX).append(" ").append(cardY).append(" ").append(cardWidth).append(" ").append(cardHeight).append(" re f\n");

                // Border
                p.stream.append("0.85 0.88 0.92 RG 0.75 w\n");
                p.stream.append(cardX).append(" ").append(cardY).append(" ").append(cardWidth).append(" ").append(cardHeight).append(" re S\n");

                // Label
                p.stream.append("0.45 0.50 0.55 rg\n");
                p.stream.append("BT /F1 7.5 Tf ").append(cardX + 6).append(" ").append(cardY + 28).append(" Td (").append(escapePdf(labels[i])).append(") Tj ET\n");

                // Value
                p.stream.append("0.08 0.15 0.30 rg\n");
                p.stream.append("BT /F2 10 Tf ").append(cardX + 6).append(" ").append(cardY + 12).append(" Td (").append(escapePdf(vals[i])).append(") Tj ET\n");
            }

            p.currentY = cardY - 16f;
        }

        public void drawTable(List<String> headers, float[] widths, List<List<String>> rows, String summaryFooter) {
            if (pages.isEmpty()) return;
            PdfPage p = pages.get(pages.size() - 1);

            float margin = 28f;
            float rowHeight = 16f;
            float headerHeight = 20f;

            // Draw Table Header
            drawTableHeader(p, headers, widths, margin, headerHeight);

            // Draw Rows
            boolean alternate = false;
            for (List<String> row : rows) {
                // Check if page overflow
                if (p.currentY - rowHeight < 40f) {
                    // Start new page
                    String comp = p.company;
                    String tit = p.title;
                    String per = p.period;
                    newPage(comp, tit, per);
                    p = pages.get(pages.size() - 1);
                    drawTableHeader(p, headers, widths, margin, headerHeight);
                }

                float rowY = p.currentY - rowHeight;

                // Alternate background
                if (alternate) {
                    p.stream.append("0.97 0.98 0.99 rg\n");
                    p.stream.append(margin).append(" ").append(rowY).append(" 539.28 ").append(rowHeight).append(" re f\n");
                }
                alternate = !alternate;

                // Grid line bottom
                p.stream.append("0.90 0.91 0.93 RG 0.5 w\n");
                p.stream.append(margin).append(" ").append(rowY).append(" m ").append(margin + 539.28f).append(" ").append(rowY).append(" l S\n");

                // Row text
                float cellX = margin;
                p.stream.append("0.18 0.22 0.28 rg\n");
                for (int c = 0; c < row.size() && c < widths.length; c++) {
                    String val = row.get(c);
                    p.stream.append("BT /F1 7.5 Tf ").append(cellX + 4).append(" ").append(rowY + 5).append(" Td (").append(escapePdf(val)).append(") Tj ET\n");
                    cellX += widths[c];
                }

                p.currentY = rowY;
            }

            // Summary Footer bar
            if (summaryFooter != null) {
                if (p.currentY - 24f < 40f) {
                    newPage(p.company, p.title, p.period);
                    p = pages.get(pages.size() - 1);
                }
                float footerY = p.currentY - 22f;
                p.stream.append("0.90 0.93 0.97 rg\n");
                p.stream.append(margin).append(" ").append(footerY).append(" 539.28 20 re f\n");
                p.stream.append("0.12 0.23 0.42 RG 1 w\n");
                p.stream.append(margin).append(" ").append(footerY).append(" 539.28 20 re S\n");

                p.stream.append("0.08 0.18 0.38 rg\n");
                p.stream.append("BT /F2 8.5 Tf ").append(margin + 8).append(" ").append(footerY + 6).append(" Td (").append(escapePdf(summaryFooter)).append(") Tj ET\n");
                p.currentY = footerY - 12f;
            }
        }

        private void drawTableHeader(PdfPage p, List<String> headers, float[] widths, float margin, float headerHeight) {
            float headerY = p.currentY - headerHeight;

            // Fill header background
            p.stream.append("0.15 0.22 0.35 rg\n"); // Slate/Navy
            p.stream.append(margin).append(" ").append(headerY).append(" 539.28 ").append(headerHeight).append(" re f\n");

            // Header text
            p.stream.append("1 1 1 rg\n");
            float colX = margin;
            for (int i = 0; i < headers.size() && i < widths.length; i++) {
                p.stream.append("BT /F2 7.5 Tf ").append(colX + 4).append(" ").append(headerY + 6).append(" Td (").append(escapePdf(headers.get(i))).append(") Tj ET\n");
                colX += widths[i];
            }

            p.currentY = headerY;
        }

        private String escapePdf(String str) {
            if (str == null) return "";
            return str.replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)");
        }

        public byte[] build() {
            try {
                xrefOffsets.clear();
                pageObjNums.clear();
                currentObjNum = 0;
                baos.reset();

                // 1. Header
                writeBytes("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");

                // We'll prepare font objects first:
                // Obj 1: Helvetica
                // Obj 2: Helvetica-Bold
                int fontNormObj = 1;
                int fontBoldObj = 2;

                // 2. Objects for Fonts
                startObject(fontNormObj);
                writeBytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

                startObject(fontBoldObj);
                writeBytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

                int nextObj = 3;

                // For each page:
                // We create a Content Stream object, then a Page object
                List<Integer> pageContentObjs = new ArrayList<>();
                List<Integer> pageObjs = new ArrayList<>();

                for (int i = 0; i < pages.size(); i++) {
                    pageContentObjs.add(nextObj++);
                    pageObjs.add(nextObj++);
                }

                int pagesCatalogObj = nextObj++;
                int rootCatalogObj = nextObj++;

                // Write each page's content stream and page object
                int totalPages = pages.size();
                for (int i = 0; i < totalPages; i++) {
                    PdfPage p = pages.get(i);

                    // Add page footer with page number
                    p.stream.append("0.55 0.55 0.55 rg\n");
                    p.stream.append("BT /F1 7.5 Tf 28 20 Td (Page ").append(i + 1).append(" of ").append(totalPages)
                            .append(" | Generated by MSME Cloud ERP) Tj ET\n");

                    byte[] streamBytes = p.stream.toString().getBytes(StandardCharsets.US_ASCII);

                    // Content Stream Object
                    startObject(pageContentObjs.get(i));
                    writeBytes("<< /Length " + streamBytes.length + " >>\nstream\n");
                    baos.write(streamBytes);
                    writeBytes("\nendstream\nendobj\n");

                    // Page Object
                    startObject(pageObjs.get(i));
                    writeBytes("<< /Type /Page\n" +
                            "   /Parent " + pagesCatalogObj + " 0 R\n" +
                            "   /MediaBox [0 0 595.28 841.89]\n" +
                            "   /Contents " + pageContentObjs.get(i) + " 0 R\n" +
                            "   /Resources << /Font << /F1 " + fontNormObj + " 0 R /F2 " + fontBoldObj + " 0 R >> >>\n" +
                            ">>\nendobj\n");
                }

                // Pages Catalog Object
                startObject(pagesCatalogObj);
                StringBuilder kids = new StringBuilder();
                for (int pObj : pageObjs) {
                    kids.append(pObj).append(" 0 R ");
                }
                writeBytes("<< /Type /Pages\n   /Kids [" + kids.toString().trim() + "]\n   /Count " + totalPages + "\n>>\nendobj\n");

                // Root Catalog Object
                startObject(rootCatalogObj);
                writeBytes("<< /Type /Catalog /Pages " + pagesCatalogObj + " 0 R >>\nendobj\n");

                // XREF Table
                long startXref = baos.size();
                writeBytes("xref\n");
                writeBytes("0 " + (rootCatalogObj + 1) + "\n");
                writeBytes("0000000000 65535 f \n");
                for (int obj = 1; obj <= rootCatalogObj; obj++) {
                    long offset = xrefOffsets.get(obj);
                    writeBytes(String.format("%010d 00000 n \n", offset));
                }

                // Trailer
                writeBytes("trailer\n<< /Size " + (rootCatalogObj + 1) + "\n   /Root " + rootCatalogObj + " 0 R\n>>\n");
                writeBytes("startxref\n" + startXref + "\n%%EOF\n");

                return baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Error rendering PDF report", e);
            }
        }

        private void startObject(int objNum) {
            while (xrefOffsets.size() <= objNum) {
                xrefOffsets.add(0L);
            }
            xrefOffsets.set(objNum, (long) baos.size());
            writeBytes(objNum + " 0 obj\n");
        }

        private void writeBytes(String str) {
            try {
                baos.write(str.getBytes(StandardCharsets.US_ASCII));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
