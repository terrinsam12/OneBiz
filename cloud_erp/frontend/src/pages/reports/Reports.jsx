import { useEffect, useState } from "react";
import {
  getSalesReport,
  getPurchaseReport,
  getInventoryReport,
  getExpenseReport,
  getProfitLossReport,
  getReceivablesReport,
  downloadReportExport,
} from "../../services/api";

function formatCurrency(val) {
  const num = Number(val || 0);
  return `₹${num.toLocaleString("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function formatDate(val) {
  if (!val) return "—";
  try {
    const d = new Date(val);
    if (isNaN(d.getTime())) return val;
    return d.toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  } catch {
    return val;
  }
}

function formatPercent(val) {
  const num = Number(val || 0);
  return `${num.toFixed(1)}%`;
}

function Reports() {
  const currentUser = JSON.parse(localStorage.getItem("erp_user") || "{}");
  const userRole = (currentUser.role || "COMPANY_ADMIN").toUpperCase();
  const isStaff = userRole === "STAFF";

  // Active Report Tab: "SALES" | "PURCHASES" | "INVENTORY" | "EXPENSES" | "PROFIT_LOSS" | "RECEIVABLES"
  const [activeTab, setActiveTab] = useState("SALES");

  // Date Filter Preset & Custom Dates
  const [preset, setPreset] = useState("this_month");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [searchQuery, setSearchQuery] = useState("");

  // Report Data States
  const [salesData, setSalesData] = useState(null);
  const [purchaseData, setPurchaseData] = useState(null);
  const [inventoryData, setInventoryData] = useState(null);
  const [expenseData, setExpenseData] = useState(null);
  const [profitLossData, setProfitLossData] = useState(null);
  const [receivablesData, setReceivablesData] = useState(null);

  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const presets = [
    { id: "this_month", label: "This Month" },
    { id: "last_month", label: "Last Month" },
    { id: "this_quarter", label: "This Quarter" },
    { id: "this_year", label: "This Year" },
    { id: "all_time", label: "All Time" },
    { id: "custom", label: "Custom Range" },
  ];

  const reportTabs = [
    { id: "SALES", label: "Sales Report", icon: "📈" },
    { id: "PURCHASES", label: "Purchase Report", icon: "📥" },
    { id: "INVENTORY", label: "Inventory Report", icon: "📦" },
    { id: "EXPENSES", label: "Expense Report", icon: "💸" },
    { id: "PROFIT_LOSS", label: "Profit & Loss", icon: "💹" },
    { id: "RECEIVABLES", label: "Receivables Report", icon: "💳" },
  ];

  async function loadActiveReport() {
    try {
      setLoading(true);
      setError("");

      const params = {};
      if (preset === "custom") {
        if (startDate) params.startDate = startDate;
        if (endDate) params.endDate = endDate;
      } else {
        params.preset = preset;
      }

      if (activeTab === "SALES") {
        const res = await getSalesReport(params);
        setSalesData(res);
      } else if (activeTab === "PURCHASES") {
        const res = await getPurchaseReport(params);
        setPurchaseData(res);
      } else if (activeTab === "INVENTORY") {
        const res = await getInventoryReport();
        setInventoryData(res);
      } else if (activeTab === "EXPENSES") {
        const res = await getExpenseReport(params);
        setExpenseData(res);
      } else if (activeTab === "PROFIT_LOSS") {
        if (isStaff) {
          setError("Access Restricted: Staff members do not have permission to view Profit & Loss reports.");
          setProfitLossData(null);
        } else {
          const res = await getProfitLossReport(params);
          setProfitLossData(res);
        }
      } else if (activeTab === "RECEIVABLES") {
        const res = await getReceivablesReport(params);
        setReceivablesData(res);
      }
    } catch (err) {
      setError(err.message || "Failed to load report data.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadActiveReport();
  }, [activeTab, preset]);

  // Handle Export Download
  async function handleExport(format) {
    if (isStaff) {
      setError("Export Restricted: Staff members cannot download confidential company reports.");
      return;
    }
    try {
      setExporting(true);
      setError("");

      let reportTypeKey = "sales";
      if (activeTab === "PURCHASES") reportTypeKey = "purchases";
      if (activeTab === "INVENTORY") reportTypeKey = "inventory";
      if (activeTab === "EXPENSES") reportTypeKey = "expenses";
      if (activeTab === "PROFIT_LOSS") reportTypeKey = "profit-loss";
      if (activeTab === "RECEIVABLES") reportTypeKey = "receivables";

      const params = {};
      if (preset === "custom") {
        if (startDate) params.startDate = startDate;
        if (endDate) params.endDate = endDate;
      } else {
        params.preset = preset;
      }

      await downloadReportExport(reportTypeKey, format, params);
      setSuccess(`Report successfully exported to ${format.toUpperCase()}!`);
      setTimeout(() => setSuccess(""), 4000);
    } catch (err) {
      setError(err.message || `Failed to export ${format.toUpperCase()} report.`);
    } finally {
      setExporting(false);
    }
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
      {/* =====================================================
          HEADER SECTION WITH EXPORT BUTTONS
      ===================================================== */}
      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          alignItems: "center",
          justifyContent: "space-between",
          gap: "18px",
          background: "#ffffff",
          padding: "24px 28px",
          borderRadius: "14px",
          border: "1px solid #e2e8f0",
          boxShadow: "0 1px 3px rgba(0,0,0,0.04)",
        }}
      >
        <div>
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <h1
              style={{
                margin: 0,
                fontSize: "26px",
                fontWeight: 800,
                color: "#0f172a",
                letterSpacing: "-0.02em",
              }}
            >
              Business Reports &amp; Exports
            </h1>
            <span
              style={{
                fontSize: "12px",
                fontWeight: 700,
                padding: "3px 10px",
                borderRadius: "12px",
                background: "#eff6ff",
                color: "#2563eb",
                border: "1px solid #bfdbfe",
              }}
            >
              Enterprise BI
            </span>
          </div>
          <p style={{ margin: "6px 0 0", fontSize: "14.5px", color: "#64748b" }}>
            Real-time financial audits, procurement intelligence, valuation, and executive statement exports.
          </p>
        </div>

        {/* Action Export Buttons */}
        <div className="report-header-actions">
          <button
            type="button"
            onClick={() => handleExport("pdf")}
            disabled={exporting || loading || isStaff}
            className="export-btn-pdf"
            title={isStaff ? "Admin/Manager only" : "Download PDF Document"}
          >
            <span>📄</span>
            <span>{exporting ? "Generating..." : "Download PDF"}</span>
          </button>

          <button
            type="button"
            onClick={() => handleExport("excel")}
            disabled={exporting || loading || isStaff}
            className="export-btn-excel"
            title={isStaff ? "Admin/Manager only" : "Download Excel Spreadsheet"}
          >
            <span>📊</span>
            <span>{exporting ? "Generating..." : "Download Excel"}</span>
          </button>

          <button
            type="button"
            onClick={() => window.print()}
            className="export-btn-print"
            title="Print current report view"
          >
            <span>🖨️</span>
            <span>Print</span>
          </button>
        </div>
      </div>

      {/* FEEDBACK BANNERS */}
      {error && (
        <div className="error-message">
          <span>⚠️</span>
          <span>{error}</span>
          <button
            type="button"
            onClick={() => setError("")}
            style={{ marginLeft: "auto", background: "none", border: "none", color: "#b91c1c", cursor: "pointer", fontWeight: 700 }}
          >
            ✕
          </button>
        </div>
      )}

      {success && (
        <div className="success-state" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span>✓ {success}</span>
          <button
            type="button"
            onClick={() => setSuccess("")}
            style={{ background: "none", border: "none", color: "#047857", cursor: "pointer", fontWeight: 700 }}
          >
            ✕
          </button>
        </div>
      )}

      {isStaff && (
        <div
          style={{
            padding: "14px 18px",
            background: "#fffbeb",
            border: "1px solid #fde68a",
            borderRadius: "10px",
            fontSize: "13.5px",
            color: "#92400e",
          }}
        >
          <strong>Staff Mode:</strong> You have view access for operational reports (Sales, Purchases, Inventory, Expenses, Receivables). Profit &amp; Loss statements and file exports require Manager or Company Admin authorization.
        </div>
      )}

      {/* =====================================================
          REPORT TABS NAVIGATION (MODERN ENTERPRISE PILLS)
      ===================================================== */}
      <div className="report-tabs">
        {reportTabs.map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`report-tab-btn ${isActive ? "active" : ""}`}
            >
              <span style={{ fontSize: "16px" }}>{tab.icon}</span>
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* =====================================================
          DATE PRESETS & CUSTOM FILTER BAR
      ===================================================== */}
      {activeTab !== "INVENTORY" && (
        <div className="report-filter-bar">
          <div style={{ display: "flex", alignItems: "center", gap: "10px", flexWrap: "wrap" }}>
            <span style={{ fontSize: "12px", fontWeight: 700, color: "#64748b", letterSpacing: "0.06em", textTransform: "uppercase" }}>
              Period:
            </span>
            {presets.map((p) => (
              <button
                key={p.id}
                type="button"
                onClick={() => setPreset(p.id)}
                className={`preset-pill ${preset === p.id ? "active" : ""}`}
              >
                {p.label}
              </button>
            ))}
          </div>

          {preset === "custom" && (
            <div className="custom-date-box">
              <span style={{ fontSize: "13px", fontWeight: 600, color: "#64748b" }}>From:</span>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
              <span style={{ fontSize: "13px", fontWeight: 600, color: "#64748b" }}>To:</span>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
              <button
                type="button"
                onClick={loadActiveReport}
                className="primary-button"
                style={{ padding: "8px 16px", fontSize: "13px" }}
              >
                Apply
              </button>
            </div>
          )}
        </div>
      )}

      {/* =====================================================
          REPORT CONTENT AREA
      ===================================================== */}
      {loading ? (
        <div className="loading-page">
          <div style={{ fontSize: "32px", marginBottom: "8px" }}>⌛</div>
          <p>Aggregating enterprise report metrics...</p>
        </div>
      ) : (
        <div>
          {/* =================================================
              TAB 1: SALES REPORT
          ================================================= */}
          {activeTab === "SALES" && salesData && (
            <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
              {/* KPI Cards */}
              <div className="stats-grid">
                <div className="stat-card" style={{ borderLeft: "4px solid #16a34a" }}>
                  <div className="stat-header">
                    <span>Total Sales Revenue</span>
                    <div className="stat-icon revenue">₹</div>
                  </div>
                  <div className="stat-value" style={{ color: "#15803d" }}>
                    {formatCurrency(salesData.total_revenue)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge positive">↑ Net Invoiced</span>
                    <span>Total sales inflow</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #2563eb" }}>
                  <div className="stat-header">
                    <span>Total Transactions</span>
                    <div className="stat-icon units">🛍️</div>
                  </div>
                  <div className="stat-value" style={{ color: "#1d4ed8" }}>
                    {salesData.total_transactions}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge neutral">Orders</span>
                    <span>Fulfilled sales invoices</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #f59e0b" }}>
                  <div className="stat-header">
                    <span>Units Sold</span>
                    <div className="stat-icon transactions">📦</div>
                  </div>
                  <div className="stat-value" style={{ color: "#b45309" }}>
                    {salesData.total_units_sold}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge neutral">Inventory Items</span>
                    <span>Physical units dispatched</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #8b5cf6" }}>
                  <div className="stat-header">
                    <span>Average Order Value</span>
                    <div className="stat-icon customers">📈</div>
                  </div>
                  <div className="stat-value" style={{ color: "#6d28d9" }}>
                    {formatCurrency(salesData.average_order_value)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge positive">Ticket Size</span>
                    <span>Per order average</span>
                  </div>
                </div>
              </div>

              {/* Top Selling Products */}
              {salesData.top_products && salesData.top_products.length > 0 && (
                <div className="dashboard-card">
                  <div className="card-header">
                    <div>
                      <h3>🏆 Top-Selling Products Ranking</h3>
                      <p>Top volume and revenue contributors in this billing period</p>
                    </div>
                  </div>

                  <div className="table-wrapper">
                    <table>
                      <thead>
                        <tr>
                          <th>Rank &amp; Product Name</th>
                          <th style={{ textAlign: "right" }}>Units Sold</th>
                          <th style={{ textAlign: "right" }}>Total Revenue</th>
                          <th style={{ textAlign: "right" }}>Avg Selling Price</th>
                          <th style={{ textAlign: "right" }}>Revenue Share</th>
                        </tr>
                      </thead>
                      <tbody>
                        {salesData.top_products.map((p, idx) => {
                          const sharePct =
                            salesData.total_revenue > 0
                              ? (p.total_revenue / salesData.total_revenue) * 100
                              : 0;
                          return (
                            <tr key={idx}>
                              <td>
                                <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                                  <span
                                    style={{
                                      width: "24px",
                                      height: "24px",
                                      borderRadius: "50%",
                                      background: "#eff6ff",
                                      color: "#2563eb",
                                      fontWeight: 800,
                                      fontSize: "12px",
                                      display: "flex",
                                      alignItems: "center",
                                      justifyContent: "center",
                                    }}
                                  >
                                    {idx + 1}
                                  </span>
                                  <strong>{p.product_name}</strong>
                                </div>
                              </td>
                              <td style={{ textAlign: "right", fontWeight: 700 }}>{p.units_sold}</td>
                              <td style={{ textAlign: "right", fontWeight: 700, color: "#15803d" }}>
                                {formatCurrency(p.total_revenue)}
                              </td>
                              <td style={{ textAlign: "right" }}>{formatCurrency(p.avg_selling_price)}</td>
                              <td style={{ textAlign: "right", fontWeight: 600, color: "#2563eb" }}>
                                {formatPercent(sharePct)}
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* Transactions Log */}
              <div className="dashboard-card">
                <div className="card-header">
                  <div>
                    <h3>🧾 Sales Transactions Log</h3>
                    <p>Detailed sales invoice ledger and payment statuses</p>
                  </div>
                  <div style={{ maxWidth: "280px", width: "100%" }}>
                    <input
                      type="text"
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      placeholder="Search invoices, customer, product..."
                      style={{ width: "100%", padding: "8px 12px", fontSize: "13px" }}
                    />
                  </div>
                </div>

                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>Invoice #</th>
                        <th>Date</th>
                        <th>Customer</th>
                        <th>Product</th>
                        <th style={{ textAlign: "center" }}>Qty</th>
                        <th style={{ textAlign: "right" }}>Unit Price</th>
                        <th style={{ textAlign: "right" }}>Total Amount</th>
                        <th style={{ textAlign: "right" }}>Paid</th>
                        <th style={{ textAlign: "right" }}>Balance</th>
                        <th style={{ textAlign: "center" }}>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {salesData.transactions &&
                        salesData.transactions
                          .filter((tx) => {
                            if (!searchQuery) return true;
                            const q = searchQuery.toLowerCase();
                            return (
                              (tx.invoice_number && tx.invoice_number.toLowerCase().includes(q)) ||
                              (tx.customer_name && tx.customer_name.toLowerCase().includes(q)) ||
                              (tx.product_name && tx.product_name.toLowerCase().includes(q))
                            );
                          })
                          .map((tx) => (
                            <tr key={tx.id}>
                              <td>
                                <strong style={{ color: "#2563eb", fontFamily: "monospace", fontSize: "13px" }}>
                                  {tx.invoice_number}
                                </strong>
                              </td>
                              <td style={{ color: "#64748b", fontSize: "13px" }}>{formatDate(tx.sale_date)}</td>
                              <td>
                                <strong>{tx.customer_name}</strong>
                              </td>
                              <td>{tx.product_name}</td>
                              <td style={{ textAlign: "center", fontWeight: 600 }}>{tx.quantity}</td>
                              <td style={{ textAlign: "right" }}>{formatCurrency(tx.unit_price)}</td>
                              <td style={{ textAlign: "right", fontWeight: 700 }} className="amount">
                                {formatCurrency(tx.total_amount)}
                              </td>
                              <td style={{ textAlign: "right", color: "#15803d", fontWeight: 600 }}>
                                {formatCurrency(tx.paid_amount)}
                              </td>
                              <td style={{ textAlign: "right", color: "#b91c1c", fontWeight: 600 }}>
                                {formatCurrency(tx.balance_amount)}
                              </td>
                              <td style={{ textAlign: "center" }}>
                                <span
                                  className={`status-badge ${
                                    tx.payment_status === "PAID"
                                      ? "paid"
                                      : tx.payment_status === "PARTIALLY_PAID"
                                      ? "partial"
                                      : "unpaid"
                                  }`}
                                >
                                  {tx.payment_status}
                                </span>
                              </td>
                            </tr>
                          ))}
                    </tbody>
                    <tfoot>
                      <tr style={{ background: "#f8fafc", fontWeight: 800 }}>
                        <td colSpan={4}>TOTALS ({salesData.total_transactions} Transactions)</td>
                        <td style={{ textAlign: "center" }}>{salesData.total_units_sold}</td>
                        <td></td>
                        <td style={{ textAlign: "right", color: "#2563eb", fontSize: "15px" }}>
                          {formatCurrency(salesData.total_revenue)}
                        </td>
                        <td colSpan={3}></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>
          )}

          {/* =================================================
              TAB 2: PURCHASE REPORT
          ================================================= */}
          {activeTab === "PURCHASES" && purchaseData && (
            <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
              <div className="stats-grid">
                <div className="stat-card" style={{ borderLeft: "4px solid #16a34a" }}>
                  <div className="stat-header">
                    <span>Procurement Spending</span>
                    <div className="stat-icon revenue">₹</div>
                  </div>
                  <div className="stat-value" style={{ color: "#15803d" }}>
                    {formatCurrency(purchaseData.total_procurement_spending)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge positive">Delivered</span>
                    <span>Received &amp; stocked goods</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #2563eb" }}>
                  <div className="stat-header">
                    <span>Total Purchase Orders</span>
                    <div className="stat-icon units">📥</div>
                  </div>
                  <div className="stat-value" style={{ color: "#1d4ed8" }}>
                    {purchaseData.total_purchases_count}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge neutral">{purchaseData.received_purchases_count} Received</span>
                    <span>Total PO volume</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #f59e0b" }}>
                  <div className="stat-header">
                    <span>Pending POs</span>
                    <div className="stat-icon transactions">⏳</div>
                  </div>
                  <div className="stat-value" style={{ color: "#b45309" }}>
                    {purchaseData.pending_purchases_count}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge warning">{formatCurrency(purchaseData.pending_amount)}</span>
                    <span>Awaiting warehouse intake</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #8b5cf6" }}>
                  <div className="stat-header">
                    <span>Active Suppliers</span>
                    <div className="stat-icon customers">🚚</div>
                  </div>
                  <div className="stat-value" style={{ color: "#6d28d9" }}>
                    {purchaseData.supplier_breakdown?.length || 0}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge positive">Vendor Network</span>
                    <span>Supplying inventory</span>
                  </div>
                </div>
              </div>

              {/* Supplier Breakdown Table */}
              {purchaseData.supplier_breakdown && purchaseData.supplier_breakdown.length > 0 && (
                <div className="dashboard-card">
                  <div className="card-header">
                    <div>
                      <h3>🚚 Supplier-wise Procurement Breakdown</h3>
                      <p>Procurement volumes and total capital deployed per vendor</p>
                    </div>
                  </div>

                  <div className="table-wrapper">
                    <table>
                      <thead>
                        <tr>
                          <th>Supplier Name</th>
                          <th>Contact Person</th>
                          <th>Phone</th>
                          <th style={{ textAlign: "center" }}>Total Orders</th>
                          <th style={{ textAlign: "center" }}>Received Orders</th>
                          <th style={{ textAlign: "right" }}>Total Spending</th>
                          <th style={{ textAlign: "center" }}>Last Order Date</th>
                        </tr>
                      </thead>
                      <tbody>
                        {purchaseData.supplier_breakdown.map((s, idx) => (
                          <tr key={idx}>
                            <td>
                              <strong>{s.supplier_name}</strong>
                            </td>
                            <td>{s.contact_person || "—"}</td>
                            <td style={{ color: "#64748b" }}>{s.phone || "—"}</td>
                            <td style={{ textAlign: "center", fontWeight: 600 }}>{s.total_orders}</td>
                            <td style={{ textAlign: "center", fontWeight: 700, color: "#15803d" }}>
                              {s.received_orders}
                            </td>
                            <td style={{ textAlign: "right", fontWeight: 700 }} className="amount">
                              {formatCurrency(s.total_spending)}
                            </td>
                            <td style={{ textAlign: "center", color: "#64748b", fontSize: "13px" }}>
                              {formatDate(s.last_order_date)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* Purchases Directory Table */}
              <div className="dashboard-card">
                <div className="card-header">
                  <div>
                    <h3>📦 Purchase Orders Directory</h3>
                    <p>Recent purchase orders and intake status</p>
                  </div>
                </div>

                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>PO #</th>
                        <th>Created Date</th>
                        <th>Supplier Name</th>
                        <th style={{ textAlign: "center" }}>Status</th>
                        <th style={{ textAlign: "right" }}>Total Amount</th>
                        <th style={{ textAlign: "center" }}>Received Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      {purchaseData.purchases &&
                        purchaseData.purchases.map((p) => (
                          <tr key={p.id}>
                            <td>
                              <strong style={{ color: "#2563eb", fontFamily: "monospace" }}>
                                {p.purchase_number}
                              </strong>
                            </td>
                            <td style={{ color: "#64748b", fontSize: "13px" }}>{formatDate(p.created_at)}</td>
                            <td>
                              <strong>{p.supplier_name}</strong>
                            </td>
                            <td style={{ textAlign: "center" }}>
                              <span
                                className={`status-badge ${
                                  p.status === "RECEIVED"
                                    ? "paid"
                                    : p.status === "PENDING"
                                    ? "partial"
                                    : "unpaid"
                                }`}
                              >
                                {p.status}
                              </span>
                            </td>
                            <td style={{ textAlign: "right", fontWeight: 700 }} className="amount">
                              {formatCurrency(p.total_amount)}
                            </td>
                            <td style={{ textAlign: "center", color: "#64748b", fontSize: "13px" }}>
                              {formatDate(p.received_at)}
                            </td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}

          {/* =================================================
              TAB 3: INVENTORY REPORT
          ================================================= */}
          {activeTab === "INVENTORY" && inventoryData && (
            <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
              <div className="stats-grid">
                <div className="stat-card" style={{ borderLeft: "4px solid #2563eb" }}>
                  <div className="stat-header">
                    <span>Valuation (Cost)</span>
                    <div className="stat-icon units">💰</div>
                  </div>
                  <div className="stat-value" style={{ color: "#1d4ed8" }}>
                    {formatCurrency(inventoryData.total_valuation_cost)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge neutral">Asset Value</span>
                    <span>Capital locked in stock</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #16a34a" }}>
                  <div className="stat-header">
                    <span>Valuation (Retail)</span>
                    <div className="stat-icon revenue">🏷️</div>
                  </div>
                  <div className="stat-value" style={{ color: "#15803d" }}>
                    {formatCurrency(inventoryData.total_valuation_retail)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge positive">Gross Margin: {formatCurrency(inventoryData.potential_gross_margin)}</span>
                    <span>Expected revenue</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #8b5cf6" }}>
                  <div className="stat-header">
                    <span>Total Stock Units</span>
                    <div className="stat-icon customers">📦</div>
                  </div>
                  <div className="stat-value" style={{ color: "#6d28d9" }}>
                    {inventoryData.total_stock_units}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge neutral">{inventoryData.total_products} SKUs</span>
                    <span>Distinct catalog items</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #ef4444" }}>
                  <div className="stat-header">
                    <span>Low Stock Alerts</span>
                    <div className="stat-icon transactions">⚠️</div>
                  </div>
                  <div className="stat-value" style={{ color: "#b91c1c" }}>
                    {inventoryData.low_stock_count}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge warning">{inventoryData.out_of_stock_count} Out of stock</span>
                    <span>Immediate attention required</span>
                  </div>
                </div>
              </div>

              {/* Critical Low Stock Alert Card if any */}
              {inventoryData.low_stock_alerts && inventoryData.low_stock_alerts.length > 0 && (
                <div
                  style={{
                    background: "#fef2f2",
                    border: "1px solid #fecaca",
                    borderRadius: "14px",
                    padding: "20px 24px",
                  }}
                >
                  <h3 style={{ margin: "0 0 12px", color: "#991b1b", fontSize: "16px", display: "flex", alignItems: "center", gap: "8px" }}>
                    <span>⚠️</span> Critical Reorder Alerts (Stock &le; Reorder Level)
                  </h3>
                  <div className="table-wrapper" style={{ background: "#ffffff" }}>
                    <table>
                      <thead>
                        <tr>
                          <th>Product Name</th>
                          <th style={{ textAlign: "center" }}>Current Stock</th>
                          <th style={{ textAlign: "center" }}>Reorder Level</th>
                          <th style={{ textAlign: "right" }}>Cost Price</th>
                          <th style={{ textAlign: "center" }}>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {inventoryData.low_stock_alerts.map((item) => (
                          <tr key={item.id}>
                            <td>
                              <strong>{item.name}</strong>
                            </td>
                            <td style={{ textAlign: "center", fontWeight: 800, color: "#b91c1c" }}>
                              {item.quantity}
                            </td>
                            <td style={{ textAlign: "center" }}>{item.reorder_level}</td>
                            <td style={{ textAlign: "right" }}>{formatCurrency(item.cost_price)}</td>
                            <td style={{ textAlign: "center" }}>
                              <span className="low-stock">{item.stock_status}</span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* Product Inventory Ledger */}
              <div className="dashboard-card">
                <div className="card-header">
                  <div>
                    <h3>📦 Product Inventory &amp; Asset Valuation</h3>
                    <p>Current on-hand inventory levels and total valuations</p>
                  </div>
                </div>

                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>Product Name</th>
                        <th style={{ textAlign: "right" }}>Cost Price</th>
                        <th style={{ textAlign: "right" }}>Selling Price</th>
                        <th style={{ textAlign: "center" }}>In Stock</th>
                        <th style={{ textAlign: "center" }}>Reorder Level</th>
                        <th style={{ textAlign: "right" }}>Valuation (Cost)</th>
                        <th style={{ textAlign: "right" }}>Valuation (Retail)</th>
                        <th style={{ textAlign: "center" }}>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {inventoryData.products &&
                        inventoryData.products.map((p) => (
                          <tr key={p.id}>
                            <td>
                              <strong>{p.name}</strong>
                            </td>
                            <td style={{ textAlign: "right" }}>{formatCurrency(p.cost_price)}</td>
                            <td style={{ textAlign: "right" }}>{formatCurrency(p.selling_price)}</td>
                            <td style={{ textAlign: "center", fontWeight: 700 }}>{p.quantity}</td>
                            <td style={{ textAlign: "center", color: "#64748b" }}>{p.reorder_level}</td>
                            <td style={{ textAlign: "right", fontWeight: 700 }}>{formatCurrency(p.valuation_cost)}</td>
                            <td style={{ textAlign: "right", fontWeight: 700, color: "#2563eb" }}>
                              {formatCurrency(p.valuation_retail)}
                            </td>
                            <td style={{ textAlign: "center" }}>
                              <span
                                className={`status-badge ${
                                  p.stock_status === "IN_STOCK"
                                    ? "paid"
                                    : p.stock_status === "LOW_STOCK"
                                    ? "partial"
                                    : "unpaid"
                                }`}
                              >
                                {p.stock_status}
                              </span>
                            </td>
                          </tr>
                        ))}
                    </tbody>
                    <tfoot>
                      <tr style={{ background: "#f8fafc", fontWeight: 800 }}>
                        <td colSpan={3}>TOTALS ({inventoryData.total_products} Products)</td>
                        <td style={{ textAlign: "center" }}>{inventoryData.total_stock_units}</td>
                        <td></td>
                        <td style={{ textAlign: "right", color: "#0f172a", fontSize: "15px" }}>
                          {formatCurrency(inventoryData.total_valuation_cost)}
                        </td>
                        <td style={{ textAlign: "right", color: "#2563eb", fontSize: "15px" }}>
                          {formatCurrency(inventoryData.total_valuation_retail)}
                        </td>
                        <td></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>
          )}

          {/* =================================================
              TAB 4: EXPENSE REPORT
          ================================================= */}
          {activeTab === "EXPENSES" && expenseData && (
            <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
              <div className="stats-grid">
                <div className="stat-card" style={{ borderLeft: "4px solid #ef4444" }}>
                  <div className="stat-header">
                    <span>Total Operating Expenses</span>
                    <div className="stat-icon transactions">💸</div>
                  </div>
                  <div className="stat-value" style={{ color: "#b91c1c" }}>
                    {formatCurrency(expenseData.total_expenses)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge warning">Outflow</span>
                    <span>Cumulative operating expenses</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #2563eb" }}>
                  <div className="stat-header">
                    <span>Transactions Count</span>
                    <div className="stat-icon units">📑</div>
                  </div>
                  <div className="stat-value" style={{ color: "#1d4ed8" }}>
                    {expenseData.total_transactions}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge neutral">Vouchers</span>
                    <span>Recorded expense vouchers</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #8b5cf6" }}>
                  <div className="stat-header">
                    <span>Average Expense</span>
                    <div className="stat-icon customers">📊</div>
                  </div>
                  <div className="stat-value" style={{ color: "#6d28d9" }}>
                    {formatCurrency(expenseData.average_expense)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge neutral">Ticket</span>
                    <span>Per voucher average</span>
                  </div>
                </div>
              </div>

              {/* Category Breakdown Cards */}
              {expenseData.category_breakdown && expenseData.category_breakdown.length > 0 && (
                <div className="dashboard-card">
                  <div className="card-header">
                    <div>
                      <h3>🏷️ Category-wise Spending Breakdown</h3>
                      <p>Expense allocation across major business operational categories</p>
                    </div>
                  </div>

                  <div
                    style={{
                      display: "grid",
                      gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
                      gap: "16px",
                    }}
                  >
                    {expenseData.category_breakdown.map((c, idx) => (
                      <div
                        key={idx}
                        style={{
                          background: "#f8fafc",
                          border: "1px solid #e2e8f0",
                          borderRadius: "10px",
                          padding: "16px",
                          display: "flex",
                          flexDirection: "column",
                          gap: "8px",
                        }}
                      >
                        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                          <strong style={{ fontSize: "14.5px", color: "#0f172a" }}>{c.category}</strong>
                          <span style={{ fontSize: "15px", fontWeight: 800, color: "#b91c1c" }}>
                            {formatCurrency(c.amount)}
                          </span>
                        </div>
                        <div style={{ width: "100%", height: "8px", background: "#e2e8f0", borderRadius: "4px", overflow: "hidden" }}>
                          <div
                            style={{
                              width: `${Math.min(100, c.percentage_of_total)}%`,
                              height: "100%",
                              background: "#2563eb",
                              borderRadius: "4px",
                            }}
                          />
                        </div>
                        <div style={{ display: "flex", justifyContent: "space-between", fontSize: "12.5px", color: "#64748b" }}>
                          <span>{c.count} transaction(s)</span>
                          <span style={{ fontWeight: 700 }}>{formatPercent(c.percentage_of_total)} of total</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Expense Ledger Table */}
              <div className="dashboard-card">
                <div className="card-header">
                  <div>
                    <h3>💸 Expense Records Audit</h3>
                    <p>Itemized ledger of operating expenditures</p>
                  </div>
                </div>

                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Expense Name</th>
                        <th>Category</th>
                        <th>Payment Method</th>
                        <th style={{ textAlign: "right" }}>Amount</th>
                        <th>Description</th>
                      </tr>
                    </thead>
                    <tbody>
                      {expenseData.expenses &&
                        expenseData.expenses.map((e) => (
                          <tr key={e.id}>
                            <td style={{ color: "#64748b", fontSize: "13px" }}>{formatDate(e.expense_date)}</td>
                            <td>
                              <strong>{e.name}</strong>
                            </td>
                            <td>
                              <span
                                style={{
                                  background: "#f1f5f9",
                                  padding: "3px 8px",
                                  borderRadius: "6px",
                                  fontSize: "12px",
                                  fontWeight: 600,
                                }}
                              >
                                {e.category}
                              </span>
                            </td>
                            <td style={{ color: "#334155", fontSize: "13px" }}>{e.payment_method}</td>
                            <td style={{ textAlign: "right", fontWeight: 700, color: "#b91c1c" }}>
                              {formatCurrency(e.amount)}
                            </td>
                            <td style={{ color: "#64748b", fontSize: "13px" }}>{e.description || "—"}</td>
                          </tr>
                        ))}
                    </tbody>
                    <tfoot>
                      <tr style={{ background: "#f8fafc", fontWeight: 800 }}>
                        <td colSpan={4}>TOTAL OPERATING EXPENSES</td>
                        <td style={{ textAlign: "right", color: "#b91c1c", fontSize: "15px" }}>
                          {formatCurrency(expenseData.total_expenses)}
                        </td>
                        <td></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>
          )}

          {/* =================================================
              TAB 5: PROFIT & LOSS REPORT
          ================================================= */}
          {activeTab === "PROFIT_LOSS" && profitLossData && (
            <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
              <div className="stats-grid">
                <div className="stat-card" style={{ borderLeft: "4px solid #2563eb" }}>
                  <div className="stat-header">
                    <span>Gross Sales Revenue</span>
                    <div className="stat-icon units">₹</div>
                  </div>
                  <div className="stat-value" style={{ color: "#1d4ed8" }}>
                    {formatCurrency(profitLossData.gross_sales_revenue)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge neutral">100% Inflow</span>
                    <span>Gross sales turnover</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #f59e0b" }}>
                  <div className="stat-header">
                    <span>Cost of Goods Sold (COGS)</span>
                    <div className="stat-icon transactions">📦</div>
                  </div>
                  <div className="stat-value" style={{ color: "#b45309" }}>
                    {formatCurrency(profitLossData.cost_of_goods_sold)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge warning">Direct Cost</span>
                    <span>Cost of inventory sold</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #16a34a" }}>
                  <div className="stat-header">
                    <span>Gross Operating Profit</span>
                    <div className="stat-icon revenue">📈</div>
                  </div>
                  <div className="stat-value" style={{ color: "#15803d" }}>
                    {formatCurrency(profitLossData.gross_profit)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge positive">Margin: {formatPercent(profitLossData.gross_profit_margin_pct)}</span>
                    <span>Revenue minus COGS</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: `4px solid ${profitLossData.net_operating_profit >= 0 ? "#10b981" : "#ef4444"}` }}>
                  <div className="stat-header">
                    <span>Net Operating Profit</span>
                    <div className="stat-icon customers">💹</div>
                  </div>
                  <div
                    className="stat-value"
                    style={{
                      color: profitLossData.net_operating_profit >= 0 ? "#047857" : "#b91c1c",
                    }}
                  >
                    {formatCurrency(profitLossData.net_operating_profit)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge positive">Net Margin: {formatPercent(profitLossData.net_operating_profit_margin_pct)}</span>
                    <span>Gross profit minus OPEX</span>
                  </div>
                </div>
              </div>

              {/* Formal Income & Profitability Statement */}
              <div className="dashboard-card">
                <div className="card-header">
                  <div>
                    <h3>📑 Formal Income &amp; Profitability Statement</h3>
                    <p>Compliant MSME profit &amp; loss statement matching ERP standards</p>
                  </div>
                  <span style={{ fontSize: "12px", background: "#f1f5f9", padding: "4px 10px", borderRadius: "6px", fontWeight: 600 }}>
                    Official Audit Format
                  </span>
                </div>

                <div className="table-wrapper">
                  <table>
                    <tbody>
                      {/* Revenue */}
                      <tr style={{ background: "#f8fafc", fontWeight: 800, fontSize: "15px" }}>
                        <td style={{ padding: "16px 20px" }}>1. GROSS SALES REVENUE</td>
                        <td style={{ textAlign: "right", color: "#2563eb", padding: "16px 20px" }}>
                          {formatCurrency(profitLossData.gross_sales_revenue)}
                        </td>
                        <td style={{ textAlign: "right", color: "#64748b", fontSize: "13px", padding: "16px 20px" }}>
                          100.0%
                        </td>
                      </tr>

                      {/* COGS */}
                      <tr>
                        <td style={{ paddingLeft: "36px", color: "#475569" }}>Less: Cost of Goods Sold (COGS)</td>
                        <td style={{ textAlign: "right", color: "#64748b" }}>
                          ({formatCurrency(profitLossData.cost_of_goods_sold)})
                        </td>
                        <td style={{ textAlign: "right", color: "#94a3b8", fontSize: "13px" }}>
                          {profitLossData.gross_sales_revenue > 0
                            ? formatPercent((profitLossData.cost_of_goods_sold / profitLossData.gross_sales_revenue) * 100)
                            : "0.0%"}
                        </td>
                      </tr>

                      {/* Gross Profit */}
                      <tr style={{ background: "#ecfdf5", fontWeight: 800, fontSize: "15px" }}>
                        <td style={{ padding: "16px 20px", color: "#065f46" }}>2. GROSS OPERATING PROFIT</td>
                        <td style={{ textAlign: "right", color: "#059669", padding: "16px 20px" }}>
                          {formatCurrency(profitLossData.gross_profit)}
                        </td>
                        <td style={{ textAlign: "right", color: "#047857", fontWeight: 800, padding: "16px 20px" }}>
                          {formatPercent(profitLossData.gross_profit_margin_pct)}
                        </td>
                      </tr>

                      {/* Operating Expenses */}
                      <tr style={{ background: "#f8fafc", fontWeight: 700 }}>
                        <td colSpan={3} style={{ padding: "12px 20px" }}>3. OPERATING EXPENSES (OPEX)</td>
                      </tr>

                      {profitLossData.operating_expenses_by_category &&
                        profitLossData.operating_expenses_by_category.map((cat, idx) => (
                          <tr key={idx} style={{ fontSize: "13.5px" }}>
                            <td style={{ paddingLeft: "36px", color: "#475569" }}>&bull; {cat.category}</td>
                            <td style={{ textAlign: "right", color: "#64748b" }}>
                              ({formatCurrency(cat.amount)})
                            </td>
                            <td style={{ textAlign: "right", color: "#94a3b8", fontSize: "12.5px" }}>
                              {profitLossData.gross_sales_revenue > 0
                                ? formatPercent((cat.amount / profitLossData.gross_sales_revenue) * 100)
                                : "0.0%"}
                            </td>
                          </tr>
                        ))}

                      <tr style={{ fontWeight: 700, color: "#b91c1c", background: "#fff1f2" }}>
                        <td style={{ paddingLeft: "36px" }}>Total Operating Expenses</td>
                        <td style={{ textAlign: "right" }}>
                          ({formatCurrency(profitLossData.total_operating_expenses)})
                        </td>
                        <td style={{ textAlign: "right", fontSize: "13px" }}>
                          {profitLossData.gross_sales_revenue > 0
                            ? formatPercent((profitLossData.total_operating_expenses / profitLossData.gross_sales_revenue) * 100)
                            : "0.0%"}
                        </td>
                      </tr>

                      {/* Net Operating Profit */}
                      <tr
                        style={{
                          background: "#eff6ff",
                          borderTop: "2px solid #93c5fd",
                          borderBottom: "2px solid #93c5fd",
                          fontWeight: 800,
                          fontSize: "16px",
                        }}
                      >
                        <td style={{ padding: "18px 20px", color: "#1e3a8a" }}>4. NET OPERATING PROFIT (EBIT)</td>
                        <td
                          style={{
                            textAlign: "right",
                            padding: "18px 20px",
                            color: profitLossData.net_operating_profit >= 0 ? "#059669" : "#dc2626",
                          }}
                        >
                          {formatCurrency(profitLossData.net_operating_profit)}
                        </td>
                        <td
                          style={{
                            textAlign: "right",
                            padding: "18px 20px",
                            color: "#1e3a8a",
                            fontWeight: 800,
                          }}
                        >
                          {formatPercent(profitLossData.net_operating_profit_margin_pct)}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}

          {/* =================================================
              TAB 6: RECEIVABLES REPORT
          ================================================= */}
          {activeTab === "RECEIVABLES" && receivablesData && (
            <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
              <div className="stats-grid">
                <div className="stat-card" style={{ borderLeft: "4px solid #f59e0b" }}>
                  <div className="stat-header">
                    <span>Total Receivables</span>
                    <div className="stat-icon transactions">💳</div>
                  </div>
                  <div className="stat-value" style={{ color: "#b45309" }}>
                    {formatCurrency(receivablesData.total_receivables)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge warning">Outstanding</span>
                    <span>Total balance due from clients</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #ef4444" }}>
                  <div className="stat-header">
                    <span>Overdue Invoices</span>
                    <div className="stat-icon transactions">⚠️</div>
                  </div>
                  <div className="stat-value" style={{ color: "#b91c1c" }}>
                    {formatCurrency(receivablesData.total_overdue)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge warning">{receivablesData.overdue_count} Invoices</span>
                    <span>Exceeded invoice payment term</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #16a34a" }}>
                  <div className="stat-header">
                    <span>Total Collected</span>
                    <div className="stat-icon revenue">✓</div>
                  </div>
                  <div className="stat-value" style={{ color: "#15803d" }}>
                    {formatCurrency(receivablesData.total_paid)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge positive">Collection Rate: {formatPercent(receivablesData.collection_rate_pct)}</span>
                    <span>Paid to date</span>
                  </div>
                </div>

                <div className="stat-card" style={{ borderLeft: "4px solid #2563eb" }}>
                  <div className="stat-header">
                    <span>Invoice Pipeline</span>
                    <div className="stat-icon units">📑</div>
                  </div>
                  <div className="stat-value" style={{ color: "#1d4ed8" }}>
                    {receivablesData.total_invoiced_count || (receivablesData.paid_count + receivablesData.partially_paid_count + receivablesData.unpaid_count)}
                  </div>
                  <div className="stat-change">
                    <span className="stat-badge neutral">
                      {receivablesData.paid_count} Paid &bull; {receivablesData.partially_paid_count} Partial &bull; {receivablesData.unpaid_count} Unpaid
                    </span>
                  </div>
                </div>
              </div>

              {/* Customer Ledger Breakdown */}
              {receivablesData.customer_breakdown && receivablesData.customer_breakdown.length > 0 && (
                <div className="dashboard-card">
                  <div className="card-header">
                    <div>
                      <h3>👥 Customer-wise Outstanding Balances</h3>
                      <p>Receivables distribution, total payments, and aging status by customer</p>
                    </div>
                  </div>

                  <div className="table-wrapper">
                    <table>
                      <thead>
                        <tr>
                          <th>Customer Name</th>
                          <th>Phone</th>
                          <th style={{ textAlign: "right" }}>Invoiced Amount</th>
                          <th style={{ textAlign: "right" }}>Paid Amount</th>
                          <th style={{ textAlign: "right" }}>Outstanding Balance</th>
                          <th style={{ textAlign: "right" }}>Overdue Balance</th>
                          <th style={{ textAlign: "center" }}>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {receivablesData.customer_breakdown.map((c, idx) => (
                          <tr key={idx}>
                            <td>
                              <strong>{c.customer_name}</strong>
                            </td>
                            <td style={{ color: "#64748b" }}>{c.phone || "—"}</td>
                            <td style={{ textAlign: "right" }}>{formatCurrency(c.total_invoiced)}</td>
                            <td style={{ textAlign: "right", color: "#15803d", fontWeight: 600 }}>
                              {formatCurrency(c.total_paid)}
                            </td>
                            <td style={{ textAlign: "right", fontWeight: 700, color: "#b91c1c" }}>
                              {formatCurrency(c.outstanding_balance)}
                            </td>
                            <td style={{ textAlign: "right", fontWeight: 700, color: "#991b1b" }}>
                              {formatCurrency(c.overdue_balance)}
                            </td>
                            <td style={{ textAlign: "center" }}>
                              <span
                                className={`status-badge ${
                                  c.status === "CLEAR" ? "paid" : c.status === "OVERDUE" ? "unpaid" : "partial"
                                }`}
                              >
                                {c.status}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* Detailed Invoices Receivables Log */}
              <div className="dashboard-card">
                <div className="card-header">
                  <div>
                    <h3>💳 Detailed Invoices Receivables Log</h3>
                    <p>Audit trail of all customer invoices, due dates, and settlement status</p>
                  </div>
                </div>

                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>Invoice #</th>
                        <th>Invoice Date</th>
                        <th>Due Date</th>
                        <th>Customer</th>
                        <th style={{ textAlign: "right" }}>Total Amount</th>
                        <th style={{ textAlign: "right" }}>Paid Amount</th>
                        <th style={{ textAlign: "right" }}>Balance Amount</th>
                        <th style={{ textAlign: "center" }}>Payment Status</th>
                        <th style={{ textAlign: "center" }}>Overdue?</th>
                      </tr>
                    </thead>
                    <tbody>
                      {receivablesData.invoices &&
                        receivablesData.invoices.map((inv) => (
                          <tr key={inv.id}>
                            <td>
                              <strong style={{ color: "#2563eb", fontFamily: "monospace" }}>
                                {inv.invoice_number}
                              </strong>
                            </td>
                            <td style={{ color: "#64748b", fontSize: "13px" }}>{formatDate(inv.sale_date)}</td>
                            <td style={{ color: "#64748b", fontSize: "13px" }}>{formatDate(inv.due_date)}</td>
                            <td>
                              <strong>{inv.customer_name}</strong>
                            </td>
                            <td style={{ textAlign: "right", fontWeight: 700 }} className="amount">
                              {formatCurrency(inv.total_amount)}
                            </td>
                            <td style={{ textAlign: "right", color: "#15803d", fontWeight: 600 }}>
                              {formatCurrency(inv.paid_amount)}
                            </td>
                            <td style={{ textAlign: "right", color: "#b91c1c", fontWeight: 700 }}>
                              {formatCurrency(inv.balance_amount)}
                            </td>
                            <td style={{ textAlign: "center" }}>
                              <span
                                className={`status-badge ${
                                  inv.payment_status === "PAID"
                                    ? "paid"
                                    : inv.payment_status === "PARTIALLY_PAID"
                                    ? "partial"
                                    : "unpaid"
                                }`}
                              >
                                {inv.payment_status}
                              </span>
                            </td>
                            <td style={{ textAlign: "center" }}>
                              {inv.is_overdue ? (
                                <span className="low-stock" style={{ background: "#fee2e2", color: "#b91c1c" }}>
                                  OVERDUE
                                </span>
                              ) : (
                                <span style={{ fontSize: "12px", color: "#64748b" }}>On Track</span>
                              )}
                            </td>
                          </tr>
                        ))}
                    </tbody>
                    <tfoot>
                      <tr style={{ background: "#f8fafc", fontWeight: 800 }}>
                        <td colSpan={4}>TOTALS</td>
                        <td style={{ textAlign: "right", color: "#0f172a", fontSize: "15px" }}>
                          {formatCurrency(receivablesData.total_invoiced)}
                        </td>
                        <td style={{ textAlign: "right", color: "#15803d", fontSize: "15px" }}>
                          {formatCurrency(receivablesData.total_paid)}
                        </td>
                        <td style={{ textAlign: "right", color: "#b91c1c", fontSize: "15px" }}>
                          {formatCurrency(receivablesData.total_receivables)}
                        </td>
                        <td colSpan={2}></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default Reports;
