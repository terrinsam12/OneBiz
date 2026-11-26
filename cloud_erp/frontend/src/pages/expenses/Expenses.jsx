import { useEffect, useState, useMemo } from "react";
import {
  getExpenses,
  getExpense,
  getExpenseSummary,
  createExpense,
  updateExpense,
  deleteExpense,
  uploadReceipt,
} from "../../services/api";

const CATEGORIES = [
  "Rent",
  "Salary",
  "Electricity",
  "Transport",
  "Maintenance",
  "Marketing",
  "Internet",
  "Others",
];

const PAYMENT_METHODS = [
  "Cash",
  "Bank Transfer",
  "UPI",
  "Credit Card",
  "Debit Card",
  "Cheque",
  "Others",
];

const CATEGORY_COLORS = {
  Rent: { bg: "#eff6ff", color: "#1d4ed8", bar: "#2563eb" },
  Salary: { bg: "#ede9fe", color: "#6d28d9", bar: "#7c3aed" },
  Electricity: { bg: "#fef3c7", color: "#b45309", bar: "#d97706" },
  Transport: { bg: "#e0f2fe", color: "#0369a1", bar: "#0284c7" },
  Maintenance: { bg: "#ffedd5", color: "#c2410c", bar: "#ea580c" },
  Marketing: { bg: "#fce7f3", color: "#be185d", bar: "#db2777" },
  Internet: { bg: "#d1fae5", color: "#047857", bar: "#059669" },
  Others: { bg: "#f1f5f9", color: "#475569", bar: "#64748b" },
};

