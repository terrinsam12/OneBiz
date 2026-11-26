import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  getProducts,
  getInventory,
  getCustomers,
  getSales,
  getSalesSummary,
  getFinancialOverview,
  getPurchases,
  getUnreadCount,
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

function Dashboard() {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [salesSummary, setSalesSummary] = useState(null);
  const [financialSummary, setFinancialSummary] = useState(null);
  const [products, setProducts] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [recentSales, setRecentSales] = useState([]);
  const [pendingPurchases, setPendingPurchases] = useState([]);
  const [unreadAlerts, setUnreadAlerts] = useState(0);

  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("erp_user")) || null;
    } catch {
      return null;
    }
  })();

  const companyId = Number(localStorage.getItem("erp_company_id")) || 1;

  useEffect(() => {
    loadDashboardData();
  }, [companyId]);

  async function loadDashboardData() {
    setLoading(true);
    setError("");

    try {
      // Execute parallel calls
      const [
        summaryRes,
        productsRes,
        inventoryRes,
        customersRes,
        salesRes,
        purchasesRes,
        unreadRes,
      ] = await Promise.allSettled([
        getSalesSummary(companyId),
        getProducts(companyId),
        getInventory(companyId),
        getCustomers(companyId),
        getSales(companyId),
        getPurchases(),
        getUnreadCount(),
      ]);

      if (summaryRes.status === "fulfilled") setSalesSummary(summaryRes.value);
      if (productsRes.status === "fulfilled") {
        const val = productsRes.value;
        setProducts(Array.isArray(val) ? val : val?.products || []);
      }
      if (inventoryRes.status === "fulfilled") {
        const val = inventoryRes.value;
        setInventory(Array.isArray(val) ? val : val?.inventory || []);
      }
      if (customersRes.status === "fulfilled") {
        const val = customersRes.value;
        setCustomers(Array.isArray(val) ? val : val?.customers || []);
      }
      if (salesRes.status === "fulfilled") {
        const val = salesRes.value;
        setRecentSales(Array.isArray(val) ? val : val?.sales || []);
      }
      if (purchasesRes.status === "fulfilled") {
        const val = purchasesRes.value;
        const list = Array.isArray(val) ? val : val?.purchases || [];
        setPendingPurchases(list.filter((p) => p.status === "PENDING"));
      }
      if (unreadRes.status === "fulfilled") {
        setUnreadAlerts(unreadRes.value?.unread_count || 0);
      }

      // Try fetching finance overview if Manager or Admin
      if (currentUser?.role !== "STAFF") {
        try {
          const fin = await getFinancialOverview();
          setFinancialSummary(fin);
        } catch {
          // staff or forbidden fallback
        }
      }
    } catch (err) {
      setError(err.message || "Failed to load dashboard data");
    } finally {
      setLoading(false);
    }
  }

  // Calculated Metrics
  const totalSalesRevenue =
    financialSummary?.total_revenue ?? salesSummary?.total_sales ?? 0;
  const grossProfit =
    financialSummary?.gross_profit ?? salesSummary?.estimated_profit ?? 0;
  const grossMargin =
    financialSummary?.gross_profit_margin ?? salesSummary?.profit_margin ?? 0;
  const totalTransactions =
    salesSummary?.total_transactions ?? recentSales.length;
  const totalUnitsSold = salesSummary?.total_units_sold ?? 0;

  const lowStockItems = inventory.filter(
    (item) => Number(item.quantity || 0) <= Number(item.reorder_level ?? 5)
  );

  const outOfStockItems = inventory.filter(
    (item) => Number(item.quantity || 0) <= 0
  );

  const avgOrderValue =
    totalTransactions > 0 ? totalSalesRevenue / totalTransactions : 0;

  // Monthly Sales Visualization calculations
  const salesByMonth = recentSales.reduce((acc, sale) => {
    const d = sale.created_at ? new Date(sale.created_at) : new Date();
    const key = d.toLocaleString("en-US", { month: "short" });
    acc[key] = (acc[key] || 0) + Number(sale.total_amount || 0);
    return acc;
  }, {});

  const months = ["Jun", "Jul", "Aug", "Sep", "Oct", "Nov"];
  const maxMonthVal = Math.max(...months.map((m) => salesByMonth[m] || 0), 100000);

  if (loading) {
    return (
      <div style={{ padding: "80px 20px", textAlign: "center", color: "#64748b" }}>
        <div
          style={{
            width: "36px",
            height: "36px",
            border: "4px solid #2563eb",
            borderTopColor: "transparent",
            borderRadius: "50%",
            animation: "spin 1s linear infinite",
            margin: "0 auto 16px",
          }}
        />
        <strong style={{ fontSize: "16px", color: "#1e293b" }}>Loading Executive Dashboard</strong>
        <p style={{ margin: "6px 0 0", fontSize: "13px" }}>
          Aggregating sales orders, inventory valuation, and real-time business health...
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div
        style={{
          padding: "30px",
          background: "#ffffff",
          borderRadius: "12px",
          border: "1px solid #fecaca",
          textAlign: "center",
        }}
      >
        <div style={{ fontSize: "32px", marginBottom: "8px" }}>⚠️</div>
        <h3 style={{ margin: "0 0 8px", color: "#b91c1c" }}>Unable to load dashboard</h3>
        <p style={{ margin: "0 0 16px", color: "#64748b", fontSize: "14px" }}>{error}</p>
        <button type="button" onClick={loadDashboardData} className="primary-button">
          ↻ Retry Loading
        </button>
      </div>
    );
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "26px" }}>
      {/* =====================================================
          EXECUTIVE HERO HEADER
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
              Executive Dashboard
            </h1>
            <span
              style={{
                fontSize: "12px",
                fontWeight: 700,
                padding: "3px 10px",
                borderRadius: "12px",
                background: "#ecfdf5",
                color: "#047857",
                border: "1px solid #a7f3d0",
              }}
            >
              ● Live System
            </span>
          </div>
          <p style={{ margin: "6px 0 0", fontSize: "14.5px", color: "#64748b" }}>
            Welcome, <strong>{currentUser?.name || "Administrator"}</strong> &bull; Operational
            metrics, financial health and real-time inventory tracking
          </p>
        </div>

        <div style={{ display: "flex", flexWrap: "wrap", alignItems: "center", gap: "12px" }}>
          <button
            type="button"
            onClick={loadDashboardData}
            className="secondary-button"
            title="Refresh dashboard metrics"
            style={{ fontSize: "14px", padding: "10px 18px" }}
          >
            <span>↻</span>
            <span>Refresh</span>
          </button>

          <Link to="/sales" className="primary-button" style={{ fontSize: "14px", padding: "10px 20px" }}>
            <span>+</span>
            <span>New Sale</span>
          </Link>

          <Link
            to="/products"
            className="secondary-button"
            style={{ borderColor: "#cbd5e1", fontSize: "14px", padding: "10px 18px" }}
          >
            <span>+</span>
            <span>Add Product</span>
          </Link>
        </div>
      </div>

      {/* =====================================================
          ROW 1: FOUR PRIMARY ENTERPRISE KPI CARDS
      ===================================================== */}
      <div className="stats-grid">
        {/* KPI 1: TOTAL REVENUE */}
        <div className="stat-card" style={{ borderLeft: "4px solid #16a34a" }}>
          <div className="stat-header">
            <span>Total Sales Revenue</span>
            <div className="stat-icon revenue">₹</div>
          </div>
          <div className="stat-value" style={{ color: "#15803d" }}>
            {formatCurrency(totalSalesRevenue)}
          </div>
          <div className="stat-change">
            <span className="stat-badge positive">↑ Net Invoiced</span>
            <span>Across {totalTransactions} customer orders</span>
          </div>
        </div>

        {/* KPI 2: GROSS OPERATING PROFIT */}
        <div className="stat-card" style={{ borderLeft: "4px solid #2563eb" }}>
          <div className="stat-header">
            <span>Gross Operating Profit</span>
            <div className="stat-icon profit">📈</div>
          </div>
          <div className="stat-value" style={{ color: "#1d4ed8" }}>
            {formatCurrency(grossProfit)}
          </div>
          <div className="stat-change">
            <span className="stat-badge positive">{Number(grossMargin).toFixed(1)}% Margin</span>
            <span>Revenue minus COGS</span>
          </div>
        </div>

        {/* KPI 3: ORDER VOLUME & AVERAGE VALUE */}
        <div className="stat-card" style={{ borderLeft: "4px solid #f59e0b" }}>
          <div className="stat-header">
            <span>Order Pipeline</span>
            <div className="stat-icon transactions">🛍️</div>
          </div>
          <div className="stat-value" style={{ color: "#b45309" }}>
            {totalTransactions} Orders
          </div>
          <div className="stat-change">
            <span className="stat-badge neutral">{totalUnitsSold} Units Sold</span>
            <span>Avg: {formatCurrency(avgOrderValue)}</span>
          </div>
        </div>

        {/* KPI 4: INVENTORY HEALTH & ALERTS */}
        <div className="stat-card" style={{ borderLeft: "4px solid #8b5cf6" }}>
          <div className="stat-header">
            <span>Inventory &amp; Alerts</span>
            <div className="stat-icon customers">📦</div>
          </div>
          <div className="stat-value" style={{ color: "#6d28d9" }}>
            {products.length} Products
          </div>
          <div className="stat-change">
            {unreadAlerts > 0 ? (
              <span className="stat-badge warning">🔔 {unreadAlerts} Alerts</span>
            ) : (
              <span className="stat-badge positive">✓ Healthy Stock</span>
            )}
            <span>
              {lowStockItems.length > 0
                ? `${lowStockItems.length} Low stock`
                : "No stockout risks"}
            </span>
          </div>
        </div>
      </div>

      {/* =====================================================
          ROW 2: VISUAL ANALYTICS & INVENTORY BREAKDOWN
      ===================================================== */}
      <div className="dashboard-grid">
        {/* CHART: MONTHLY REVENUE MOMENTUM */}
        <div className="dashboard-card">
          <div className="card-header">
            <div>
              <h3>Monthly Sales &amp; Revenue Momentum</h3>
              <p>Actual invoiced revenue distribution across billing periods</p>
            </div>
            <Link to="/analytics">Analytics Deep Dive →</Link>
          </div>

          {/* Clean SVG Bar / Area Visualization */}
          <div
            style={{
              height: "240px",
              display: "flex",
              alignItems: "flex-end",
              gap: "20px",
              paddingTop: "24px",
              paddingBottom: "12px",
              borderBottom: "1px solid #f1f5f9",
            }}
          >
            {months.map((m) => {
              const val = salesByMonth[m] || (m === "Nov" ? totalSalesRevenue : 0);
              const heightPct = Math.max(12, Math.round((val / maxMonthVal) * 100));

              return (
                <div
                  key={m}
                  style={{
                    flex: 1,
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    gap: "8px",
                    height: "100%",
                    justifyContent: "flex-end",
                  }}
                >
                  <span
                    style={{
                      fontSize: "13px",
                      fontWeight: 700,
                      color: val > 0 ? "#1e293b" : "#94a3b8",
                    }}
                  >
                    {val > 0 ? `₹${(val / 1000).toFixed(0)}k` : "—"}
                  </span>
                  <div
                    style={{
                      width: "100%",
                      maxWidth: "52px",
                      height: `${heightPct}%`,
                      background:
                        val > 0
                          ? "linear-gradient(180deg, #3b82f6 0%, #1d4ed8 100%)"
                          : "#f1f5f9",
                      borderRadius: "6px 6px 2px 2px",
                      transition: "height 0.4s ease",
                      boxShadow: val > 0 ? "0 4px 10px rgba(37,99,235,0.2)" : "none",
                    }}
                    title={`${m}: ${formatCurrency(val)}`}
                  />
                  <span
                    style={{
                      fontSize: "13px",
                      fontWeight: 600,
                      color: val > 0 ? "#2563eb" : "#64748b",
                    }}
                  >
                    {m}
                  </span>
                </div>
              );
            })}
          </div>

          <div
            style={{
              display: "flex",
              flexWrap: "wrap",
              alignItems: "center",
              justifyContent: "space-between",
              gap: "12px",
              marginTop: "18px",
              paddingTop: "8px",
              fontSize: "13.5px",
              color: "#64748b",
            }}
          >
            <span>
              Cumulative Invoiced: <strong style={{ color: "#0f172a" }}>{formatCurrency(totalSalesRevenue)}</strong>
            </span>
            <span>
              Average Ticket Size: <strong style={{ color: "#0f172a" }}>{formatCurrency(avgOrderValue)}</strong>
            </span>
          </div>
        </div>

        {/* INVENTORY & OPERATIONAL SUMMARY */}
        <div className="dashboard-card">
          <div className="card-header">
            <div>
              <h3>Inventory Health &amp; Procurement</h3>
              <p>SKU status and active supplier purchase orders</p>
            </div>
            <Link to="/inventory">Inventory →</Link>
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: "16px", flex: 1 }}>
            {/* Stock Progress Indicators */}
            <div style={{ background: "#f8fafc", padding: "16px", borderRadius: "10px", border: "1px solid #e2e8f0" }}>
              <div style={{ display: "flex", justifyContent: "space-between", fontSize: "13.5px", fontWeight: 700, color: "#334155", marginBottom: "8px" }}>
                <span>Catalog Stock Health</span>
                <span style={{ color: "#2563eb" }}>{products.length} Active SKUs</span>
              </div>
              <div style={{ height: "10px", background: "#e2e8f0", borderRadius: "5px", overflow: "hidden", display: "flex" }}>
                <div
                  style={{
                    width: `${Math.max(10, ((products.length - lowStockItems.length) / Math.max(1, products.length)) * 100)}%`,
                    background: "#10b981",
                  }}
                  title="Adequate Stock"
                />
                <div
                  style={{
                    width: `${(lowStockItems.length / Math.max(1, products.length)) * 100}%`,
                    background: "#f59e0b",
                  }}
                  title="Low Stock"
                />
                <div
                  style={{
                    width: `${(outOfStockItems.length / Math.max(1, products.length)) * 100}%`,
                    background: "#ef4444",
                  }}
                  title="Out of Stock"
                />
              </div>
              <div style={{ display: "flex", gap: "14px", marginTop: "10px", fontSize: "12.5px", color: "#64748b" }}>
                <span style={{ display: "flex", alignItems: "center", gap: "5px" }}>
                  <span style={{ width: "9px", height: "9px", borderRadius: "50%", background: "#10b981" }} />
                  Healthy ({products.length - lowStockItems.length})
                </span>
                <span style={{ display: "flex", alignItems: "center", gap: "5px" }}>
                  <span style={{ width: "9px", height: "9px", borderRadius: "50%", background: "#f59e0b" }} />
                  Low ({lowStockItems.length})
                </span>
                <span style={{ display: "flex", alignItems: "center", gap: "5px" }}>
                  <span style={{ width: "9px", height: "9px", borderRadius: "50%", background: "#ef4444" }} />
                  Out ({outOfStockItems.length})
                </span>
              </div>
            </div>

            {/* Pending Purchases Quick Box */}
            <div style={{ background: "#f8fafc", padding: "16px", borderRadius: "10px", border: "1px solid #e2e8f0" }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "4px" }}>
                <span style={{ fontSize: "13.5px", fontWeight: 700, color: "#334155" }}>
                  Pending Procurement Orders
                </span>
                <Link to="/purchases" style={{ fontSize: "12.5px", color: "#2563eb", fontWeight: 600 }}>
                  Manage POs →
                </Link>
              </div>
              <div style={{ fontSize: "22px", fontWeight: 800, color: "#0f172a", marginTop: "4px" }}>
                {pendingPurchases.length} Purchase Order(s)
              </div>
              <div style={{ fontSize: "13px", color: "#64748b", marginTop: "2px" }}>
                Awaiting supplier fulfillment and warehouse intake
              </div>
            </div>

            {/* AI Demand Shortcut */}
            <div
              style={{
                background: "linear-gradient(135deg, #eff6ff 0%, #e0e7ff 100%)",
                padding: "16px",
                borderRadius: "10px",
                border: "1px solid #bfdbfe",
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                marginTop: "auto",
              }}
            >
              <div>
                <strong style={{ fontSize: "14px", color: "#1e3a8a", display: "block" }}>
                  AI Demand Forecasting
                </strong>
                <span style={{ fontSize: "12.5px", color: "#3b82f6" }}>
                  Predict replenishment for next 90 days
                </span>
              </div>
              <Link to="/forecast" className="primary-button" style={{ padding: "6px 14px", fontSize: "12px" }}>
                Forecast →
              </Link>
            </div>
          </div>
        </div>
      </div>

      {/* =====================================================
          ROW 3: OPERATIONAL TABLES & RECENT ACTIVITY
      ===================================================== */}
      <div className="dashboard-grid">
        {/* RECENT SALES ORDERS TABLE */}
        <div className="dashboard-card">
          <div className="card-header">
            <div>
              <h3>Recent Sales Orders</h3>
              <p>Latest customer invoices recorded in this company</p>
            </div>
            <Link to="/sales">View All Invoices →</Link>
          </div>

          {recentSales.length === 0 ? (
            <div className="empty-state">
              <div style={{ fontSize: "28px", marginBottom: "6px" }}>📑</div>
              No sales recorded yet. Click "New Sale" to create an invoice.
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Invoice #</th>
                    <th>Customer</th>
                    <th>Product</th>
                    <th>Units</th>
                    <th>Total</th>
                    <th>Status</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {recentSales.slice(0, 5).map((sale, idx) => {
                    const status = sale.payment_status || "UNPAID";
                    const isPaid = status === "PAID";
                    const isPartial = status === "PARTIALLY_PAID";

                    return (
                      <tr key={sale.id || sale.sale_id || idx}>
                        <td>
                          <span
                            style={{
                              fontFamily: "monospace",
                              fontWeight: 700,
                              color: "#2563eb",
                              fontSize: "13px",
                            }}
                          >
                            {sale.invoice_number || `INV-${sale.id}`}
                          </span>
                        </td>
                        <td>
                          <strong style={{ color: "#1e293b", fontSize: "14.5px" }}>
                            {sale.customer || "Walk-in Customer"}
                          </strong>
                        </td>
                        <td style={{ fontSize: "14px" }}>{sale.product || "Product Item"}</td>
                        <td style={{ fontSize: "14px", fontWeight: 600 }}>{sale.quantity || 1}x</td>
                        <td className="amount" style={{ fontSize: "15px" }}>{formatCurrency(sale.total_amount)}</td>
                        <td>
                          <span
                            className={`status-badge ${
                              isPaid ? "paid" : isPartial ? "partial" : "unpaid"
                            }`}
                            style={{ fontSize: "12px", padding: "4px 10px" }}
                          >
                            {isPaid ? "✓ Paid" : isPartial ? "Partial" : "Unpaid"}
                          </span>
                        </td>
                        <td style={{ color: "#64748b", fontSize: "13px" }}>
                          {formatDate(sale.created_at)}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* PRIORITY ALERTS & ACTION REQUIRED */}
        <div className="dashboard-card">
          <div className="card-header">
            <div>
              <h3>Priority Inventory Alerts</h3>
              <p>Stock requiring immediate replenishment</p>
            </div>
            <Link to="/inventory">Catalog →</Link>
          </div>

          {lowStockItems.length === 0 ? (
            <div className="success-state">
              ✓ All inventory items are currently above safety reorder thresholds.
            </div>
          ) : (
            <div className="stock-list">
              {lowStockItems.slice(0, 5).map((item) => (
                <div key={item.product_id || item.id} className="stock-item">
                  <div>
                    <strong style={{ fontSize: "14.5px" }}>{item.product || item.name}</strong>
                    <span style={{ fontSize: "13px", marginTop: "3px" }}>
                      Current: <strong>{item.quantity}</strong> &bull; Reorder Level:{" "}
                      {item.reorder_level || 5}
                    </span>
                  </div>
                  <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                    <span className="low-stock" style={{ fontSize: "12px", padding: "4px 10px" }}>
                      {item.quantity <= 0 ? "Out of Stock" : `${item.quantity} left`}
                    </span>
                    <Link
                      to="/purchases"
                      style={{
                        padding: "5px 10px",
                        fontSize: "12px",
                        fontWeight: 600,
                        borderRadius: "6px",
                        background: "#eff6ff",
                        color: "#2563eb",
                        textDecoration: "none",
                        border: "1px solid #bfdbfe",
                      }}
                    >
                      Reorder
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* =====================================================
          ROW 4: ENTERPRISE NAVIGATION SHORTCUTS
      ===================================================== */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
          gap: "18px",
        }}
      >
        <Link
          to="/finance"
          style={{
            background: "#ffffff",
            padding: "18px 20px",
            borderRadius: "12px",
            border: "1px solid #e2e8f0",
            textDecoration: "none",
            color: "inherit",
            display: "flex",
            alignItems: "center",
            gap: "14px",
            transition: "all 0.15s ease",
            boxShadow: "0 1px 3px rgba(0,0,0,0.03)",
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.borderColor = "#3b82f6";
            e.currentTarget.style.transform = "translateY(-1px)";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.borderColor = "#e2e8f0";
            e.currentTarget.style.transform = "none";
          }}
        >
          <div style={{ fontSize: "28px" }}>💹</div>
          <div>
            <strong style={{ fontSize: "14.5px", color: "#0f172a", display: "block" }}>
              Financial Overview
            </strong>
            <span style={{ fontSize: "12.5px", color: "#64748b", marginTop: "2px", display: "block" }}>
              P&amp;L margins, revenue vs expenses
            </span>
          </div>
        </Link>

        <Link
          to="/receivables"
          style={{
            background: "#ffffff",
            padding: "18px 20px",
            borderRadius: "12px",
            border: "1px solid #e2e8f0",
            textDecoration: "none",
            color: "inherit",
            display: "flex",
            alignItems: "center",
            gap: "14px",
            transition: "all 0.15s ease",
            boxShadow: "0 1px 3px rgba(0,0,0,0.03)",
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.borderColor = "#3b82f6";
            e.currentTarget.style.transform = "translateY(-1px)";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.borderColor = "#e2e8f0";
            e.currentTarget.style.transform = "none";
          }}
        >
          <div style={{ fontSize: "28px" }}>💳</div>
          <div>
            <strong style={{ fontSize: "14.5px", color: "#0f172a", display: "block" }}>
              Customer Receivables
            </strong>
            <span style={{ fontSize: "12.5px", color: "#64748b", marginTop: "2px", display: "block" }}>
              Track outstanding balances &amp; aging
            </span>
          </div>
        </Link>

        <Link
          to="/reports"
          style={{
            background: "#ffffff",
            padding: "18px 20px",
            borderRadius: "12px",
            border: "1px solid #e2e8f0",
            textDecoration: "none",
            color: "inherit",
            display: "flex",
            alignItems: "center",
            gap: "14px",
            transition: "all 0.15s ease",
            boxShadow: "0 1px 3px rgba(0,0,0,0.03)",
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.borderColor = "#3b82f6";
            e.currentTarget.style.transform = "translateY(-1px)";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.borderColor = "#e2e8f0";
            e.currentTarget.style.transform = "none";
          }}
        >
          <div style={{ fontSize: "28px" }}>📑</div>
          <div>
            <strong style={{ fontSize: "14.5px", color: "#0f172a", display: "block" }}>
              Reports &amp; Exports
            </strong>
            <span style={{ fontSize: "12.5px", color: "#64748b", marginTop: "2px", display: "block" }}>
              Download PDF &amp; Excel statements
            </span>
          </div>
        </Link>

        <Link
          to="/notifications"
          style={{
            background: "#ffffff",
            padding: "18px 20px",
            borderRadius: "12px",
            border: "1px solid #e2e8f0",
            textDecoration: "none",
            color: "inherit",
            display: "flex",
            alignItems: "center",
            gap: "14px",
            transition: "all 0.15s ease",
            boxShadow: "0 1px 3px rgba(0,0,0,0.03)",
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.borderColor = "#3b82f6";
            e.currentTarget.style.transform = "translateY(-1px)";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.borderColor = "#e2e8f0";
            e.currentTarget.style.transform = "none";
          }}
        >
          <div style={{ fontSize: "28px" }}>🔔</div>
          <div>
            <strong style={{ fontSize: "14.5px", color: "#0f172a", display: "block" }}>
              Alerts &amp; Reminders
            </strong>
            <span style={{ fontSize: "12.5px", color: "#64748b", marginTop: "2px", display: "block" }}>
              {unreadAlerts > 0 ? `${unreadAlerts} unread notifications` : "System alerts center"}
            </span>
          </div>
        </Link>
      </div>
    </div>
  );
}

export default Dashboard;
