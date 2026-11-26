import { useEffect, useMemo, useState } from "react";
import { getFinancialOverview } from "../../services/api";

function formatCurrency(val) {
  const num = Number(val || 0);
  return `₹${num.toLocaleString("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function formatPercent(val) {
  const num = Number(val || 0);
  return `${num >= 0 ? "+" : ""}${num.toFixed(1)}%`;
}

function Finance() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Date filters
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [activePreset, setActivePreset] = useState("ALL");

  async function loadFinancials(customStart, customEnd) {
    try {
      setLoading(true);
      setError("");

      const start = customStart !== undefined ? customStart : startDate;
      const end = customEnd !== undefined ? customEnd : endDate;

      const params = {};
      if (start) params.startDate = start;
      if (end) params.endDate = end;

      const res = await getFinancialOverview(params);
      setData(res);
    } catch (err) {
      setError(err.message || "Failed to load financial overview.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadFinancials();

    function handleCompanyChange() {
      loadFinancials();
    }

    window.addEventListener("companyChanged", handleCompanyChange);
    return () => {
      window.removeEventListener("companyChanged", handleCompanyChange);
    };
  }, []);

  // Filter Presets
  function applyPreset(preset) {
    setActivePreset(preset);
    const now = new Date();
    let s = "";
    let e = "";

    if (preset === "THIS_MONTH") {
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, "0");
      s = `${year}-${month}-01`;
      e = now.toISOString().split("T")[0];
    } else if (preset === "LAST_30_DAYS") {
      const d = new Date();
      d.setDate(d.getDate() - 30);
      s = d.toISOString().split("T")[0];
      e = now.toISOString().split("T")[0];
    } else if (preset === "THIS_QUARTER") {
      const currQuarter = Math.floor(now.getMonth() / 3);
      const startQuarterMonth = String(currQuarter * 3 + 1).padStart(2, "0");
      s = `${now.getFullYear()}-${startQuarterMonth}-01`;
      e = now.toISOString().split("T")[0];
    } else if (preset === "THIS_YEAR") {
      s = `${now.getFullYear()}-01-01`;
      e = now.toISOString().split("T")[0];
    } else if (preset === "ALL") {
      s = "";
      e = "";
    }

    setStartDate(s);
    setEndDate(e);
    loadFinancials(s, e);
  }

  function handleFilterSubmit(ev) {
    if (ev) ev.preventDefault();
    setActivePreset("CUSTOM");
    loadFinancials();
  }

  function handleResetFilters() {
    setActivePreset("ALL");
    setStartDate("");
    setEndDate("");
    loadFinancials("", "");
  }

  // Max value calculation for visual chart scaling
  const maxMonthlyMetric = useMemo(() => {
    if (!data?.monthly_trends || data.monthly_trends.length === 0) return 1;
    let max = 1;
    for (const m of data.monthly_trends) {
      if (m.revenue > max) max = m.revenue;
      if (m.operating_expenses > max) max = m.operating_expenses;
      if (m.procurement_spending > max) max = m.procurement_spending;
      if (m.cogs > max) max = m.cogs;
    }
    return max;
  }, [data]);

  if (loading && !data) {
    return (
      <div className="loading-page">
        <div className="spinner"></div>
        Loading Financial Overview & Profitability Analytics...
      </div>
    );
  }

  return (
    <div className="page-container">
      {/* =====================================================
          HEADER
      ===================================================== */}
      <div className="page-header">
        <div>
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <h2>Financial Overview & Profitability</h2>
            <span
              style={{
                background: "#dcfce7",
                color: "#15803d",
                padding: "3px 10px",
                borderRadius: "12px",
                fontSize: "12px",
                fontWeight: 600,
              }}
            >
              Audited P&L Analysis
            </span>
          </div>
          <p>
            Real-time financial performance, Gross Margin, Net Operating Profit, COGS, and Procurement Outflow for{" "}
            <strong>{data?.company_name || "Active Company"}</strong>.
          </p>
        </div>

        <button
          type="button"
          className="secondary-button"
          onClick={() => loadFinancials()}
          title="Refresh Financial Analytics"
        >
          🔄 Refresh
        </button>
      </div>

      {/* Error Notice */}
      {error && (
        <div className="error-card" style={{ marginBottom: "20px" }}>
          <strong>Error loading financials:</strong> {error}
        </div>
      )}

      {/* =====================================================
          DATE-RANGE FILTER BAR
      ===================================================== */}
      <section
        className="dashboard-card"
        style={{
          marginBottom: "25px",
          padding: "16px 20px",
          background: "#ffffff",
          boxShadow: "0 1px 3px rgba(0,0,0,0.06)",
        }}
      >
        <div
          style={{
            display: "flex",
            flexWrap: "wrap",
            justifyContent: "space-between",
            alignItems: "center",
            gap: "14px",
          }}
        >
          {/* Preset Buttons */}
          <div style={{ display: "flex", gap: "8px", flexWrap: "wrap", alignItems: "center" }}>
            <span style={{ fontSize: "12px", fontWeight: 600, color: "#64748b", marginRight: "4px" }}>
              TIMEFRAME:
            </span>
            {[
              { id: "ALL", label: "All Time" },
              { id: "THIS_MONTH", label: "This Month" },
              { id: "LAST_30_DAYS", label: "Last 30 Days" },
              { id: "THIS_QUARTER", label: "This Quarter" },
              { id: "THIS_YEAR", label: "This Year" },
            ].map((p) => (
              <button
                key={p.id}
                type="button"
                onClick={() => applyPreset(p.id)}
                style={{
                  padding: "5px 12px",
                  fontSize: "12px",
                  borderRadius: "6px",
                  fontWeight: 500,
                  cursor: "pointer",
                  border: activePreset === p.id ? "1px solid #2563eb" : "1px solid #e2e8f0",
                  background: activePreset === p.id ? "#eff6ff" : "#ffffff",
                  color: activePreset === p.id ? "#1d4ed8" : "#475569",
                  transition: "all 0.15s ease",
                }}
              >
                {p.label}
              </button>
            ))}
          </div>

          {/* Date Picker Form */}
          <form
            onSubmit={handleFilterSubmit}
            style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
              <label style={{ fontSize: "12px", color: "#64748b", fontWeight: 500 }}>From:</label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                style={{
                  padding: "5px 8px",
                  fontSize: "12px",
                  borderRadius: "6px",
                  border: "1px solid #cbd5e1",
                }}
              />
            </div>

            <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
              <label style={{ fontSize: "12px", color: "#64748b", fontWeight: 500 }}>To:</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                style={{
                  padding: "5px 8px",
                  fontSize: "12px",
                  borderRadius: "6px",
                  border: "1px solid #cbd5e1",
                }}
              />
            </div>

            <button type="submit" className="primary-button" style={{ padding: "6px 14px", fontSize: "12px" }}>
              Apply Range
            </button>

            {(startDate || endDate || activePreset !== "ALL") && (
              <button
                type="button"
                className="secondary-button"
                style={{ padding: "6px 12px", fontSize: "12px" }}
                onClick={handleResetFilters}
              >
                Clear
              </button>
            )}
          </form>
        </div>
      </section>

      {/* =====================================================
          PRIMARY KPI PROFITABILITY METRIC CARDS
      ===================================================== */}
      <section className="stats-grid" style={{ marginBottom: "25px" }}>
        {/* Total Revenue */}
        <div className="stat-card" style={{ borderLeft: "4px solid #3b82f6" }}>
          <div className="stat-header">
            <span>TOTAL REVENUE</span>
            <span style={{ fontSize: "18px" }}>💰</span>
          </div>
          <div className="stat-value" style={{ color: "#1d4ed8" }}>
            {formatCurrency(data?.total_revenue)}
          </div>
          <div className="stat-change" style={{ color: "#64748b" }}>
            <span>{data?.total_sales_count || 0} sales transactions</span>
            <span style={{ marginLeft: "8px" }}>• {data?.total_units_sold || 0} units</span>
          </div>
        </div>

        {/* Cost of Goods Sold */}
        <div className="stat-card" style={{ borderLeft: "4px solid #f97316" }}>
          <div className="stat-header">
            <span>COST OF GOODS SOLD (COGS)</span>
            <span style={{ fontSize: "18px" }}>🏷️</span>
          </div>
          <div className="stat-value" style={{ color: "#ea580c" }}>
            {formatCurrency(data?.cogs)}
          </div>
          <div className="stat-change" style={{ color: "#64748b" }}>
            <span>Direct product production/inventory cost</span>
          </div>
        </div>

        {/* Gross Profit & Margin */}
        <div className="stat-card" style={{ borderLeft: "4px solid #10b981" }}>
          <div className="stat-header">
            <span>GROSS PROFIT</span>
            <span
              style={{
                fontSize: "11px",
                fontWeight: 700,
                padding: "2px 8px",
                borderRadius: "10px",
                background: (data?.gross_profit || 0) >= 0 ? "#d1fae5" : "#fee2e2",
                color: (data?.gross_profit || 0) >= 0 ? "#065f46" : "#b91c1c",
              }}
            >
              {data?.gross_profit_margin || 0}% Margin
            </span>
          </div>
          <div
            className="stat-value"
            style={{ color: (data?.gross_profit || 0) >= 0 ? "#059669" : "#dc2626" }}
          >
            {formatCurrency(data?.gross_profit)}
          </div>
          <div className="stat-change" style={{ color: "#64748b" }}>
            <span>Revenue minus COGS</span>
          </div>
        </div>

        {/* Total Operating Expenses */}
        <div className="stat-card" style={{ borderLeft: "4px solid #ef4444" }}>
          <div className="stat-header">
            <span>TOTAL OPERATING EXPENSES</span>
            <span style={{ fontSize: "18px" }}>💸</span>
          </div>
          <div className="stat-value" style={{ color: "#dc2626" }}>
            {formatCurrency(data?.total_operating_expenses)}
          </div>
          <div className="stat-change" style={{ color: "#64748b" }}>
            <span>{data?.expense_count || 0} recorded business expenses</span>
          </div>
        </div>

        {/* Net Operating Profit */}
        <div
          className="stat-card"
          style={{
            borderLeft: `4px solid ${(data?.net_operating_profit || 0) >= 0 ? "#16a34a" : "#dc2626"}`,
            background: (data?.net_operating_profit || 0) >= 0 ? "#f0fdf4" : "#fef2f2",
          }}
        >
          <div className="stat-header">
            <span style={{ fontWeight: 700 }}>NET OPERATING PROFIT</span>
            <span
              style={{
                fontSize: "11px",
                fontWeight: 700,
                padding: "2px 8px",
                borderRadius: "10px",
                background: (data?.net_operating_profit || 0) >= 0 ? "#bbf7d0" : "#fecaca",
                color: (data?.net_operating_profit || 0) >= 0 ? "#14532d" : "#991b1b",
              }}
            >
              {data?.net_operating_profit_margin || 0}% Margin
            </span>
          </div>
          <div
            className="stat-value"
            style={{ color: (data?.net_operating_profit || 0) >= 0 ? "#15803d" : "#b91c1c" }}
          >
            {formatCurrency(data?.net_operating_profit)}
          </div>
          <div className="stat-change" style={{ color: "#64748b" }}>
            <span>Gross Profit minus Operating Expenses</span>
          </div>
        </div>

        {/* Procurement / Restocking Spending */}
        <div className="stat-card" style={{ borderLeft: "4px solid #8b5cf6" }}>
          <div className="stat-header">
            <span>PROCUREMENT SPENDING</span>
            <span style={{ fontSize: "18px" }}>📥</span>
          </div>
          <div className="stat-value" style={{ color: "#6d28d9" }}>
            {formatCurrency(data?.procurement_spending)}
          </div>
          <div className="stat-change" style={{ color: "#64748b" }}>
            <span>
              {data?.procurement_count || 0} Received POs • {data?.pending_purchases_count || 0} Pending (
              {formatCurrency(data?.pending_purchases_amount)})
            </span>
          </div>
        </div>

        {/* Customer Receivables Integration */}
        <div className="stat-card" style={{ borderLeft: "4px solid #06b6d4" }}>
          <div className="stat-header">
            <span>CUSTOMER RECEIVABLES</span>
            <span style={{ fontSize: "18px" }}>💳</span>
          </div>
          <div className="stat-value" style={{ color: "#0891b2" }}>
            {formatCurrency(data?.total_receivables)}
          </div>
          <div className="stat-change" style={{ color: (data?.total_overdue_receivables || 0) > 0 ? "#dc2626" : "#64748b" }}>
            <span>
              {(data?.total_overdue_receivables || 0) > 0
                ? `${formatCurrency(data?.total_overdue_receivables)} overdue`
                : "Healthy: No overdue invoices"}
            </span>
          </div>
        </div>
      </section>

      {/* =====================================================
          EXECUTIVE P&L STATEMENT SUMMARY (WATERFALL VIEW)
      ===================================================== */}
      <section className="dashboard-card" style={{ marginBottom: "25px" }}>
        <div className="card-header">
          <div>
            <h3>Profit & Loss (P&L) Statement Breakdown</h3>
            <p>Direct accounting reconciliation from revenue down to bottom-line net profit.</p>
          </div>
          <div style={{ fontSize: "12px", color: "#64748b" }}>
            Currency: <strong>{data?.currency || "INR"} (₹)</strong>
          </div>
        </div>

        <div style={{ overflowX: "auto" }}>
          <table className="table" style={{ margin: 0 }}>
            <thead>
              <tr>
                <th style={{ width: "45%" }}>Financial Line Item</th>
                <th style={{ width: "20%", textAlign: "right" }}>Amount (₹)</th>
                <th style={{ width: "15%", textAlign: "right" }}>% of Revenue</th>
                <th style={{ width: "20%" }}>Accounting Classification</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style={{ fontWeight: 600 }}>
                  <span style={{ color: "#2563eb", marginRight: "8px" }}>●</span> Gross Sales & Billing Revenue
                </td>
                <td style={{ textAlign: "right", fontWeight: 700, color: "#1e293b" }}>
                  {formatCurrency(data?.total_revenue)}
                </td>
                <td style={{ textAlign: "right", fontWeight: 600 }}>100.0%</td>
                <td style={{ color: "#64748b", fontSize: "12px" }}>Operating Inflow</td>
              </tr>

              <tr>
                <td style={{ paddingLeft: "25px", color: "#ea580c" }}>
                  <span style={{ marginRight: "8px" }}>−</span> Less: Cost of Goods Sold (COGS)
                </td>
                <td style={{ textAlign: "right", color: "#ea580c", fontWeight: 600 }}>
                  ({formatCurrency(data?.cogs)})
                </td>
                <td style={{ textAlign: "right", color: "#ea580c" }}>
                  {data?.total_revenue > 0
                    ? ((data.cogs / data.total_revenue) * 100).toFixed(1) + "%"
                    : "0.0%"}
                </td>
                <td style={{ color: "#64748b", fontSize: "12px" }}>Direct Product Cost</td>
              </tr>

              <tr style={{ background: "#f8fafc", borderTop: "2px solid #e2e8f0" }}>
                <td style={{ fontWeight: 700, color: "#0f172a" }}>
                  <span style={{ color: "#10b981", marginRight: "8px" }}>=</span> GROSS OPERATING PROFIT
                </td>
                <td
                  style={{
                    textAlign: "right",
                    fontWeight: 700,
                    color: (data?.gross_profit || 0) >= 0 ? "#059669" : "#dc2626",
                  }}
                >
                  {formatCurrency(data?.gross_profit)}
                </td>
                <td
                  style={{
                    textAlign: "right",
                    fontWeight: 700,
                    color: (data?.gross_profit || 0) >= 0 ? "#059669" : "#dc2626",
                  }}
                >
                  {data?.gross_profit_margin || 0}%
                </td>
                <td style={{ fontSize: "12px", fontWeight: 600, color: "#059669" }}>Gross Margin Metric</td>
              </tr>

              <tr>
                <td style={{ paddingLeft: "25px", color: "#dc2626" }}>
                  <span style={{ marginRight: "8px" }}>−</span> Less: Operating Expenses (OpEx)
                </td>
                <td style={{ textAlign: "right", color: "#dc2626", fontWeight: 600 }}>
                  ({formatCurrency(data?.total_operating_expenses)})
                </td>
                <td style={{ textAlign: "right", color: "#dc2626" }}>
                  {data?.total_revenue > 0
                    ? ((data.total_operating_expenses / data.total_revenue) * 100).toFixed(1) + "%"
                    : "0.0%"}
                </td>
                <td style={{ color: "#64748b", fontSize: "12px" }}>General & Administrative Overhead</td>
              </tr>

              <tr
                style={{
                  background: (data?.net_operating_profit || 0) >= 0 ? "#f0fdf4" : "#fef2f2",
                  borderTop: "2px solid #cbd5e1",
                  borderBottom: "2px solid #cbd5e1",
                }}
              >
                <td style={{ fontWeight: 800, fontSize: "14px", color: "#0f172a" }}>
                  <span style={{ color: (data?.net_operating_profit || 0) >= 0 ? "#16a34a" : "#dc2626", marginRight: "8px" }}>
                    ★
                  </span>{" "}
                  NET OPERATING PROFIT
                </td>
                <td
                  style={{
                    textAlign: "right",
                    fontWeight: 800,
                    fontSize: "15px",
                    color: (data?.net_operating_profit || 0) >= 0 ? "#15803d" : "#b91c1c",
                  }}
                >
                  {formatCurrency(data?.net_operating_profit)}
                </td>
                <td
                  style={{
                    textAlign: "right",
                    fontWeight: 800,
                    fontSize: "14px",
                    color: (data?.net_operating_profit || 0) >= 0 ? "#15803d" : "#b91c1c",
                  }}
                >
                  {data?.net_operating_profit_margin || 0}%
                </td>
                <td style={{ fontSize: "12px", fontWeight: 700, color: (data?.net_operating_profit || 0) >= 0 ? "#15803d" : "#b91c1c" }}>
                  Final Bottom-Line Margin
                </td>
              </tr>

              <tr style={{ background: "#fafafa" }}>
                <td style={{ color: "#6b21a8" }}>
                  <span style={{ marginRight: "8px" }}>ℹ</span> Separate: Inventory Procurement Outflow
                </td>
                <td style={{ textAlign: "right", color: "#6b21a8", fontWeight: 600 }}>
                  {formatCurrency(data?.procurement_spending)}
                </td>
                <td style={{ textAlign: "right", color: "#6b21a8" }}>
                  {data?.total_revenue > 0
                    ? ((data.procurement_spending / data.total_revenue) * 100).toFixed(1) + "%"
                    : "0.0%"}
                </td>
                <td style={{ color: "#7e22ce", fontSize: "12px" }}>
                  Capital / Restocking (Excluded from Net Profit to avoid double-counting COGS)
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      {/* =====================================================
          BEST & LOWEST PERFORMING MONTHS HIGHLIGHTS
      ===================================================== */}
      <section style={{ marginBottom: "25px" }}>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))", gap: "16px" }}>
          {/* Best Month by Revenue */}
          <div
            style={{
              padding: "16px 20px",
              borderRadius: "8px",
              background: "#ffffff",
              border: "1px solid #bfdbfe",
              borderLeft: "4px solid #2563eb",
            }}
          >
            <div style={{ fontSize: "11px", fontWeight: 700, color: "#1d4ed8", textTransform: "uppercase" }}>
              🏆 Best Month by Revenue
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", marginTop: "8px" }}>
              <span style={{ fontSize: "16px", fontWeight: 700, color: "#0f172a" }}>
                {data?.best_month_revenue?.label || "No data"}
              </span>
              <span style={{ fontSize: "15px", fontWeight: 700, color: "#2563eb" }}>
                {formatCurrency(data?.best_month_revenue?.amount)}
              </span>
            </div>
          </div>

          {/* Lowest Month by Revenue */}
          <div
            style={{
              padding: "16px 20px",
              borderRadius: "8px",
              background: "#ffffff",
              border: "1px solid #e2e8f0",
              borderLeft: "4px solid #94a3b8",
            }}
          >
            <div style={{ fontSize: "11px", fontWeight: 700, color: "#64748b", textTransform: "uppercase" }}>
              📉 Lowest Month by Revenue
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", marginTop: "8px" }}>
              <span style={{ fontSize: "16px", fontWeight: 700, color: "#0f172a" }}>
                {data?.lowest_month_revenue?.label || "No data"}
              </span>
              <span style={{ fontSize: "15px", fontWeight: 700, color: "#64748b" }}>
                {formatCurrency(data?.lowest_month_revenue?.amount)}
              </span>
            </div>
          </div>

          {/* Best Month by Net Profit */}
          <div
            style={{
              padding: "16px 20px",
              borderRadius: "8px",
              background: "#ffffff",
              border: "1px solid #bbf7d0",
              borderLeft: "4px solid #16a34a",
            }}
          >
            <div style={{ fontSize: "11px", fontWeight: 700, color: "#15803d", textTransform: "uppercase" }}>
              ⭐ Best Month by Net Profit
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", marginTop: "8px" }}>
              <span style={{ fontSize: "16px", fontWeight: 700, color: "#0f172a" }}>
                {data?.best_month_profit?.label || "No data"}
              </span>
              <span style={{ fontSize: "15px", fontWeight: 700, color: "#16a34a" }}>
                {formatCurrency(data?.best_month_profit?.amount)}
              </span>
            </div>
          </div>

          {/* Lowest Month by Net Profit */}
          <div
            style={{
              padding: "16px 20px",
              borderRadius: "8px",
              background: "#ffffff",
              border: "1px solid #fecaca",
              borderLeft: "4px solid #ef4444",
            }}
          >
            <div style={{ fontSize: "11px", fontWeight: 700, color: "#b91c1c", textTransform: "uppercase" }}>
              ⚠️ Lowest Month by Net Profit
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", marginTop: "8px" }}>
              <span style={{ fontSize: "16px", fontWeight: 700, color: "#0f172a" }}>
                {data?.lowest_month_profit?.label || "No data"}
              </span>
              <span style={{ fontSize: "15px", fontWeight: 700, color: "#dc2626" }}>
                {formatCurrency(data?.lowest_month_profit?.amount)}
              </span>
            </div>
          </div>
        </div>
      </section>

      {/* =====================================================
          MONTHLY REVENUE VS EXPENSES & NET PROFIT (CHART & TABLE)
      ===================================================== */}
      <section className="dashboard-card" style={{ marginBottom: "25px" }}>
        <div className="card-header">
          <div>
            <h3>Monthly Revenue vs. Total Outflow & Net Profit</h3>
            <p>Comparative timeline of sales inflows, COGS, operational expenses, and resulting profit margins.</p>
          </div>
          <div style={{ display: "flex", gap: "12px", alignItems: "center", fontSize: "12px" }}>
            <span style={{ display: "flex", alignItems: "center", gap: "5px" }}>
              <span style={{ width: "12px", height: "12px", background: "#3b82f6", borderRadius: "2px" }}></span>
              Revenue
            </span>
            <span style={{ display: "flex", alignItems: "center", gap: "5px" }}>
              <span style={{ width: "12px", height: "12px", background: "#f97316", borderRadius: "2px" }}></span>
              COGS
            </span>
            <span style={{ display: "flex", alignItems: "center", gap: "5px" }}>
              <span style={{ width: "12px", height: "12px", background: "#ef4444", borderRadius: "2px" }}></span>
              Operating Expenses
            </span>
            <span style={{ display: "flex", alignItems: "center", gap: "5px" }}>
              <span style={{ width: "12px", height: "12px", background: "#10b981", borderRadius: "2px" }}></span>
              Net Profit
            </span>
          </div>
        </div>

        {/* Visual Monthly Progress Track Bar Charts */}
        <div style={{ display: "flex", flexDirection: "column", gap: "16px", padding: "10px 0" }}>
          {data?.monthly_trends?.map((m) => {
            const revPercent = Math.min(100, Math.round((m.revenue / maxMonthlyMetric) * 100));
            const cogsPercent = Math.min(100, Math.round((m.cogs / maxMonthlyMetric) * 100));
            const expPercent = Math.min(100, Math.round((m.operating_expenses / maxMonthlyMetric) * 100));

            return (
              <div
                key={m.month}
                style={{
                  padding: "12px 16px",
                  background: "#f8fafc",
                  borderRadius: "8px",
                  border: "1px solid #e2e8f0",
                }}
              >
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "8px",
                  }}
                >
                  <span style={{ fontWeight: 700, color: "#1e293b", fontSize: "13px" }}>{m.label}</span>
                  <div style={{ display: "flex", gap: "12px", fontSize: "12px" }}>
                    <span style={{ color: "#1d4ed8" }}>Revenue: {formatCurrency(m.revenue)}</span>
                    <span style={{ color: "#ea580c" }}>COGS: {formatCurrency(m.cogs)}</span>
                    <span style={{ color: "#dc2626" }}>OpEx: {formatCurrency(m.operating_expenses)}</span>
                    <span
                      style={{
                        fontWeight: 700,
                        color: m.net_profit >= 0 ? "#15803d" : "#b91c1c",
                      }}
                    >
                      Net Profit: {formatCurrency(m.net_profit)} ({m.net_margin_pct}%)
                    </span>
                  </div>
                </div>

                {/* Comparative Horizontal Multi-Bars */}
                <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                  {/* Revenue Bar */}
                  <div style={{ display: "flex", alignItems: "center", height: "8px" }}>
                    <div
                      style={{
                        height: "100%",
                        width: `${revPercent}%`,
                        background: "#3b82f6",
                        borderRadius: "4px",
                        transition: "width 0.3s ease",
                      }}
                      title={`Revenue: ${formatCurrency(m.revenue)}`}
                    />
                  </div>
                  {/* COGS Bar */}
                  <div style={{ display: "flex", alignItems: "center", height: "6px" }}>
                    <div
                      style={{
                        height: "100%",
                        width: `${cogsPercent}%`,
                        background: "#f97316",
                        borderRadius: "4px",
                        transition: "width 0.3s ease",
                      }}
                      title={`COGS: ${formatCurrency(m.cogs)}`}
                    />
                  </div>
                  {/* OpEx Bar */}
                  <div style={{ display: "flex", alignItems: "center", height: "6px" }}>
                    <div
                      style={{
                        height: "100%",
                        width: `${expPercent}%`,
                        background: "#ef4444",
                        borderRadius: "4px",
                        transition: "width 0.3s ease",
                      }}
                      title={`Operating Expenses: ${formatCurrency(m.operating_expenses)}`}
                    />
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* =====================================================
          TWO-COLUMN SECTION: EXPENSES & PRODUCT PROFITABILITY
      ===================================================== */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(450px, 1fr))", gap: "25px" }}>
        {/* Left Column: Operating Expense Breakdown */}
        <section className="dashboard-card">
          <div className="card-header">
            <div>
              <h3>Operating Expense Distribution</h3>
              <p>Breakdown by operational category.</p>
            </div>
            <span style={{ fontSize: "12px", color: "#64748b", fontWeight: 600 }}>
              Total: {formatCurrency(data?.total_operating_expenses)}
            </span>
          </div>

          {data?.expense_breakdown?.length === 0 ? (
            <div style={{ textAlign: "center", padding: "30px", color: "#94a3b8" }}>
              No operating expenses recorded for this timeframe.
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: "14px" }}>
              {data?.expense_breakdown?.map((cat) => (
                <div key={cat.category}>
                  <div
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                      fontSize: "13px",
                      marginBottom: "4px",
                    }}
                  >
                    <span style={{ fontWeight: 600, color: "#334155" }}>
                      {cat.category}{" "}
                      <span style={{ color: "#94a3b8", fontSize: "11px", fontWeight: 400 }}>
                        ({cat.count} txns)
                      </span>
                    </span>
                    <span style={{ fontWeight: 700, color: "#0f172a" }}>
                      {formatCurrency(cat.amount)}{" "}
                      <span style={{ color: "#64748b", fontWeight: 400 }}>({cat.percentage}%)</span>
                    </span>
                  </div>
                  <div
                    style={{
                      height: "8px",
                      background: "#f1f5f9",
                      borderRadius: "4px",
                      overflow: "hidden",
                    }}
                  >
                    <div
                      style={{
                        height: "100%",
                        width: `${Math.min(100, cat.percentage)}%`,
                        background:
                          cat.category === "Salary"
                            ? "#3b82f6"
                            : cat.category === "Rent"
                            ? "#8b5cf6"
                            : cat.category === "Electricity"
                            ? "#f59e0b"
                            : cat.category === "Maintenance"
                            ? "#10b981"
                            : "#64748b",
                        borderRadius: "4px",
                      }}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Right Column: Product Profitability Ranking */}
        <section className="dashboard-card">
          <div className="card-header">
            <div>
              <h3>Product Profitability Breakdown</h3>
              <p>Gross margin contribution per product line.</p>
            </div>
          </div>

          {data?.product_profitability?.length === 0 ? (
            <div style={{ textAlign: "center", padding: "30px", color: "#94a3b8" }}>
              No sales recorded for this timeframe.
            </div>
          ) : (
            <div style={{ overflowX: "auto" }}>
              <table className="table" style={{ margin: 0, fontSize: "12px" }}>
                <thead>
                  <tr>
                    <th>Product</th>
                    <th style={{ textAlign: "right" }}>Sold</th>
                    <th style={{ textAlign: "right" }}>Revenue</th>
                    <th style={{ textAlign: "right" }}>COGS</th>
                    <th style={{ textAlign: "right" }}>Gross Profit</th>
                    <th style={{ textAlign: "right" }}>Margin</th>
                  </tr>
                </thead>
                <tbody>
                  {data?.product_profitability?.map((prod) => (
                    <tr key={prod.product_id}>
                      <td style={{ fontWeight: 600, color: "#1e293b" }}>{prod.product_name}</td>
                      <td style={{ textAlign: "right" }}>{prod.units_sold}</td>
                      <td style={{ textAlign: "right" }}>{formatCurrency(prod.revenue)}</td>
                      <td style={{ textAlign: "right", color: "#ea580c" }}>{formatCurrency(prod.cogs)}</td>
                      <td
                        style={{
                          textAlign: "right",
                          fontWeight: 700,
                          color: prod.gross_profit >= 0 ? "#059669" : "#dc2626",
                        }}
                      >
                        {formatCurrency(prod.gross_profit)}
                      </td>
                      <td
                        style={{
                          textAlign: "right",
                          fontWeight: 700,
                          color: prod.margin_pct >= 0 ? "#059669" : "#dc2626",
                        }}
                      >
                        {prod.margin_pct}%
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

export default Finance;