function Expenses() {
  const [expenses, setExpenses] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Filters
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("ALL");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  // Modals & States
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingExpense, setEditingExpense] = useState(null);
  const [viewingReceipt, setViewingReceipt] = useState(null);
  const [saving, setSaving] = useState(false);
  const [uploadingReceipt, setUploadingReceipt] = useState(false);

  // Form State
  const [form, setForm] = useState({
    title: "",
    category: "Others",
    amount: "",
    expense_date: new Date().toISOString().split("T")[0],
    payment_method: "Cash",
    description: "",
    receipt_url: "",
    receipt_name: "",
  });

  // Current logged in user
  const currentUser = useMemo(() => {
    try {
      return JSON.parse(localStorage.getItem("erp_user")) || null;
    } catch {
      return null;
    }
  }, []);

  const canManage = currentUser?.role === "COMPANY_ADMIN" || currentUser?.role === "MANAGER";

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    try {
      setLoading(true);
      setError("");
      await Promise.all([loadExpenses(), loadSummary()]);
    } catch (err) {
      setError(err.message || "Failed to load expenses data.");
    } finally {
      setLoading(false);
    }
  }

  async function loadExpenses() {
    const params = {};
    if (search) params.search = search;
    if (categoryFilter && categoryFilter !== "ALL") params.category = categoryFilter;
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    const data = await getExpenses(params);
    setExpenses(Array.isArray(data) ? data : []);
  }

  async function loadSummary() {
    try {
      const sum = await getExpenseSummary();
      setSummary(sum);
    } catch {
      // Non-blocking
    }
  }

  function handleFilterSubmit(e) {
    if (e) e.preventDefault();
    loadExpenses();
  }

  function handleResetFilters() {
    setSearch("");
    setCategoryFilter("ALL");
    setStartDate("");
    setEndDate("");
    setTimeout(() => {
      loadExpenses();
    }, 0);
  }

  // Handle Receipt Upload
  async function handleReceiptUpload(e) {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      setUploadingReceipt(true);
      setError("");
      const res = await uploadReceipt(file);
      setForm((prev) => ({
        ...prev,
        receipt_url: res.file_url,
        receipt_name: res.file_name,
      }));
      setSuccess(`Receipt "${res.file_name}" uploaded.`);
    } catch (err) {
      setError(err.message || "Failed to upload receipt.");
    } finally {
      setUploadingReceipt(false);
    }
  }

  // Handle Add Expense
  async function handleCreateSubmit(e) {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!form.title.trim()) {
      setError("Expense title is required.");
      return;
    }
    if (!form.amount || Number(form.amount) <= 0) {
      setError("Please enter a valid expense amount.");
      return;
    }

    try {
      setSaving(true);
      await createExpense({
        title: form.title.trim(),
        category: form.category,
        amount: Number(form.amount),
        expense_date: form.expense_date,
        payment_method: form.payment_method,
        description: form.description.trim(),
        receipt_url: form.receipt_url,
        receipt_name: form.receipt_name,
      });

      setSuccess(`Expense "${form.title}" recorded successfully.`);
      setShowAddModal(false);
      setForm({
        title: "",
        category: "Others",
        amount: "",
        expense_date: new Date().toISOString().split("T")[0],
        payment_method: "Cash",
        description: "",
        receipt_url: "",
        receipt_name: "",
      });
      await Promise.all([loadExpenses(), loadSummary()]);
    } catch (err) {
      setError(err.message || "Failed to record expense.");
    } finally {
      setSaving(false);
    }
  }

  // Open Edit Modal
  function openEditModal(exp) {
    setError("");
    setSuccess("");
    setEditingExpense(exp);
    setForm({
      title: exp.title || "",
      category: exp.category || "Others",
      amount: exp.amount || "",
      expense_date: exp.expense_date || new Date().toISOString().split("T")[0],
      payment_method: exp.payment_method || "Cash",
      description: exp.description || "",
      receipt_url: exp.receipt_url || "",
      receipt_name: exp.receipt_name || "",
    });
  }

  // Handle Edit Submit
  async function handleEditSubmit(e) {
    e.preventDefault();
    if (!editingExpense) return;

    setError("");
    setSuccess("");

    if (!form.title.trim()) {
      setError("Expense title is required.");
      return;
    }
    if (!form.amount || Number(form.amount) <= 0) {
      setError("Please enter a valid expense amount.");
      return;
    }

    try {
      setSaving(true);
      await updateExpense(editingExpense.id, {
        title: form.title.trim(),
        category: form.category,
        amount: Number(form.amount),
        expense_date: form.expense_date,
        payment_method: form.payment_method,
        description: form.description.trim(),
        receipt_url: form.receipt_url,
        receipt_name: form.receipt_name,
      });

      setSuccess(`Expense "${form.title}" updated successfully.`);
      setEditingExpense(null);
      await Promise.all([loadExpenses(), loadSummary()]);
    } catch (err) {
      setError(err.message || "Failed to update expense.");
    } finally {
      setSaving(false);
    }
  }

  // Handle Delete Expense
  async function handleDelete(exp) {
    if (!window.confirm(`Are you sure you want to delete expense "${exp.title}" (₹${exp.amount})?`)) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      await deleteExpense(exp.id);
      setSuccess(`Expense "${exp.title}" deleted.`);
      await Promise.all([loadExpenses(), loadSummary()]);
    } catch (err) {
      setError(err.message || "Failed to delete expense.");
    }
  }

  // Max value calculation for visual charts
  const maxCategoryAmount = useMemo(() => {
    if (!summary?.category_breakdown || summary.category_breakdown.length === 0) return 1;
    return Math.max(...summary.category_breakdown.map((c) => Number(c.amount || 0)), 1);
  }, [summary]);

  const maxMonthAmount = useMemo(() => {
    if (!summary?.monthly_trends || summary.monthly_trends.length === 0) return 1;
    return Math.max(...summary.monthly_trends.map((m) => Number(m.amount || 0)), 1);
  }, [summary]);

  return (
    <div className="page-content">
      {/* =====================================================
          PAGE HEADER
      ===================================================== */}
      <div className="page-heading">
        <div>
          <h1>Expense Management</h1>
          <p>Track business operational overheads, manage recurring expenditures, and monitor departmental spending.</p>
        </div>

        {canManage && (
          <button
            type="button"
            className="primary-button"
            onClick={() => {
              setError("");
              setSuccess("");
              setForm({
                title: "",
                category: "Others",
                amount: "",
                expense_date: new Date().toISOString().split("T")[0],
                payment_method: "Cash",
                description: "",
                receipt_url: "",
                receipt_name: "",
              });
              setShowAddModal(true);
            }}
          >
            + Add Expense
          </button>
        )}
      </div>

      {/* =====================================================
          SUMMARY STATS
      ===================================================== */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon revenue" style={{ background: "#fee2e2", color: "#dc2626" }}>
            💸
          </div>
          <div>
            <span>TOTAL EXPENDITURE</span>
            <h2>
              ₹
              {Number(summary?.total_expenses ?? 0).toLocaleString("en-IN", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
              })}
            </h2>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon transactions" style={{ background: "#fef3c7", color: "#d97706" }}>
            📅
          </div>
          <div>
            <span>THIS MONTH'S SPEND</span>
            <h2>
              ₹
              {Number(summary?.this_month_expenses ?? 0).toLocaleString("en-IN", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
              })}
            </h2>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon customers" style={{ background: "#ede9fe", color: "#7c3aed" }}>
            🏷
          </div>
          <div>
            <span>TOP EXPENSE CATEGORY</span>
            <h2 style={{ fontSize: "19px" }}>{summary?.top_category || "None"}</h2>
            {summary?.top_category_amount ? (
              <small style={{ color: "#6b7280" }}>
                ₹{Number(summary.top_category_amount).toLocaleString("en-IN")}
              </small>
            ) : null}
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon units" style={{ background: "#e0f2fe", color: "#0284c7" }}>
            🧾
          </div>
          <div>
            <span>RECORDED EXPENSES</span>
            <h2>{summary?.expense_count ?? expenses.length}</h2>
          </div>
        </div>
      </div>

      {/* =====================================================
          ALERTS
      ===================================================== */}
      {success && <div className="success-message">✓ {success}</div>}
      {error && <div className="error-message">⚠ {error}</div>}

      {/* =====================================================
          VISUAL CHARTS & REPORTS SECTION
      ===================================================== */}
      <div className="analytics-two-column" style={{ marginBottom: "20px" }}>
        {/* Category Breakdown */}
        <section className="dashboard-card analytics-card" style={{ marginBottom: 0 }}>
          <div className="card-header">
            <div>
              <h3>Category-Wise Spending</h3>
              <p>Breakdown of expenditures across business categories</p>
            </div>
          </div>

          {!summary?.category_breakdown || summary.category_breakdown.length === 0 ? (
            <div className="empty-state">No expense categories recorded yet.</div>
          ) : (
            <div className="rank-list">
              {summary.category_breakdown.map((cat, idx) => {
                const colors = CATEGORY_COLORS[cat.category] || CATEGORY_COLORS.Others;
                const width = (Number(cat.amount) / maxCategoryAmount) * 100;

                return (
                  <div className="rank-item" key={cat.category || idx}>
                    <div
                      className="rank-number"
                      style={{
                        background: colors.bg,
                        color: colors.color,
                        fontWeight: 700,
                      }}
                    >
                      {idx + 1}
                    </div>

                    <div className="rank-content" style={{ flex: 1 }}>
                      <div
                        style={{
                          display: "flex",
                          justifyContent: "space-between",
                          alignItems: "center",
                          marginBottom: "5px",
                        }}
                      >
                        <div>
                          <strong>{cat.category}</strong>
                          <span style={{ fontSize: "11px", color: "#9ca3af", marginLeft: "8px" }}>
                            {cat.count} record{cat.count === 1 ? "" : "s"} ({cat.percentage}%)
                          </span>
                        </div>
                        <strong style={{ fontSize: "13px", color: "#111827" }}>
                          ₹{Number(cat.amount).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                        </strong>
                      </div>

                      <div className="mini-track">
                        <div
                          className="mini-bar"
                          style={{
                            width: `${Math.max(width, 3)}%`,
                            background: colors.bar,
                          }}
                        />
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        {/* Monthly Trend */}
        <section className="dashboard-card analytics-card" style={{ marginBottom: 0 }}>
          <div className="card-header">
            <div>
              <h3>Monthly Expense Trend</h3>
              <p>Spending trend for the past 6 billing cycles</p>
            </div>
          </div>

          {!summary?.monthly_trends || summary.monthly_trends.length === 0 ? (
            <div className="empty-state">No historical expense data available.</div>
          ) : (
            <div className="trend-chart">
              {summary.monthly_trends.map((m) => {
                const width = (Number(m.amount) / maxMonthAmount) * 100;

                return (
                  <div className="trend-row" key={m.month}>
                    <div className="trend-label">
                      <strong>{m.label}</strong>
                      <span>{m.count} expense{m.count === 1 ? "" : "s"}</span>
                    </div>

                    <div className="trend-track">
                      <div
                        className="trend-bar"
                        style={{
                          width: `${Math.max(width, 2)}%`,
                          background: "#dc2626",
                        }}
                      />
                    </div>

                    <strong className="trend-value">
                      ₹{Number(m.amount).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                    </strong>
                  </div>
                );
              })}
            </div>
          )}
        </section>
      </div>

      {/* =====================================================
          FILTER TOOLBAR
      ===================================================== */}
      <form onSubmit={handleFilterSubmit} className="filter-toolbar">
        <div className="search-input-wrapper">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            placeholder="Search by title, description, or payment method..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <div className="filter-select-wrapper" style={{ minWidth: "160px" }}>
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
          >
            <option value="ALL">All Categories</option>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <span style={{ fontSize: "13px", fontWeight: 600, color: "#64748b" }}>From:</span>
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            style={{
              padding: "10px 12px",
              borderRadius: "8px",
              border: "1px solid #cbd5e1",
              fontSize: "13.5px",
            }}
          />
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <span style={{ fontSize: "13px", fontWeight: 600, color: "#64748b" }}>To:</span>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            style={{
              padding: "10px 12px",
              borderRadius: "8px",
              border: "1px solid #cbd5e1",
              fontSize: "13.5px",
            }}
          />
        </div>

        <div className="filter-actions">
          <button
            type="submit"
            className="primary-button"
            style={{ padding: "11px 18px", fontSize: "14px" }}
          >
            Filter
          </button>

          {(search || categoryFilter !== "ALL" || startDate || endDate) && (
            <button
              type="button"
              className="secondary-button"
              onClick={handleResetFilters}
              style={{ padding: "11px 16px", fontSize: "14px" }}
            >
              Reset
            </button>
          )}
        </div>
      </form>

      {/* =====================================================
          ADD EXPENSE MODAL
      ===================================================== */}
      {showAddModal && (
        <section className="form-card" style={{ marginBottom: "25px", border: "2px solid #2563eb" }}>
          <div className="card-header">
            <div>
              <h3>Record New Expense</h3>
              <p>Log a business expenditure with category classification and optional receipt upload.</p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setShowAddModal(false)}
            >
              ✕ Cancel
            </button>
          </div>

          <form onSubmit={handleCreateSubmit}>
            <div className="form-grid">
              <div className="form-group">
                <label>Expense Title / Purpose *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Workshop Monthly Lease"
                  value={form.title}
                  onChange={(e) => setForm({ ...form, title: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Category *</label>
                <select
                  required
                  value={form.category}
                  onChange={(e) => setForm({ ...form, category: e.target.value })}
                >
                  {CATEGORIES.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Amount (₹) *</label>
                <input
                  type="number"
                  required
                  min="0.01"
                  step="0.01"
                  placeholder="e.g. 15000.00"
                  value={form.amount}
                  onChange={(e) => setForm({ ...form, amount: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Expense Date *</label>
                <input
                  type="date"
                  required
                  value={form.expense_date}
                  onChange={(e) => setForm({ ...form, expense_date: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Payment Method *</label>
                <select
                  required
                  value={form.payment_method}
                  onChange={(e) => setForm({ ...form, payment_method: e.target.value })}
                >
                  {PAYMENT_METHODS.map((m) => (
                    <option key={m} value={m}>
                      {m}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Receipt Attachment (Optional)</label>
                <input
                  type="file"
                  accept="image/*,.pdf"
                  onChange={handleReceiptUpload}
                  disabled={uploadingReceipt}
                />
                {uploadingReceipt && <small style={{ color: "#2563eb" }}>Uploading receipt file...</small>}
                {form.receipt_name && (
                  <small style={{ color: "#16a34a" }}>✓ Attached: {form.receipt_name}</small>
                )}
              </div>

              <div className="form-group" style={{ gridColumn: "1 / -1" }}>
                <label>Description / Notes</label>
                <input
                  type="text"
                  placeholder="e.g. Invoice #INV-2026-89 paid via RTGS to landlord"
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </div>
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() => setShowAddModal(false)}
              >
                Cancel
              </button>
              <button type="submit" className="primary-button" disabled={saving || uploadingReceipt}>
                {saving ? "Saving..." : "Save Expense"}
              </button>
            </div>
          </form>
        </section>
      )}

      {/* =====================================================
          EDIT EXPENSE MODAL
      ===================================================== */}
      {editingExpense && (
        <section className="form-card" style={{ marginBottom: "25px", border: "2px solid #7c3aed" }}>
          <div className="card-header">
            <div>
              <h3>Edit Expense: {editingExpense.title}</h3>
              <p>Update expenditure amount, category classification, or receipt attachment.</p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setEditingExpense(null)}
            >
              ✕ Cancel
            </button>
          </div>

          <form onSubmit={handleEditSubmit}>
            <div className="form-grid">
              <div className="form-group">
                <label>Expense Title / Purpose *</label>
                <input
                  type="text"
                  required
                  value={form.title}
                  onChange={(e) => setForm({ ...form, title: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Category *</label>
                <select
                  required
                  value={form.category}
                  onChange={(e) => setForm({ ...form, category: e.target.value })}
                >
                  {CATEGORIES.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Amount (₹) *</label>
                <input
                  type="number"
                  required
                  min="0.01"
                  step="0.01"
                  value={form.amount}
                  onChange={(e) => setForm({ ...form, amount: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Expense Date *</label>
                <input
                  type="date"
                  required
                  value={form.expense_date}
                  onChange={(e) => setForm({ ...form, expense_date: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Payment Method *</label>
                <select
                  required
                  value={form.payment_method}
                  onChange={(e) => setForm({ ...form, payment_method: e.target.value })}
                >
                  {PAYMENT_METHODS.map((m) => (
                    <option key={m} value={m}>
                      {m}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Replace Receipt Attachment</label>
                <input
                  type="file"
                  accept="image/*,.pdf"
                  onChange={handleReceiptUpload}
                  disabled={uploadingReceipt}
                />
                {uploadingReceipt && <small style={{ color: "#2563eb" }}>Uploading...</small>}
                {form.receipt_name && (
                  <small style={{ color: "#16a34a" }}>Current file: {form.receipt_name}</small>
                )}
              </div>

              <div className="form-group" style={{ gridColumn: "1 / -1" }}>
                <label>Description / Notes</label>
                <input
                  type="text"
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </div>
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() => setEditingExpense(null)}
              >
                Cancel
              </button>
              <button type="submit" className="primary-button" disabled={saving || uploadingReceipt}>
                {saving ? "Saving..." : "Update Expense"}
              </button>
            </div>
          </form>
        </section>
      )}

      {/* =====================================================
          RECEIPT PREVIEW MODAL
      ===================================================== */}
      {viewingReceipt && (
        <section className="dashboard-card" style={{ marginBottom: "25px", border: "2px solid #0284c7" }}>
          <div className="card-header">
            <div>
              <h3>Receipt Document: {viewingReceipt.receipt_name || "Attachment"}</h3>
              <p>Attached to expense: <strong>{viewingReceipt.title}</strong> (₹{viewingReceipt.amount})</p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setViewingReceipt(null)}
            >
              ✕ Close
            </button>
          </div>

          <div style={{ textAlign: "center", padding: "20px", background: "#f8fafc", borderRadius: "8px" }}>
            {viewingReceipt.receipt_url?.endsWith(".pdf") ? (
              <div>
                <p>PDF Document Attached: {viewingReceipt.receipt_name}</p>
                <a
                  href={`http://127.0.0.1:8000${viewingReceipt.receipt_url}`}
                  target="_blank"
                  rel="noreferrer"
                  className="primary-button"
                  style={{ display: "inline-block", textDecoration: "none" }}
                >
                  Download / Open PDF
                </a>
              </div>
            ) : (
              <div>
                <img
                  src={`http://127.0.0.1:8000${viewingReceipt.receipt_url}`}
                  alt="Receipt Preview"
                  style={{
                    maxWidth: "100%",
                    maxHeight: "450px",
                    objectFit: "contain",
                    borderRadius: "6px",
                    border: "1px solid #e5e7eb",
                  }}
                  onError={(e) => {
                    e.target.style.display = "none";
                  }}
                />
                <div style={{ marginTop: "12px" }}>
                  <a
                    href={`http://127.0.0.1:8000${viewingReceipt.receipt_url}`}
                    target="_blank"
                    rel="noreferrer"
                    style={{ color: "#2563eb", fontSize: "13px", fontWeight: 600 }}
                  >
                    Open original file in new tab ↗
                  </a>
                </div>
              </div>
            )}
          </div>
        </section>
      )}

      {/* =====================================================
          EXPENSES TABLE
      ===================================================== */}
      <section className="dashboard-card">
        <div className="card-header">
          <div>
            <h3>Business Expenses</h3>
            <p>Showing {expenses.length} record{expenses.length === 1 ? "" : "s"}</p>
          </div>
        </div>

        {loading ? (
          <div className="loading-page">
            <div className="spinner"></div>
            Loading expenses...
          </div>
        ) : expenses.length === 0 ? (
          <div className="empty-state">
            No expenses found. {canManage ? "Click \"+ Add Expense\" above to record your first expenditure." : ""}
          </div>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Expense Title</th>
                  <th>Category</th>
                  <th>Payment Method</th>
                  <th>Amount</th>
                  <th>Receipt</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {expenses.map((exp) => {
                  const colors = CATEGORY_COLORS[exp.category] || CATEGORY_COLORS.Others;

                  return (
                    <tr key={exp.id}>
                      <td>
                        <strong>
                          {exp.expense_date
                            ? new Date(exp.expense_date).toLocaleDateString()
                            : "N/A"}
                        </strong>
                      </td>

                      <td>
                        <div>
                          <strong>{exp.title}</strong>
                          {exp.description && (
                            <div style={{ fontSize: "11px", color: "#6b7280", marginTop: "2px" }}>
                              {exp.description}
                            </div>
                          )}
                        </div>
                      </td>

                      <td>
                        <span
                          style={{
                            display: "inline-block",
                            padding: "3px 9px",
                            borderRadius: "12px",
                            fontSize: "11px",
                            fontWeight: 700,
                            background: colors.bg,
                            color: colors.color,
                          }}
                        >
                          {exp.category}
                        </span>
                      </td>

                      <td>
                        <span
                          style={{
                            display: "inline-block",
                            padding: "3px 8px",
                            borderRadius: "6px",
                            fontSize: "11px",
                            background: "#f1f5f9",
                            color: "#334155",
                          }}
                        >
                          {exp.payment_method}
                        </span>
                      </td>

                      <td className="amount" style={{ color: "#dc2626" }}>
                        ₹{Number(exp.amount || 0).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                      </td>

                      <td>
                        {exp.receipt_url ? (
                          <button
                            type="button"
                            onClick={() => setViewingReceipt(exp)}
                            style={{
                              background: "#e0f2fe",
                              color: "#0369a1",
                              border: "1px solid #bae6fd",
                              borderRadius: "6px",
                              padding: "4px 8px",
                              fontSize: "11px",
                              fontWeight: 600,
                              cursor: "pointer",
                            }}
                          >
                            📎 View
                          </button>
                        ) : (
                          <span style={{ color: "#9ca3af", fontSize: "12px" }}>—</span>
                        )}
                      </td>

                      <td style={{ textAlign: "right" }}>
                        <div
                          style={{
                            display: "inline-flex",
                            gap: "8px",
                            justifyContent: "flex-end",
                          }}
                        >
                          {canManage ? (
                            <>
                              <button
                                type="button"
                                className="secondary-button"
                                style={{ padding: "6px 12px", fontSize: "12px" }}
                                onClick={() => openEditModal(exp)}
                              >
                                Edit
                              </button>

                              <button
                                type="button"
                                className="danger-button"
                                style={{ padding: "6px 12px", fontSize: "12px" }}
                                onClick={() => handleDelete(exp)}
                              >
                                Delete
                              </button>
                            </>
                          ) : (
                            <span style={{ color: "#9ca3af", fontSize: "11px" }}>View Only</span>
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
    </div>
  );
}

export default Expenses;
