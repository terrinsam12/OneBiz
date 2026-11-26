import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { getAnalyticsOverview } from "../../services/api";
import "./Analytics.css";

function getCompanyId() {
  return Number(localStorage.getItem("erp_company_id")) || 1;
}

function formatCurrency(value) {
  const num = Number(value || 0);
  return `₹${num.toLocaleString("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function formatCompactCurrency(value) {
  const num = Number(value || 0);
  if (num >= 10000000) return `₹${(num / 10000000).toFixed(1)}Cr`;
  if (num >= 100000) return `₹${(num / 100000).toFixed(1)}L`;
  if (num >= 1000) return `₹${(num / 1000).toFixed(1)}k`;
  return `₹${num.toLocaleString("en-IN", { maximumFractionDigits: 0 })}`;
}

function formatPercent(value) {
  const number = Number(value || 0);
  return `${number >= 0 ? "+" : ""}${number.toFixed(1)}%`;
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

function Analytics() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadAnalytics() {
    try {
      setLoading(true);
      setError("");

      const companyId = getCompanyId();
      const response = await getAnalyticsOverview(companyId);

      setData(response);
    } catch (err) {
      setError(err.message || "Unable to load business analytics.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAnalytics();

    function handleCompanyChanged() {
      loadAnalytics();
    }

    window.addEventListener("companyChanged", handleCompanyChanged);

    return () => {
      window.removeEventListener("companyChanged", handleCompanyChanged);
    };
  }, []);

  const revenue = data?.revenue_analysis || {};
  const customer = data?.customer_analysis || {};
  const trends = useMemo(() => data?.sales_trends || [], [data]);
  const topProducts = useMemo(() => data?.top_products || [], [data]);
  const profitability = useMemo(() => data?.product_profitability || [], [data]);

  const maxRevenue = useMemo(() => {
    const vals = trends.map((item) => Number(item.revenue || 0));
    return Math.max(...vals, 1);
  }, [trends]);

  const maxProductRevenue = useMemo(() => {
    const vals = topProducts.map((item) => Number(item.revenue || 0));
    return Math.max(...vals, 1);
  }, [topProducts]);

  // Aggregated Summary Calculations
  const totalTrendRevenue = useMemo(() => {
    return trends.reduce((sum, item) => sum + Number(item.revenue || 0), 0);
  }, [trends]);

  const totalTrendUnits = useMemo(() => {
    return trends.reduce((sum, item) => sum + Number(item.units || 0), 0);
  }, [trends]);

  const totalTrendTransactions = useMemo(() => {
    return trends.reduce((sum, item) => sum + Number(item.transactions || 0), 0);
  }, [trends]);

  const avgMonthlyRunRate = useMemo(() => {
    return trends.length > 0 ? totalTrendRevenue / trends.length : 0;
  }, [trends, totalTrendRevenue]);

  const totalProfitabilityRevenue = useMemo(() => {
    return profitability.reduce((sum, item) => sum + Number(item.revenue || 0), 0);
  }, [profitability]);

  const totalProfitabilityCost = useMemo(() => {
    return profitability.reduce((sum, item) => sum + Number(item.estimated_cost || 0), 0);
  }, [profitability]);

  const totalGrossProfit = useMemo(() => {
    return profitability.reduce((sum, item) => sum + Number(item.gross_profit || 0), 0);
  }, [profitability]);

  const blendedMargin = useMemo(() => {
    if (totalProfitabilityRevenue <= 0) return 0;
    return (totalGrossProfit / totalProfitabilityRevenue) * 100;
  }, [totalProfitabilityRevenue, totalGrossProfit]);

  if (loading) {
    return (
      <div className="analytics-page">
        <div className="dashboard-card" style={{ padding: "60px 20px", textAlign: "center" }}>
          <div className="spinner" style={{ margin: "0 auto 16px" }}></div>
          <p style={{ margin: 0, fontSize: "15px", color: "#64748b", fontWeight: 600 }}>
            Analyzing financial records &amp; business performance...
          </p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="analytics-page">
        <div className="dashboard-card" style={{ padding: "40px 30px", borderLeft: "4px solid #ef4444" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "8px" }}>
            <span style={{ fontSize: "20px" }}>⚠️</span>
            <h3 style={{ margin: 0, color: "#991b1b" }}>Unable to load Business Analytics</h3>
          </div>
          <p style={{ margin: "0 0 16px", color: "#64748b", fontSize: "14px" }}>{error}</p>
          <button className="primary-button" onClick={loadAnalytics}>
            ↻ Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="analytics-page">
      {/* =====================================================
          1. EXECUTIVE HERO HEADER
      ===================================================== */}
      <div className="analytics-hero-header">
        <div>
          <div className="analytics-header-badges">
            <h1>Business Analytics &amp; Intelligence</h1>
            <span className="analytics-badge-live">
              <span style={{ width: "7px", height: "7px", borderRadius: "50%", background: "#10b981" }}></span>
              Live System
            </span>
          </div>
          <p>
            Real-time performance metrics, sales momentum, product margins, and customer retention insights
          </p>
        </div>

        <div className="analytics-header-actions">
          <div className="analytics-company-pill">
            <span>🏢</span>
            <span>Organization:</span>
            <strong>{data?.company || "Primary Company"}</strong>
          </div>

          <button
            type="button"
            className="secondary-button"
            onClick={loadAnalytics}
            title="Refresh analytics data"
            style={{ fontSize: "13.5px", padding: "9px 16px" }}
          >
            <span>↻</span>
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* =====================================================
          2. ENTERPRISE KPI CARDS (4 PRIMARY METRICS)
      ===================================================== */}
      <div className="analytics-stats-grid">
        {/* KPI 1: TOTAL REVENUE */}
        <div className="analytics-stat-card" style={{ borderLeft: "4px solid #2563eb" }}>
          <div className="stat-header">
            <span>Total Sales Revenue</span>
            <div className="analytics-stat-icon revenue">💰</div>
          </div>
          <div className="analytics-stat-value" style={{ color: "#1d4ed8" }}>
            {formatCurrency(revenue.total_revenue)}
          </div>
          <div className="analytics-stat-footer">
            <span className={`analytics-trend-badge ${Number(revenue.revenue_growth_pct) >= 0 ? "positive" : "neutral"}`}>
              {Number(revenue.revenue_growth_pct) >= 0 ? "↑" : "↓"} {formatPercent(revenue.revenue_growth_pct)} MoM
            </span>
            <span>Best: {revenue.best_month || "Nov 2026"}</span>
          </div>
        </div>

        {/* KPI 2: UNITS SOLD */}
        <div className="analytics-stat-card" style={{ borderLeft: "4px solid #059669" }}>
          <div className="stat-header">
            <span>Volume Dispatched</span>
            <div className="analytics-stat-icon units">📦</div>
          </div>
          <div className="analytics-stat-value" style={{ color: "#047857" }}>
            {Number(revenue.total_units || 0).toLocaleString("en-IN")} Units
          </div>
          <div className="analytics-stat-footer">
            <span className="analytics-trend-badge positive">
              ✓ Invoiced
            </span>
            <span>{revenue.total_transactions || 0} Sales Orders</span>
          </div>
        </div>

        {/* KPI 3: AVERAGE ORDER VALUE */}
        <div className="analytics-stat-card" style={{ borderLeft: "4px solid #d97706" }}>
          <div className="stat-header">
            <span>Avg. Order Value (AOV)</span>
            <div className="analytics-stat-icon aov">🛒</div>
          </div>
          <div className="analytics-stat-value" style={{ color: "#b45309" }}>
            {formatCurrency(revenue.average_order_value)}
          </div>
          <div className="analytics-stat-footer">
            <span className="analytics-trend-badge neutral">
              Per Transaction
            </span>
            <span>Ticket Average</span>
          </div>
        </div>

        {/* KPI 4: GROSS OPERATING MARGIN */}
        <div className="analytics-stat-card" style={{ borderLeft: "4px solid #7c3aed" }}>
          <div className="stat-header">
            <span>Gross Operating Margin</span>
            <div className="analytics-stat-icon margin">💎</div>
          </div>
          <div className="analytics-stat-value" style={{ color: "#6d28d9" }}>
            {blendedMargin.toFixed(1)}%
          </div>
          <div className="analytics-stat-footer">
            <span className="analytics-trend-badge purple">
              {formatCurrency(totalGrossProfit)} Profit
            </span>
            <span>Net of Product COGS</span>
          </div>
        </div>
      </div>

      {/* =====================================================
          3. HERO SALES TRENDS & REVENUE MOMENTUM (HISTOGRAM)
      ===================================================== */}
      <section className="analytics-chart-card">
        <div className="analytics-card-header">
          <div>
            <h3>Monthly Sales &amp; Revenue Momentum</h3>
            <p>Historical invoiced sales distribution over the past 6 rolling months</p>
          </div>

          <div className="analytics-header-metrics">
            <div className="analytics-pill-metric highlight">
              <span>🏆 Peak Month:</span>
              <strong>{revenue.best_month || "Nov 2026"} ({formatCurrency(revenue.best_month_revenue || revenue.total_revenue)})</strong>
            </div>

            <div className="analytics-pill-metric">
              <span>6-Mo Total:</span>
              <strong>{formatCurrency(totalTrendRevenue)}</strong>
            </div>
          </div>
        </div>

        {trends.length === 0 ? (
          <div className="empty-state" style={{ padding: "40px 20px", textAlign: "center" }}>
            No historical sales transactions found for this company.
          </div>
        ) : (
          <div className="analytics-chart-container">
            {/* Visual Histogram Stage */}
            <div className="analytics-histogram-stage">
              {trends.map((item) => {
                const itemRev = Number(item.revenue || 0);
                const heightPct = itemRev > 0 ? Math.max(16, Math.round((itemRev / maxRevenue) * 100)) : 6;
                const isActive = itemRev > 0;

                return (
                  <div className="analytics-bar-column" key={item.month}>
                    {/* Top Value Label */}
                    <span className={`analytics-bar-val-label ${isActive ? "" : "zero"}`}>
                      {isActive ? formatCompactCurrency(itemRev) : "—"}
                    </span>

                    {/* Column Pillar */}
                    <div className="analytics-bar-track">
                      <div
                        className={`analytics-bar-pillar ${isActive ? "active" : "zero"}`}
                        style={{ height: `${heightPct}%` }}
                        title={`${item.label}: ${formatCurrency(itemRev)} (${item.transactions} orders · ${item.units} units)`}
                      />
                    </div>

                    {/* Bottom Label */}
                    <span className="analytics-bar-month-label">
                      {item.label}
                    </span>
                    <span className="analytics-bar-month-sub">
                      {item.transactions} ord · {item.units} units
                    </span>
                  </div>
                );
              })}
            </div>

            {/* Secondary Momentum Strip */}
            <div className="analytics-momentum-strip">
              <div className="analytics-momentum-card">
                <div className="analytics-momentum-icon">🚀</div>
                <div className="analytics-momentum-content">
                  <span>Revenue Velocity</span>
                  <strong>{formatPercent(revenue.revenue_growth_pct)} Momentum</strong>
                  <small>Month-over-month performance trend</small>
                </div>
              </div>

              <div className="analytics-momentum-card">
                <div className="analytics-momentum-icon">📅</div>
                <div className="analytics-momentum-content">
                  <span>Monthly Run-Rate</span>
                  <strong>{formatCurrency(avgMonthlyRunRate)} / mo</strong>
                  <small>6-month rolling sales average</small>
                </div>
              </div>

              <div className="analytics-momentum-card">
                <div className="analytics-momentum-icon">📊</div>
                <div className="analytics-momentum-content">
                  <span>Fulfillment Pipeline</span>
                  <strong>{totalTrendTransactions} Orders · {totalTrendUnits} Units</strong>
                  <small>Cumulative customer dispatches</small>
                </div>
              </div>
            </div>
          </div>
        )}
      </section>

      {/* =====================================================
          4. TWO-COLUMN: TOP PRODUCTS & CUSTOMER ANALYSIS
      ===================================================== */}
      <div className="analytics-two-column">
        {/* LEFT: TOP PRODUCTS RANKING */}
        <section className="analytics-chart-card">
          <div className="analytics-card-header">
            <div>
              <h3>Top Products &amp; Volume Leaders</h3>
              <p>Ranked by invoiced sales revenue and volume share</p>
            </div>
            <span style={{ fontSize: "12px", color: "#64748b", fontWeight: 600 }}>
              {topProducts.length} Active SKUs
            </span>
          </div>

          {topProducts.length === 0 ? (
            <div className="empty-state" style={{ padding: "30px 20px", textAlign: "center" }}>
              No product sales recorded yet.
            </div>
          ) : (
            <div className="analytics-product-list">
              {topProducts.slice(0, 5).map((item, index) => {
                const itemRev = Number(item.revenue || 0);
                const width = (itemRev / maxProductRevenue) * 100;
                const sharePct = revenue.total_revenue > 0 ? ((itemRev / revenue.total_revenue) * 100).toFixed(1) : "0.0";

                const rankClass = index === 0 ? "rank-1" : index === 1 ? "rank-2" : index === 2 ? "rank-3" : "rank-other";
                const rankIcon = index === 0 ? "🥇" : index === 1 ? "🥈" : index === 2 ? "🥉" : `#${index + 1}`;

                return (
                  <div className="analytics-product-row" key={item.product_id || index}>
                    <div className={`analytics-rank-badge ${rankClass}`}>
                      {rankIcon}
                    </div>

                    <div className="analytics-product-meta">
                      <div className="analytics-product-title-row">
                        <span className="analytics-product-name" title={item.product}>
                          {item.product}
                        </span>
                        <span className="analytics-product-revenue">
                          {formatCurrency(item.revenue)}
                        </span>
                      </div>

                      <div className="analytics-progress-bar-wrap">
                        <div
                          className="analytics-progress-bar-fill"
                          style={{ width: `${Math.max(width, 4)}%` }}
                        />
                      </div>

                      <div className="analytics-product-sub-row">
                        <span>
                          {item.units_sold} units sold &bull; {sharePct}% revenue share
                        </span>
                        <span className={`analytics-stock-chip ${item.current_stock > 10 ? "healthy" : "low"}`}>
                          {item.current_stock > 10 ? `Stock: ${item.current_stock}` : `Low Stock: ${item.current_stock}`}
                        </span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        {/* RIGHT: CUSTOMER ANALYSIS & COHORTS */}
        <section className="analytics-chart-card">
          <div className="analytics-card-header">
            <div>
              <h3>Customer Intelligence &amp; Accounts</h3>
              <p>Client purchasing volume, retention rates and top revenue accounts</p>
            </div>
          </div>

          {/* 4 Stat Tiles */}
          <div className="analytics-customer-tiles">
            <div className="analytics-customer-tile">
              <div className="analytics-customer-tile-icon">👥</div>
              <div className="analytics-customer-tile-content">
                <span>Total Directory</span>
                <strong>{customer.total_customers || 0} Accounts</strong>
              </div>
            </div>

            <div className="analytics-customer-tile">
              <div className="analytics-customer-tile-icon">🛍️</div>
              <div className="analytics-customer-tile-content">
                <span>Active Buyers</span>
                <strong>{customer.customers_with_sales || 0} Clients</strong>
              </div>
            </div>

            <div className="analytics-customer-tile">
              <div className="analytics-customer-tile-icon">🔁</div>
              <div className="analytics-customer-tile-content">
                <span>Repeat Buyers</span>
                <strong>{customer.repeat_customers || 0} Clients</strong>
              </div>
            </div>

            <div className="analytics-customer-tile">
              <div className="analytics-customer-tile-icon">🛡️</div>
              <div className="analytics-customer-tile-content">
                <span>Account Health</span>
                <strong>{customer.inactive_customers || 0} Inactive</strong>
              </div>
            </div>
          </div>

          {/* Top Customer Accounts Table */}
          {customer.top_customers?.length ? (
            <div className="table-wrapper" style={{ marginTop: "4px" }}>
              <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                  <tr style={{ background: "#f8fafc" }}>
                    <th style={{ padding: "12px 14px", fontSize: "11px", fontWeight: 700, color: "#475569", textTransform: "uppercase" }}>
                      Customer Account
                    </th>
                    <th style={{ padding: "12px 14px", fontSize: "11px", fontWeight: 700, color: "#475569", textTransform: "uppercase" }}>
                      Orders
                    </th>
                    <th style={{ padding: "12px 14px", fontSize: "11px", fontWeight: 700, color: "#475569", textTransform: "uppercase", textAlign: "right" }}>
                      Invoiced Revenue
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {customer.top_customers.slice(0, 5).map((item) => {
                    const initials = (item.customer || "CU").slice(0, 2).toUpperCase();
                    return (
                      <tr key={item.customer_id} style={{ borderBottom: "1px solid #f1f5f9" }}>
                        <td style={{ padding: "12px 14px" }}>
                          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                            <div className="analytics-avatar-badge">{initials}</div>
                            <div>
                              <strong style={{ display: "block", color: "#0f172a", fontSize: "13.5px" }}>
                                {item.customer}
                              </strong>
                              {item.last_purchase && (
                                <span style={{ fontSize: "11px", color: "#94a3b8" }}>
                                  Last order: {formatDate(item.last_purchase)}
                                </span>
                              )}
                            </div>
                          </div>
                        </td>
                        <td style={{ padding: "12px 14px", fontSize: "13px", color: "#475569" }}>
                          <span style={{ padding: "3px 8px", background: "#f1f5f9", borderRadius: "6px", fontWeight: 600 }}>
                            {item.transactions} orders
                          </span>
                        </td>
                        <td style={{ padding: "12px 14px", textAlign: "right", fontSize: "14px", fontWeight: 800, color: "#0f172a" }}>
                          {formatCurrency(item.revenue)}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state" style={{ padding: "24px 20px", textAlign: "center", color: "#94a3b8" }}>
              No customer-attributed orders recorded yet.
            </div>
          )}
        </section>
      </div>

      {/* =====================================================
          5. PRODUCT PROFITABILITY & UNIT ECONOMICS MATRIX
      ===================================================== */}
      <section className="analytics-chart-card">
        <div className="analytics-card-header">
          <div>
            <h3>Product Profitability &amp; Unit Economics</h3>
            <p>Detailed cost of goods sold (COGS), gross margins, and realized profit per SKU line</p>
          </div>

          <div className="analytics-header-metrics">
            <div className="analytics-pill-metric">
              <span>Total Gross Profit:</span>
              <strong style={{ color: "#047857" }}>{formatCurrency(totalGrossProfit)}</strong>
            </div>

            <div className="analytics-pill-metric highlight">
              <span>Blended Margin:</span>
              <strong>{blendedMargin.toFixed(1)}%</strong>
            </div>
          </div>
        </div>

        {profitability.length === 0 ? (
          <div className="empty-state" style={{ padding: "40px 20px", textAlign: "center" }}>
            No product profitability data available yet.
          </div>
        ) : (
          <div className="table-wrapper">
            <table className="analytics-profit-table" style={{ width: "100%", borderCollapse: "collapse" }}>
              <thead>
                <tr>
                  <th style={{ textAlign: "left" }}>Product SKU / Name</th>
                  <th style={{ textAlign: "center" }}>Units Sold</th>
                  <th style={{ textAlign: "right" }}>Invoiced Revenue</th>
                  <th style={{ textAlign: "right" }}>Estimated COGS</th>
                  <th style={{ textAlign: "right" }}>Gross Profit</th>
                  <th style={{ textAlign: "center" }}>Margin %</th>
                  <th style={{ textAlign: "center" }}>Margin Health</th>
                </tr>
              </thead>

              <tbody>
                {profitability.map((item) => {
                  const margin = Number(item.margin_pct || 0);
                  const isHealthy = margin >= 20;
                  const isModerate = margin >= 5 && margin < 20;

                  const marginClass = isHealthy ? "healthy" : isModerate ? "moderate" : "low";
                  const statusLabel = isHealthy ? "★ Healthy" : isModerate ? "● Moderate" : "⚠️ Narrow";

                  return (
                    <tr key={item.product_id}>
                      <td>
                        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                          <span style={{ fontSize: "16px" }}>📦</span>
                          <strong style={{ color: "#0f172a", fontSize: "14px" }}>
                            {item.product}
                          </strong>
                        </div>
                      </td>

                      <td style={{ textAlign: "center" }}>
                        <span style={{ padding: "4px 10px", background: "#f1f5f9", borderRadius: "12px", fontSize: "12.5px", fontWeight: 700, color: "#334155" }}>
                          {item.units_sold} pcs
                        </span>
                      </td>

                      <td style={{ textAlign: "right", fontWeight: 700 }}>
                        {formatCurrency(item.revenue)}
                      </td>

                      <td style={{ textAlign: "right", color: "#64748b" }}>
                        {formatCurrency(item.estimated_cost)}
                      </td>

                      <td style={{ textAlign: "right" }}>
                        <strong style={{ color: Number(item.gross_profit) >= 0 ? "#047857" : "#b91c1c", fontSize: "14.5px" }}>
                          {Number(item.gross_profit) >= 0 ? "+" : ""}
                          {formatCurrency(item.gross_profit)}
                        </strong>
                      </td>

                      <td style={{ textAlign: "center" }}>
                        <span className={`analytics-margin-pill ${marginClass}`}>
                          {margin.toFixed(1)}%
                        </span>
                      </td>

                      <td style={{ textAlign: "center" }}>
                        <span style={{ fontSize: "12px", fontWeight: 600, color: isHealthy ? "#047857" : isModerate ? "#b45309" : "#b91c1c" }}>
                          {statusLabel}
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>

              {/* Aggregated Totals Footer */}
              <tfoot>
                <tr>
                  <td>Portfolio Total ({profitability.length} SKUs)</td>
                  <td style={{ textAlign: "center" }}>
                    {profitability.reduce((sum, item) => sum + Number(item.units_sold || 0), 0)} pcs
                  </td>
                  <td style={{ textAlign: "right" }}>
                    {formatCurrency(totalProfitabilityRevenue)}
                  </td>
                  <td style={{ textAlign: "right", color: "#64748b" }}>
                    {formatCurrency(totalProfitabilityCost)}
                  </td>
                  <td style={{ textAlign: "right", color: "#047857", fontSize: "15px" }}>
                    +{formatCurrency(totalGrossProfit)}
                  </td>
                  <td style={{ textAlign: "center" }}>
                    <span className={`analytics-margin-pill ${blendedMargin >= 20 ? "healthy" : blendedMargin >= 5 ? "moderate" : "low"}`}>
                      {blendedMargin.toFixed(1)}%
                    </span>
                  </td>
                  <td style={{ textAlign: "center" }}>
                    <span style={{ fontSize: "12px", fontWeight: 700, color: "#047857" }}>
                      Portfolio Margin
                    </span>
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        )}
      </section>

      {/* =====================================================
          6. PREDICTIVE AI INTELLIGENCE ADVISORY BANNER
      ===================================================== */}
      <div className="analytics-ai-banner">
        <div className="analytics-ai-banner-left">
          <div className="analytics-ai-icon-circle">⚡</div>
          <div className="analytics-ai-banner-text">
            <h4>Ready for Predictive Intelligence &amp; Demand Forecasting?</h4>
            <p>
              Leverage machine learning algorithms to forecast next month's SKU sales, identify stockout risks early,
              and receive automated inventory replenishment suggestions.
            </p>
          </div>
        </div>

        <Link
          to="/ai-forecast"
          className="primary-button"
          style={{ padding: "10px 20px", fontSize: "14px", textDecoration: "none", whiteSpace: "nowrap" }}
        >
          <span>Explore AI Demand Forecast →</span>
        </Link>
      </div>
    </div>
  );
}

export default Analytics;
