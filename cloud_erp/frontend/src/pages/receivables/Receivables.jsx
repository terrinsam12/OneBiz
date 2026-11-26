import { useEffect, useState } from "react";
import {
  getReceivablesSummary,
  getInvoiceReceivables,
  getCustomerReceivables,
  recordPayment,
  getSalePayments,
  getCustomerPayments,
  getPayments,
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

function Receivables() {
  const [summary, setSummary] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [customerBalances, setCustomerBalances] = useState([]);
  const [paymentsLog, setPaymentsLog] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Tabs: "INVOICES" | "CUSTOMERS" | "PAYMENTS_LOG"
  const [activeTab, setActiveTab] = useState("INVOICES");

  // Filters for Invoices
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [search, setSearch] = useState("");

  // Record Payment Modal State
  const [payingInvoice, setPayingInvoice] = useState(null);
  const [payForm, setPayForm] = useState({
    amount: "",
    payment_date: new Date().toISOString().split("T")[0],
    payment_method: "Bank Transfer",
    reference_number: "",
    notes: "",
  });
  const [submittingPayment, setSubmittingPayment] = useState(false);

  // Payment History Modal State
  const [historyModal, setHistoryModal] = useState({
    open: false,
    title: "",
    payments: [],
    loading: false,
  });

  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("erp_user")) || null;
    } catch {
      return null;
    }
  })();

  const canManage =
    currentUser?.role === "COMPANY_ADMIN" || currentUser?.role === "MANAGER";

  async function loadData() {
    try {
      setLoading(true);
      setError("");

      const [sumData, invData] = await Promise.all([
        getReceivablesSummary(),
        getInvoiceReceivables({ status: statusFilter, search }),
      ]);

      setSummary(sumData);
      setInvoices(Array.isArray(invData) ? invData : []);

      if (activeTab === "CUSTOMERS") {
        const custData = await getCustomerReceivables({ search });
        setCustomerBalances(Array.isArray(custData) ? custData : []);
      } else if (activeTab === "PAYMENTS_LOG") {
        const payData = await getPayments();
        setPaymentsLog(Array.isArray(payData) ? payData : []);
      }
    } catch (err) {
      setError(err.message || "Failed to load receivables data.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();

    function handleCompanyChange() {
      loadData();
    }
    window.addEventListener("companyChanged", handleCompanyChange);
    return () => {
      window.removeEventListener("companyChanged", handleCompanyChange);
    };
  }, [activeTab, statusFilter]);

  function handleFilterSubmit(e) {
    if (e) e.preventDefault();
    loadData();
  }

  function handleResetFilters() {
    setStatusFilter("ALL");
    setSearch("");
    setTimeout(() => {
      loadData();
    }, 0);
  }

  // Open Payment Modal
  function openPaymentModal(inv) {
    setPayingInvoice(inv);
    setPayForm({
      amount: inv.balance_amount || "",
      payment_date: new Date().toISOString().split("T")[0],
      payment_method: "Bank Transfer",
      reference_number: "",
      notes: `Payment for ${inv.invoice_number}`,
    });
    setError("");
  }

  // Submit Payment
  async function handlePaymentSubmit(e) {
    e.preventDefault();
    if (!payingInvoice) return;

    const amt = parseFloat(payForm.amount);
    if (!amt || amt <= 0) {
      setError("Please enter a valid payment amount greater than zero.");
      return;
    }

    if (amt > payingInvoice.balance_amount + 0.001) {
      setError(
        `Payment amount (${formatCurrency(amt)}) cannot exceed remaining balance of ${formatCurrency(
          payingInvoice.balance_amount
        )}.`
      );
      return;
    }

    try {
      setSubmittingPayment(true);
      setError("");

      await recordPayment({
        sale_id: payingInvoice.id,
        amount: amt,
        payment_date: payForm.payment_date,
        payment_method: payForm.payment_method,
        reference_number: payForm.reference_number,
        notes: payForm.notes,
      });

      setSuccess(
        `Payment of ${formatCurrency(amt)} recorded successfully for ${payingInvoice.invoice_number}!`
      );
      setPayingInvoice(null);
      await loadData();
    } catch (err) {
      setError(err.message || "Failed to record payment.");
    } finally {
      setSubmittingPayment(false);
    }
  }

  // View Payments for Invoice
  async function openInvoiceHistory(inv) {
    setHistoryModal({
      open: true,
      title: `Payment History for ${inv.invoice_number} (${inv.customer_name})`,
      payments: [],
      loading: true,
    });

    try {
      const history = await getSalePayments(inv.id);
      setHistoryModal((prev) => ({
        ...prev,
        payments: Array.isArray(history) ? history : [],
        loading: false,
      }));
    } catch (err) {
      setError("Failed to load invoice payment history.");
      setHistoryModal((prev) => ({ ...prev, loading: false }));
    }
  }

  // View Payments for Customer
  async function openCustomerHistory(cust) {
    setHistoryModal({
      open: true,
      title: `All Payments from ${cust.customer_name}`,
      payments: [],
      loading: true,
    });

    try {
      const history = await getCustomerPayments(cust.customer_id);
      setHistoryModal((prev) => ({
        ...prev,
        payments: Array.isArray(history) ? history : [],
        loading: false,
      }));
    } catch (err) {
      setError("Failed to load customer payment history.");
      setHistoryModal((prev) => ({ ...prev, loading: false }));
    }
  }

  return (
    <div className="page-container">
      {/* =====================================================
          HEADER
      ===================================================== */}
      <div className="page-header">
        <div>
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <h2>Customer Payments & Receivables</h2>
            <span
              style={{
                background: "#e0f2fe",
                color: "#0369a1",
                padding: "3px 10px",
                borderRadius: "12px",
                fontSize: "12px",
                fontWeight: 600,
              }}
            >
              Cash Inflow Management
            </span>
          </div>
          <p>
            Track invoice payment statuses (Paid, Partially Paid, Unpaid), due dates, overdue receivables, and record full or partial customer payments.
          </p>
        </div>

        <button
          type="button"
          className="secondary-button"
          onClick={() => loadData()}
          title="Refresh Data"
        >
          🔄 Refresh
        </button>
      </div>

      {/* Notifications */}
      {error && (
        <div className="error-card" style={{ marginBottom: "20px" }}>
          <strong>Error:</strong> {error}
        </div>
      )}
      {success && (
        <div
          style={{
            background: "#dcfce7",
            color: "#15803d",
            padding: "12px 16px",
            borderRadius: "8px",
            marginBottom: "20px",
            border: "1px solid #bbf7d0",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <span>✓ {success}</span>
          <button
            type="button"
            onClick={() => setSuccess("")}
            style={{ background: "none", border: "none", cursor: "pointer", color: "#15803d" }}
          >
            ✕
          </button>
        </div>
      )}

      {/* =====================================================
          KPI SUMMARY STATS CARDS
      ===================================================== */}
      <section className="stats-grid" style={{ marginBottom: "25px" }}>
        {/* Total Receivables */}
        <div className="stat-card" style={{ borderLeft: "4px solid #3b82f6" }}>
          <div className="stat-header">
            <span>TOTAL RECEIVABLES</span>
            <span style={{ fontSize: "18px" }}>💳</span>
          </div>
          <div className="stat-value" style={{ color: "#1d4ed8" }}>
            {formatCurrency(summary?.total_receivables)}
          </div>
          <div className="stat-change" style={{ color: "#64748b" }}>
            <span>Outstanding across {summary?.unpaid_invoices_count + summary?.partially_paid_invoices_count || 0} open invoices</span>
          </div>
        </div>

        {/* Overdue Amount */}
        <div
          className="stat-card"
          style={{
            borderLeft: "4px solid #ef4444",
            background: (summary?.total_overdue || 0) > 0 ? "#fef2f2" : "#ffffff",
          }}
        >
          <div className="stat-header">
            <span style={{ color: (summary?.total_overdue || 0) > 0 ? "#b91c1c" : "#64748b", fontWeight: 700 }}>
              OVERDUE RECEIVABLES
            </span>
            <span style={{ fontSize: "18px" }}>⚠️</span>
          </div>
          <div className="stat-value" style={{ color: "#dc2626" }}>
            {formatCurrency(summary?.total_overdue)}
          </div>
          <div className="stat-change" style={{ color: "#b91c1c", fontWeight: 500 }}>
            <span>{summary?.overdue_invoices_count || 0} past due invoices requiring follow-up</span>
          </div>
        </div>

        {/* Total Collected */}
        <div className="stat-card" style={{ borderLeft: "4px solid #10b981" }}>
          <div className="stat-header">
            <span>COLLECTED PAYMENTS</span>
            <span
              style={{
                fontSize: "11px",
                fontWeight: 700,
                padding: "2px 8px",
                borderRadius: "10px",
                background: "#d1fae5",
                color: "#065f46",
              }}
            >
              {summary?.collection_rate_pct || 0}% Rate
            </span>
          </div>
          <div className="stat-value" style={{ color: "#059669" }}>
            {formatCurrency(summary?.total_paid)}
          </div>
          <div className="stat-change" style={{ color: "#64748b" }}>
            <span>Out of {formatCurrency(summary?.total_invoiced)} total billed</span>
          </div>
        </div>

        {/* Invoice Status Overview */}
        <div className="stat-card" style={{ borderLeft: "4px solid #8b5cf6" }}>
          <div className="stat-header">
            <span>INVOICE PIPELINE</span>
            <span style={{ fontSize: "18px" }}>📑</span>
          </div>
          <div className="stat-value" style={{ color: "#6d28d9" }}>
            {summary?.total_invoices_count || 0} Invoices
          </div>
          <div className="stat-change" style={{ color: "#64748b" }}>
            <span>
              <strong style={{ color: "#16a34a" }}>{summary?.paid_invoices_count || 0}</strong> Paid •{" "}
              <strong style={{ color: "#f59e0b" }}>{summary?.partially_paid_invoices_count || 0}</strong> Partial •{" "}
              <strong style={{ color: "#dc2626" }}>{summary?.unpaid_invoices_count || 0}</strong> Unpaid
            </span>
          </div>
        </div>
      </section>

      {/* =====================================================
          TAB NAVIGATION SWITCHER
      ===================================================== */}
      <div style={{ display: "flex", gap: "10px", marginBottom: "20px", borderBottom: "1px solid #e2e8f0", paddingBottom: "10px" }}>
        <button
          type="button"
          onClick={() => setActiveTab("INVOICES")}
          style={{
            padding: "8px 18px",
            fontSize: "13px",
            fontWeight: activeTab === "INVOICES" ? 700 : 500,
            borderRadius: "6px",
            border: "none",
            background: activeTab === "INVOICES" ? "#2563eb" : "#f1f5f9",
            color: activeTab === "INVOICES" ? "#ffffff" : "#475569",
            cursor: "pointer",
            transition: "all 0.15s ease",
          }}
        >
          📄 Invoices & Receivables ({invoices.length})
        </button>

        <button
          type="button"
          onClick={() => {
            setActiveTab("CUSTOMERS");
            getCustomerReceivables({ search }).then((d) => setCustomerBalances(Array.isArray(d) ? d : []));
          }}
          style={{
            padding: "8px 18px",
            fontSize: "13px",
            fontWeight: activeTab === "CUSTOMERS" ? 700 : 500,
            borderRadius: "6px",
            border: "none",
            background: activeTab === "CUSTOMERS" ? "#2563eb" : "#f1f5f9",
            color: activeTab === "CUSTOMERS" ? "#ffffff" : "#475569",
            cursor: "pointer",
            transition: "all 0.15s ease",
          }}
        >
          👥 Customer Balances
        </button>

        <button
          type="button"
          onClick={() => {
            setActiveTab("PAYMENTS_LOG");
            getPayments().then((d) => setPaymentsLog(Array.isArray(d) ? d : []));
          }}
          style={{
            padding: "8px 18px",
            fontSize: "13px",
            fontWeight: activeTab === "PAYMENTS_LOG" ? 700 : 500,
            borderRadius: "6px",
            border: "none",
            background: activeTab === "PAYMENTS_LOG" ? "#2563eb" : "#f1f5f9",
            color: activeTab === "PAYMENTS_LOG" ? "#ffffff" : "#475569",
            cursor: "pointer",
            transition: "all 0.15s ease",
          }}
        >
          📜 Payment Audit Log
        </button>
      </div>

      {/* =====================================================
          TAB 1: INVOICES & RECEIVABLES TABLE
      ===================================================== */}
      {activeTab === "INVOICES" && (
        <section className="dashboard-card">
          <div className="card-header" style={{ flexWrap: "wrap", gap: "12px" }}>
            <div>
              <h3>Sales Invoices & Receivables Tracking</h3>
              <p>Monitor customer invoice payment progress, due dates, and record payments.</p>
            </div>

            {/* Filters */}
            <form onSubmit={handleFilterSubmit} style={{ display: "flex", gap: "10px", flexWrap: "wrap", alignItems: "center" }}>
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                style={{ padding: "6px 10px", borderRadius: "6px", border: "1px solid #cbd5e1", fontSize: "12px" }}
              >
                <option value="ALL">All Payment Statuses</option>
                <option value="UNPAID">Unpaid Only</option>
                <option value="PARTIALLY_PAID">Partially Paid</option>
                <option value="PAID">Fully Paid</option>
                <option value="OVERDUE">Overdue Only ⚠️</option>
              </select>

              <input
                type="text"
                placeholder="Search invoice, customer, product..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                style={{ padding: "6px 10px", borderRadius: "6px", border: "1px solid #cbd5e1", fontSize: "12px", width: "220px" }}
              />

              <button type="submit" className="primary-button" style={{ padding: "6px 12px", fontSize: "12px" }}>
                Filter
              </button>

              {(statusFilter !== "ALL" || search) && (
                <button
                  type="button"
                  className="secondary-button"
                  style={{ padding: "6px 10px", fontSize: "12px" }}
                  onClick={handleResetFilters}
                >
                  Clear
                </button>
              )}
            </form>
          </div>

          {loading ? (
            <div style={{ textAlign: "center", padding: "40px", color: "#64748b" }}>
              <div className="spinner"></div> Loading invoices...
            </div>
          ) : invoices.length === 0 ? (
            <div style={{ textAlign: "center", padding: "40px", color: "#94a3b8" }}>
              No sales invoices match the selected filter.
            </div>
          ) : (
            <div style={{ overflowX: "auto" }}>
              <table className="table" style={{ margin: 0 }}>
                <thead>
                  <tr>
                    <th>Invoice #</th>
                    <th>Date / Due Date</th>
                    <th>Customer</th>
                    <th>Product</th>
                    <th style={{ textAlign: "right" }}>Total</th>
                    <th style={{ textAlign: "right" }}>Paid</th>
                    <th style={{ textAlign: "right" }}>Balance</th>
                    <th style={{ textAlign: "center" }}>Status</th>
                    <th style={{ textAlign: "right" }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {invoices.map((inv) => {
                    const isFullyPaid = inv.payment_status === "PAID";
                    const isOverdue = inv.is_overdue;

                    return (
                      <tr key={inv.id}>
                        {/* Invoice Number */}
                        <td style={{ fontWeight: 700, color: "#1e293b" }}>
                          {inv.invoice_number}
                          <div style={{ fontSize: "11px", color: "#94a3b8", fontWeight: 400 }}>
                            ID #{inv.id}
                          </div>
                        </td>

                        {/* Date & Due Date */}
                        <td>
                          <div>{formatDate(inv.created_at)}</div>
                          <div style={{ fontSize: "11px", color: isOverdue ? "#dc2626" : "#64748b" }}>
                            Due: {formatDate(inv.due_date)}
                            {isOverdue && (
                              <span
                                style={{
                                  marginLeft: "6px",
                                  background: "#fee2e2",
                                  color: "#b91c1c",
                                  padding: "1px 6px",
                                  borderRadius: "4px",
                                  fontSize: "10px",
                                  fontWeight: 700,
                                }}
                              >
                                {inv.days_overdue}d overdue
                              </span>
                            )}
                          </div>
                        </td>

                        {/* Customer */}
                        <td style={{ fontWeight: 600 }}>{inv.customer_name}</td>

                        {/* Product */}
                        <td style={{ fontSize: "12px", color: "#475569" }}>
                          {inv.product_name} <span style={{ color: "#94a3b8" }}>× {inv.quantity}</span>
                        </td>

                        {/* Total Amount */}
                        <td style={{ textAlign: "right", fontWeight: 600 }}>
                          {formatCurrency(inv.total_amount)}
                        </td>

                        {/* Paid Amount */}
                        <td style={{ textAlign: "right", color: "#15803d", fontWeight: 600 }}>
                          {formatCurrency(inv.paid_amount)}
                        </td>

                        {/* Outstanding Balance */}
                        <td
                          style={{
                            textAlign: "right",
                            fontWeight: 700,
                            color: isFullyPaid ? "#10b981" : isOverdue ? "#dc2626" : "#b45309",
                          }}
                        >
                          {formatCurrency(inv.balance_amount)}
                        </td>

                        {/* Status Badge */}
                        <td style={{ textAlign: "center" }}>
                          <span
                            style={{
                              display: "inline-block",
                              padding: "3px 8px",
                              borderRadius: "12px",
                              fontSize: "11px",
                              fontWeight: 700,
                              background:
                                inv.payment_status === "PAID"
                                  ? "#dcfce7"
                                  : inv.payment_status === "PARTIALLY_PAID"
                                  ? "#fef3c7"
                                  : isOverdue
                                  ? "#fee2e2"
                                  : "#f1f5f9",
                              color:
                                inv.payment_status === "PAID"
                                  ? "#15803d"
                                  : inv.payment_status === "PARTIALLY_PAID"
                                  ? "#b45309"
                                  : isOverdue
                                  ? "#b91c1c"
                                  : "#475569",
                            }}
                          >
                            {inv.payment_status === "PAID"
                              ? "✓ PAID"
                              : inv.payment_status === "PARTIALLY_PAID"
                              ? "PARTIAL"
                              : isOverdue
                              ? "OVERDUE"
                              : "UNPAID"}
                          </span>
                        </td>

                        {/* Actions */}
                        <td style={{ textAlign: "right" }}>
                          <div style={{ display: "inline-flex", gap: "6px", justifyContent: "flex-end" }}>
                            {canManage && !isFullyPaid && (
                              <button
                                type="button"
                                className="primary-button"
                                style={{ padding: "4px 8px", fontSize: "11px" }}
                                onClick={() => openPaymentModal(inv)}
                              >
                                Record Pay
                              </button>
                            )}

                            {inv.payments_count > 0 ? (
                              <button
                                type="button"
                                className="secondary-button"
                                style={{ padding: "4px 8px", fontSize: "11px" }}
                                onClick={() => openInvoiceHistory(inv)}
                              >
                                History ({inv.payments_count})
                              </button>
                            ) : (
                              !canManage && <span style={{ color: "#9ca3af", fontSize: "11px" }}>No Payments</span>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      {/* =====================================================
          TAB 2: CUSTOMER BALANCES VIEW
      ===================================================== */}
      {activeTab === "CUSTOMERS" && (
        <section className="dashboard-card">
          <div className="card-header">
            <div>
              <h3>Customer Accounts & Outstanding Balances</h3>
              <p>Aggregated customer receivables, total billing, and collection records.</p>
            </div>
            <input
              type="text"
              placeholder="Search customer name, phone, email..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyUp={(e) => e.key === "Enter" && loadData()}
              style={{ padding: "6px 10px", borderRadius: "6px", border: "1px solid #cbd5e1", fontSize: "12px", width: "240px" }}
            />
          </div>

          {customerBalances.length === 0 ? (
            <div style={{ textAlign: "center", padding: "40px", color: "#94a3b8" }}>
              No customer balance records found.
            </div>
          ) : (
            <div style={{ overflowX: "auto" }}>
              <table className="table" style={{ margin: 0 }}>
                <thead>
                  <tr>
                    <th>Customer Name</th>
                    <th>Contact Info</th>
                    <th style={{ textAlign: "right" }}>Total Invoiced</th>
                    <th style={{ textAlign: "right" }}>Total Collected</th>
                    <th style={{ textAlign: "right" }}>Outstanding Balance</th>
                    <th style={{ textAlign: "right" }}>Overdue</th>
                    <th style={{ textAlign: "center" }}>Invoices</th>
                    <th>Last Payment</th>
                    <th style={{ textAlign: "right" }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {customerBalances.map((c) => (
                    <tr key={c.customer_id}>
                      <td style={{ fontWeight: 700, color: "#1e293b" }}>{c.customer_name}</td>
                      <td style={{ fontSize: "12px", color: "#64748b" }}>
                        <div>{c.phone || "—"}</div>
                        <div>{c.email || ""}</div>
                      </td>
                      <td style={{ textAlign: "right", fontWeight: 600 }}>{formatCurrency(c.total_invoiced)}</td>
                      <td style={{ textAlign: "right", color: "#16a34a", fontWeight: 600 }}>
                        {formatCurrency(c.total_paid)}
                      </td>
                      <td
                        style={{
                          textAlign: "right",
                          fontWeight: 700,
                          color: c.outstanding_balance > 0 ? "#dc2626" : "#16a34a",
                        }}
                      >
                        {formatCurrency(c.outstanding_balance)}
                      </td>
                      <td
                        style={{
                          textAlign: "right",
                          fontWeight: 600,
                          color: c.overdue_balance > 0 ? "#b91c1c" : "#64748b",
                        }}
                      >
                        {c.overdue_balance > 0 ? formatCurrency(c.overdue_balance) : "—"}
                      </td>
                      <td style={{ textAlign: "center", fontSize: "12px" }}>
                        {c.invoices_count} ({c.unpaid_invoices_count} open)
                      </td>
                      <td style={{ fontSize: "12px" }}>{formatDate(c.last_payment_date)}</td>
                      <td style={{ textAlign: "right" }}>
                        <button
                          type="button"
                          className="secondary-button"
                          style={{ padding: "4px 10px", fontSize: "11px" }}
                          onClick={() => openCustomerHistory(c)}
                        >
                          View Ledger
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      {/* =====================================================
          TAB 3: PAYMENT AUDIT LOG VIEW
      ===================================================== */}
      {activeTab === "PAYMENTS_LOG" && (
        <section className="dashboard-card">
          <div className="card-header">
            <div>
              <h3>Payment Audit Trail</h3>
              <p>Full chronological record of all recorded customer receipts.</p>
            </div>
          </div>

          {paymentsLog.length === 0 ? (
            <div style={{ textAlign: "center", padding: "40px", color: "#94a3b8" }}>
              No customer payments recorded yet.
            </div>
          ) : (
            <div style={{ overflowX: "auto" }}>
              <table className="table" style={{ margin: 0 }}>
                <thead>
                  <tr>
                    <th>Payment #</th>
                    <th>Payment Date</th>
                    <th>Invoice #</th>
                    <th>Customer</th>
                    <th>Payment Method</th>
                    <th>Reference / UTR</th>
                    <th style={{ textAlign: "right" }}>Amount Collected</th>
                    <th>Notes</th>
                  </tr>
                </thead>
                <tbody>
                  {paymentsLog.map((p) => (
                    <tr key={p.id}>
                      <td style={{ fontWeight: 700, color: "#1e293b" }}>{p.payment_number}</td>
                      <td>{formatDate(p.payment_date)}</td>
                      <td style={{ fontWeight: 600, color: "#2563eb" }}>{p.invoice_number}</td>
                      <td style={{ fontWeight: 600 }}>{p.customer_name}</td>
                      <td>
                        <span
                          style={{
                            padding: "2px 8px",
                            borderRadius: "6px",
                            fontSize: "11px",
                            background: "#f1f5f9",
                            color: "#334155",
                            fontWeight: 500,
                          }}
                        >
                          {p.payment_method}
                        </span>
                      </td>
                      <td style={{ fontSize: "12px", color: "#64748b" }}>{p.reference_number || "—"}</td>
                      <td style={{ textAlign: "right", fontWeight: 700, color: "#16a34a" }}>
                        {formatCurrency(p.amount)}
                      </td>
                      <td style={{ fontSize: "12px", color: "#64748b" }}>{p.notes || "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      {/* =====================================================
          RECORD PAYMENT MODAL
      ===================================================== */}
      {payingInvoice && (
        <section
          className="form-card"
          style={{
            position: "fixed",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            zIndex: 1000,
            width: "90%",
            maxWidth: "560px",
            maxHeight: "90vh",
            overflowY: "auto",
            boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.25)",
            border: "2px solid #2563eb",
            background: "#ffffff",
          }}
        >
          <div className="card-header">
            <div>
              <h3>Record Customer Payment</h3>
              <p>
                Against <strong>{payingInvoice.invoice_number}</strong> ({payingInvoice.customer_name})
              </p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setPayingInvoice(null)}
            >
              ✕
            </button>
          </div>

          {/* Invoice Financial Context Card */}
          <div
            style={{
              background: "#f8fafc",
              padding: "12px 16px",
              borderRadius: "8px",
              marginBottom: "16px",
              display: "grid",
              gridTemplateColumns: "1fr 1fr 1fr",
              gap: "10px",
              textAlign: "center",
              fontSize: "12px",
            }}
          >
            <div>
              <span style={{ color: "#64748b" }}>Invoice Total</span>
              <div style={{ fontWeight: 700, fontSize: "14px", color: "#1e293b" }}>
                {formatCurrency(payingInvoice.total_amount)}
              </div>
            </div>
            <div>
              <span style={{ color: "#64748b" }}>Already Paid</span>
              <div style={{ fontWeight: 700, fontSize: "14px", color: "#16a34a" }}>
                {formatCurrency(payingInvoice.paid_amount)}
              </div>
            </div>
            <div>
              <span style={{ color: "#64748b" }}>Remaining Balance</span>
              <div style={{ fontWeight: 800, fontSize: "15px", color: "#dc2626" }}>
                {formatCurrency(payingInvoice.balance_amount)}
              </div>
            </div>
          </div>

          <form onSubmit={handlePaymentSubmit}>
            <div className="form-group" style={{ marginBottom: "14px" }}>
              <label>Payment Amount (₹) *</label>
              <div style={{ display: "flex", gap: "8px" }}>
                <input
                  type="number"
                  required
                  min="0.01"
                  max={payingInvoice.balance_amount}
                  step="0.01"
                  value={payForm.amount}
                  onChange={(e) => setPayForm({ ...payForm, amount: e.target.value })}
                  placeholder={`Up to ${payingInvoice.balance_amount}`}
                  style={{ flex: 1 }}
                />
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => setPayForm({ ...payForm, amount: payingInvoice.balance_amount })}
                  style={{ fontSize: "11px", whiteSpace: "nowrap" }}
                >
                  Pay Full ({formatCurrency(payingInvoice.balance_amount)})
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() =>
                    setPayForm({
                      ...payForm,
                      amount: (payingInvoice.balance_amount / 2).toFixed(2),
                    })
                  }
                  style={{ fontSize: "11px", whiteSpace: "nowrap" }}
                >
                  Pay 50%
                </button>
              </div>
            </div>

            <div className="form-grid" style={{ marginBottom: "14px" }}>
              <div className="form-group">
                <label>Payment Date *</label>
                <input
                  type="date"
                  required
                  value={payForm.payment_date}
                  onChange={(e) => setPayForm({ ...payForm, payment_date: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Payment Method *</label>
                <select
                  value={payForm.payment_method}
                  onChange={(e) => setPayForm({ ...payForm, payment_method: e.target.value })}
                >
                  <option value="Cash">Cash</option>
                  <option value="UPI">UPI</option>
                  <option value="Bank Transfer">Bank Transfer (NEFT/RTGS/IMPS)</option>
                  <option value="Card">Debit / Credit Card</option>
                </select>
              </div>
            </div>

            <div className="form-group" style={{ marginBottom: "14px" }}>
              <label>Reference / UTR / Cheque # (Optional)</label>
              <input
                type="text"
                value={payForm.reference_number}
                onChange={(e) => setPayForm({ ...payForm, reference_number: e.target.value })}
                placeholder="e.g. UPI-123456789 or NEFT Ref"
              />
            </div>

            <div className="form-group" style={{ marginBottom: "18px" }}>
              <label>Notes / Memo</label>
              <input
                type="text"
                value={payForm.notes}
                onChange={(e) => setPayForm({ ...payForm, notes: e.target.value })}
                placeholder="Optional payment notes"
              />
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() => setPayingInvoice(null)}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="primary-button"
                disabled={submittingPayment}
              >
                {submittingPayment ? "Recording Payment..." : "Confirm & Save Payment"}
              </button>
            </div>
          </form>
        </section>
      )}

      {/* Backdrop for Payment Modal */}
      {payingInvoice && (
        <div
          onClick={() => setPayingInvoice(null)}
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(15, 23, 42, 0.5)",
            zIndex: 999,
          }}
        />
      )}

      {/* =====================================================
          PAYMENT HISTORY DRAWER / MODAL
      ===================================================== */}
      {historyModal.open && (
        <section
          className="dashboard-card"
          style={{
            position: "fixed",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            zIndex: 1000,
            width: "90%",
            maxWidth: "680px",
            maxHeight: "85vh",
            overflowY: "auto",
            boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.25)",
            border: "2px solid #0284c7",
            background: "#ffffff",
          }}
        >
          <div className="card-header">
            <div>
              <h3>{historyModal.title}</h3>
              <p>All recorded payment receipts.</p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setHistoryModal({ open: false, title: "", payments: [], loading: false })}
            >
              ✕ Close
            </button>
          </div>

          {historyModal.loading ? (
            <div style={{ textAlign: "center", padding: "30px", color: "#64748b" }}>
              <div className="spinner"></div> Loading payment records...
            </div>
          ) : historyModal.payments.length === 0 ? (
            <div style={{ textAlign: "center", padding: "30px", color: "#94a3b8" }}>
              No payments have been recorded for this item yet.
            </div>
          ) : (
            <div style={{ overflowX: "auto" }}>
              <table className="table" style={{ margin: 0, fontSize: "12px" }}>
                <thead>
                  <tr>
                    <th>Receipt #</th>
                    <th>Date</th>
                    <th>Method</th>
                    <th>Reference</th>
                    <th style={{ textAlign: "right" }}>Amount</th>
                    <th>Notes</th>
                  </tr>
                </thead>
                <tbody>
                  {historyModal.payments.map((p) => (
                    <tr key={p.id}>
                      <td style={{ fontWeight: 700, color: "#1e293b" }}>{p.payment_number}</td>
                      <td>{formatDate(p.payment_date)}</td>
                      <td>
                        <span
                          style={{
                            padding: "2px 6px",
                            borderRadius: "4px",
                            fontSize: "10px",
                            background: "#e2e8f0",
                            color: "#1e293b",
                          }}
                        >
                          {p.payment_method}
                        </span>
                      </td>
                      <td style={{ color: "#64748b" }}>{p.reference_number || "—"}</td>
                      <td style={{ textAlign: "right", fontWeight: 700, color: "#16a34a" }}>
                        {formatCurrency(p.amount)}
                      </td>
                      <td style={{ color: "#64748b" }}>{p.notes || "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      {/* Backdrop for History Modal */}
      {historyModal.open && (
        <div
          onClick={() => setHistoryModal({ open: false, title: "", payments: [], loading: false })}
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(15, 23, 42, 0.5)",
            zIndex: 999,
          }}
        />
      )}
    </div>
  );
}

export default Receivables;
