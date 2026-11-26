import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { getAIForecast } from "../../services/api";
import "./Forecast.css";

function getCompanyId() {
  const stored = Number(localStorage.getItem("erp_company_id"));
  return Number.isFinite(stored) && stored > 0 ? stored : 1;
}

function toNumber(value, fallback = 0) {
  const number = Number(value);
  return Number.isFinite(number) ? number : fallback;
}

function getForecastValue(value) {
  if (value && typeof value === "object") {
    return toNumber(value.predicted_units ?? value.demand ?? value.units ?? 0);
  }
  return toNumber(value);
}

function Forecast() {
  const [forecast, setForecast] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedProductId, setSelectedProductId] = useState(null);

  // Simulation State: allow user to test "what-if" inventory changes
  const [simulatedStock, setSimulatedStock] = useState(null);

  const loadForecast = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const companyId = getCompanyId();
      const data = await getAIForecast(companyId);

      setForecast(data && typeof data === "object" ? data : {});
    } catch (err) {
      console.error("Forecast error:", err);
      setForecast(null);
      setError(err?.message || "Unable to load the forecast. Make sure the backend is running.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadForecast();

    const handleCompanyChanged = () => {
      loadForecast();
    };

    window.addEventListener("companyChanged", handleCompanyChanged);

    return () => {
      window.removeEventListener("companyChanged", handleCompanyChanged);
    };
  }, [loadForecast]);

  const products = useMemo(() => {
    if (!Array.isArray(forecast?.forecasts)) return [];
    return forecast.forecasts.filter((item) => item && typeof item === "object");
  }, [forecast]);

  // Set default selected product for chart
  useEffect(() => {
    if (products.length > 0 && selectedProductId === null) {
      setSelectedProductId(products[0].product_id);
    }
  }, [products, selectedProductId]);

  const activeProduct = useMemo(() => {
    if (!products.length) return null;
    return products.find((p) => p.product_id === selectedProductId) || products[0];
  }, [products, selectedProductId]);

  const historyMonths = useMemo(() => {
    return forecast?.history_months || [];
  }, [forecast]);

  const forecastMonths = useMemo(() => {
    return forecast?.forecast_months || [];
  }, [forecast]);

  const summary = forecast?.summary && typeof forecast.summary === "object" ? forecast.summary : {};

  const productCount = toNumber(summary.products_analyzed, products.length);
  const highRisk = toNumber(
    summary.high_risk_products,
    products.filter((item) => String(item.stockout_risk || "").toUpperCase() === "HIGH").length
  );
  const mediumRisk = toNumber(
    summary.medium_risk_products,
    products.filter((item) => String(item.stockout_risk || "").toUpperCase() === "MEDIUM").length
  );
  const threeMonthForecast = toNumber(
    summary.three_month_forecast_units,
    products.reduce((total, item) => total + toNumber(item.next_three_month_demand), 0)
  );

  // Maximum unit value across history and forecast for scaling the chart
  const maxTimelineUnits = useMemo(() => {
    if (!activeProduct) return 10;
    const past = activeProduct.historical_units || [];
    const future = (activeProduct.forecast || []).map((f) => getForecastValue(f));
    const maxVal = Math.max(...past, ...future, 1);
    return maxVal;
  }, [activeProduct]);

  // Simulation calculations for active product
  const effectiveStock = simulatedStock !== null ? simulatedStock : (activeProduct ? activeProduct.current_stock : 0);
  const simulatedRisk = useMemo(() => {
    if (!activeProduct) return "LOW";
    const nextDemand = activeProduct.next_month_demand || 1;
    const reorderLvl = activeProduct.reorder_level || 5;

    if (effectiveStock <= reorderLvl || effectiveStock <= nextDemand) return "HIGH";
    if (effectiveStock <= nextDemand * 2) return "MEDIUM";
    return "LOW";
  }, [activeProduct, effectiveStock]);

  const simulatedReorder = useMemo(() => {
    if (!activeProduct) return 0;
    const threeMo = activeProduct.next_three_month_demand || (activeProduct.next_month_demand * 3) || 6;
    const target = threeMo + (activeProduct.reorder_level || 5);
    return Math.max(0, target - effectiveStock);
  }, [activeProduct, effectiveStock]);

  if (loading) {
    return (
      <div className="forecast-page">
        <div className="dashboard-card" style={{ padding: "60px 20px", textAlign: "center" }}>
          <div className="spinner" style={{ margin: "0 auto 16px" }}></div>
          <p style={{ margin: 0, fontSize: "15px", color: "#64748b", fontWeight: 600 }}>
            Analyzing sales velocity &amp; generating predictive demand models...
          </p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="forecast-page">
        <div className="dashboard-card" style={{ padding: "40px 30px", borderLeft: "4px solid #ef4444" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "8px" }}>
            <span style={{ fontSize: "20px" }}>⚠️</span>
            <h3 style={{ margin: 0, color: "#991b1b" }}>Unable to load Demand Forecast</h3>
          </div>
          <p style={{ margin: "0 0 16px", color: "#64748b", fontSize: "14px" }}>{error}</p>
          <button className="primary-button" onClick={loadForecast}>
            ↻ Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="forecast-page">
      {/* =====================================================
          1. EXECUTIVE HERO HEADER
      ===================================================== */}
      <div className="forecast-hero-header">
        <div>
          <div className="forecast-header-badges">
            <h1>AI Demand Forecast &amp; Inventory Protection</h1>
            <span className="forecast-badge-live">
              <span style={{ width: "7px", height: "7px", borderRadius: "50%", background: "#10b981" }}></span>
              Live Predictive Engine
            </span>
            <span className="forecast-badge-model">
              ⚡ Linear Trend &amp; Moving Average
            </span>
          </div>
          <p>
            Machine-learning intelligence forecasting 90-day SKU demand and automated stockout prevention
          </p>
        </div>

        <div className="forecast-header-actions">
          <div className="forecast-company-pill">
            <span>🏢</span>
            <span>Organization:</span>
            <strong>{forecast?.company || "Primary Company"}</strong>
          </div>

          <button
            type="button"
            className="secondary-button"
            onClick={loadForecast}
            title="Recalculate AI forecast models"
            style={{ fontSize: "13.5px", padding: "9px 16px" }}
          >
            <span>↻</span>
            <span>Refresh Model</span>
          </button>
        </div>
      </div>

      {/* =====================================================
          2. ENTERPRISE KPI CARDS (4 PRIMARY METRICS)
      ===================================================== */}
      <div className="forecast-stats-grid">
        {/* KPI 1: PRODUCTS ANALYZED */}
        <div className="forecast-stat-card" style={{ borderLeft: "4px solid #2563eb" }}>
          <div className="stat-header">
            <span>Catalog Monitored</span>
            <div className="forecast-stat-icon blue">📦</div>
          </div>
          <div className="forecast-stat-value" style={{ color: "#1d4ed8" }}>
            {productCount} SKUs
          </div>
          <div className="forecast-stat-footer">
            <span className="forecast-trend-badge positive">✓ 100% Tracked</span>
            <span>Active SKU Portfolio</span>
          </div>
        </div>

        {/* KPI 2: HIGH STOCKOUT RISK */}
        <div className="forecast-stat-card" style={{ borderLeft: `4px solid ${highRisk > 0 ? "#dc2626" : "#059669"}` }}>
          <div className="stat-header">
            <span>Critical Stockout Risk</span>
            <div className={`forecast-stat-icon ${highRisk > 0 ? "red" : "blue"}`}>
              {highRisk > 0 ? "🚨" : "🛡️"}
            </div>
          </div>
          <div className="forecast-stat-value" style={{ color: highRisk > 0 ? "#dc2626" : "#059669" }}>
            {highRisk} SKUs
          </div>
          <div className="forecast-stat-footer">
            {highRisk > 0 ? (
              <span className="forecast-trend-badge danger">⚠️ Depletion &lt; 30d</span>
            ) : (
              <span className="forecast-trend-badge positive">✓ Zero Critical Risks</span>
            )}
            <span>Immediate Reorder Need</span>
          </div>
        </div>

        {/* KPI 3: MEDIUM RISK */}
        <div className="forecast-stat-card" style={{ borderLeft: `4px solid ${mediumRisk > 0 ? "#d97706" : "#64748b"}` }}>
          <div className="stat-header">
            <span>Medium Risk (30-60d)</span>
            <div className="forecast-stat-icon amber">⏳</div>
          </div>
          <div className="forecast-stat-value" style={{ color: mediumRisk > 0 ? "#b45309" : "#334155" }}>
            {mediumRisk} SKUs
          </div>
          <div className="forecast-stat-footer">
            {mediumRisk > 0 ? (
              <span className="forecast-trend-badge warning">Monitor Runway</span>
            ) : (
              <span className="forecast-trend-badge positive">✓ 60-Day Buffer Safe</span>
            )}
            <span>Pre-emptive Procurement</span>
          </div>
        </div>

        {/* KPI 4: 90-DAY FORECAST DEMAND */}
        <div className="forecast-stat-card" style={{ borderLeft: "4px solid #7c3aed" }}>
          <div className="stat-header">
            <span>90-Day Projected Demand</span>
            <div className="forecast-stat-icon purple">📈</div>
          </div>
          <div className="forecast-stat-value" style={{ color: "#6d28d9" }}>
            {threeMonthForecast.toLocaleString("en-IN")} Units
          </div>
          <div className="forecast-stat-footer">
            <span className="forecast-trend-badge purple">
              ~{(threeMonthForecast / 3).toFixed(0)} units / mo
            </span>
            <span>Next Quarter Dispatches</span>
          </div>
        </div>
      </div>

      {/* =====================================================
          3. PREDICTIVE DEMAND HORIZON (PAST VS FUTURE VISUALIZER)
      ===================================================== */}
      {activeProduct && (
        <section className="forecast-chart-card">
          <div className="forecast-card-header">
            <div>
              <h3>9-Month Demand Horizon: {activeProduct.product}</h3>
              <p>
                Seamless transition from the last 6 calendar months of actual invoiced sales to the next 3 months of AI forecasted demand
              </p>
            </div>

            <div style={{ display: "flex", alignItems: "center", gap: "16px", flexWrap: "wrap" }}>
              {products.length > 1 && (
                <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                  <label htmlFor="productSelect" style={{ fontSize: "12.5px", fontWeight: 700, color: "#475569" }}>
                    Select SKU:
                  </label>
                  <select
                    id="productSelect"
                    className="filter-select"
                    value={selectedProductId || ""}
                    onChange={(e) => {
                      setSelectedProductId(Number(e.target.value));
                      setSimulatedStock(null);
                    }}
                    style={{ padding: "6px 12px", fontSize: "13px" }}
                  >
                    {products.map((p) => (
                      <option key={p.product_id} value={p.product_id}>
                        {p.product}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              <div className="forecast-legend">
                <div className="forecast-legend-item">
                  <span className="legend-swatch actual"></span>
                  <span>Historical Invoiced Sales</span>
                </div>
                <div className="forecast-legend-item">
                  <span className="legend-swatch forecast"></span>
                  <span>AI Predicted Demand (⚡ Next Quarter)</span>
                </div>
              </div>
            </div>
          </div>

          {/* Interactive Histogram Stage */}
          <div className="forecast-timeline-stage">
            {/* 6 Historical Months */}
            {(activeProduct.historical_units || []).map((units, idx) => {
              const labelObj = historyMonths[idx] || { label: `Month -${6 - idx}` };
              const heightPct = units > 0 ? Math.max(16, Math.round((units / maxTimelineUnits) * 100)) : 6;
              const isActive = units > 0;

              return (
                <div className="forecast-column-group" key={`hist-${idx}`}>
                  <span className={`forecast-col-val ${isActive ? "" : "zero"}`}>
                    {isActive ? `${units} units` : "—"}
                  </span>

                  <div className="forecast-bar-track">
                    <div
                      className={`forecast-pillar ${isActive ? "actual" : "zero"}`}
                      style={{ height: `${heightPct}%` }}
                      title={`${labelObj.label}: ${units} actual units sold`}
                    />
                  </div>

                  <span className="forecast-col-label">{labelObj.label}</span>
                  <span className="forecast-col-sub">Actual Sales</span>
                </div>
              );
            })}

            {/* Visual AI Prediction Horizon Divider */}
            <div className="forecast-divider">
              <span className="forecast-divider-badge">⚡ AI Horizon</span>
            </div>

            {/* 3 Future Forecast Months */}
            {(activeProduct.forecast || []).map((fItem, idx) => {
              const units = getForecastValue(fItem);
              const labelObj = forecastMonths[idx] || { label: fItem.label || `Month +${idx + 1}` };
              const heightPct = units > 0 ? Math.max(16, Math.round((units / maxTimelineUnits) * 100)) : 6;

              return (
                <div className="forecast-column-group" key={`fore-${idx}`}>
                  <span className="forecast-col-val forecast-val">
                    ~{units} units
                  </span>

                  <div className="forecast-bar-track">
                    <div
                      className="forecast-pillar predicted"
                      style={{ height: `${heightPct}%` }}
                      title={`${labelObj.label}: Predicted ~${units} units (${activeProduct.forecast_method || "AI Trend"})`}
                    />
                  </div>

                  <span className="forecast-col-label" style={{ color: "#6d28d9" }}>
                    {labelObj.label}
                  </span>
                  <span className="forecast-col-sub" style={{ color: "#7c3aed", fontWeight: 600 }}>
                    Projected
                  </span>
                </div>
              );
            })}
          </div>

          {/* Horizon Insights Strip */}
          <div className="forecast-insights-strip">
            <div className="forecast-insight-card">
              <div className="forecast-insight-icon">🧠</div>
              <div className="forecast-insight-content">
                <span>Model Applied</span>
                <strong>{activeProduct.forecast_method || "Recent Moving Average"}</strong>
                <small>Calculated from 6-month historical trajectory</small>
              </div>
            </div>

            <div className="forecast-insight-card">
              <div className="forecast-insight-icon">⚡</div>
              <div className="forecast-insight-content">
                <span>Projected Velocity</span>
                <strong>~{activeProduct.next_month_demand || 2} units / month</strong>
                <small>Anticipated average monthly consumption</small>
              </div>
            </div>

            <div className="forecast-insight-card">
              <div className="forecast-insight-icon">🛡️</div>
              <div className="forecast-insight-content">
                <span>Inventory Longevity</span>
                <strong>
                  {activeProduct.next_month_demand > 0
                    ? `${(activeProduct.current_stock / activeProduct.next_month_demand).toFixed(1)} Months Runway`
                    : "Ample Stock Coverage"}
                </strong>
                <small>
                  Stock of {activeProduct.current_stock} covers demand through {forecastMonths[2]?.label || "Next Quarter"}
                </small>
              </div>
            </div>
          </div>
        </section>
      )}

      {/* =====================================================
          4. INTERACTIVE STRESS-TEST & REORDER SIMULATOR
      ===================================================== */}
      {activeProduct && (
        <section className="forecast-simulation-card">
          <div className="simulation-header">
            <h4>
              <span>🧪</span>
              <span>Interactive Stockout Simulator: {activeProduct.product}</span>
            </h4>
            {simulatedStock !== null && (
              <button
                type="button"
                className="secondary-button"
                onClick={() => setSimulatedStock(null)}
                style={{ fontSize: "12px", padding: "4px 10px" }}
              >
                Reset to Live Stock ({activeProduct.current_stock})
              </button>
            )}
          </div>

          <div className="simulation-controls">
            <div className="simulation-control-group" style={{ flex: 1 }}>
              <label>
                Simulate Current Stock Level: <strong>{effectiveStock} units</strong>{" "}
                <span style={{ color: "#64748b", fontWeight: 400 }}>
                  (Live inventory is {activeProduct.current_stock})
                </span>
              </label>
              <div className="simulation-slider-wrap">
                <input
                  type="range"
                  min="0"
                  max="40"
                  value={effectiveStock}
                  onChange={(e) => setSimulatedStock(Number(e.target.value))}
                  style={{ width: "100%", maxWidth: "320px", accentColor: "#2563eb" }}
                />
                <span style={{ fontSize: "13px", fontWeight: 700, color: "#1e293b", minWidth: "50px" }}>
                  {effectiveStock} pcs
                </span>
              </div>
            </div>

            <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
              <div
                className={`simulation-result-badge ${
                  simulatedRisk === "HIGH" ? "risk-pill high" : simulatedRisk === "MEDIUM" ? "risk-pill medium" : "risk-pill low"
                }`}
              >
                <span>Stockout Risk:</span>
                <strong>{simulatedRisk}</strong>
              </div>

              <div
                style={{
                  padding: "8px 14px",
                  borderRadius: "8px",
                  background: simulatedReorder > 0 ? "#fef2f2" : "#ecfdf5",
                  border: `1px solid ${simulatedReorder > 0 ? "#fecaca" : "#a7f3d0"}`,
                  fontSize: "13px",
                  color: simulatedReorder > 0 ? "#991b1b" : "#065f46",
                  fontWeight: 700,
                }}
              >
                {simulatedReorder > 0 ? (
                  <span>⚠️ AI Recommendation: Reorder {simulatedReorder} units</span>
                ) : (
                  <span>✓ Stock is Sufficient (0 units needed)</span>
                )}
              </div>
            </div>
          </div>
        </section>
      )}

      {/* =====================================================
          5. DEMAND FORECAST & REPLENISHMENT MATRIX (TABLE)
      ===================================================== */}
      <section className="forecast-chart-card">
        <div className="forecast-card-header">
          <div>
            <h3>SKU Demand Forecast &amp; Automated Replenishment Matrix</h3>
            <p>Calculates exact supplier reorder quantities based on target safety stock and projected 90-day depletion</p>
          </div>
        </div>

        {products.length === 0 ? (
          <div className="empty-state" style={{ padding: "40px 20px", textAlign: "center" }}>
            No products available for forecasting. Create products and record sales to enable automated prediction.
          </div>
        ) : (
          <div className="table-wrapper">
            <table className="forecast-table" style={{ width: "100%", borderCollapse: "collapse" }}>
              <thead>
                <tr>
                  <th style={{ textAlign: "left" }}>Product SKU / Name</th>
                  <th style={{ textAlign: "center" }}>Current Stock</th>
                  <th style={{ textAlign: "center" }}>Safety Buffer</th>
                  <th style={{ textAlign: "center" }}>{forecastMonths[0]?.label || "Month 1"}</th>
                  <th style={{ textAlign: "center" }}>{forecastMonths[1]?.label || "Month 2"}</th>
                  <th style={{ textAlign: "center" }}>{forecastMonths[2]?.label || "Month 3"}</th>
                  <th style={{ textAlign: "center" }}>3-Mo Total</th>
                  <th style={{ textAlign: "center" }}>Stockout Risk</th>
                  <th style={{ textAlign: "left" }}>Stock Runway</th>
                  <th style={{ textAlign: "center" }}>Reorder Quantity</th>
                  <th style={{ textAlign: "center" }}>Action</th>
                </tr>
              </thead>

              <tbody>
                {products.map((item, index) => {
                  const values = Array.isArray(item.forecast) ? item.forecast : [];
                  const m1 = getForecastValue(values[0]);
                  const m2 = getForecastValue(values[1]);
                  const m3 = getForecastValue(values[2]);
                  const threeMoDemand = toNumber(item.next_three_month_demand, m1 + m2 + m3);

                  const stock = toNumber(item.current_stock);
                  const reorderLevel = toNumber(item.reorder_level, 5);
                  const reorderQty = toNumber(item.recommended_reorder);
                  const risk = String(item.stockout_risk || "LOW").toUpperCase();

                  // Runway calculation
                  const runwayMonths = m1 > 0 ? (stock / m1).toFixed(1) : "12+";
                  const runwayPct = Math.min(100, Math.round((stock / Math.max(threeMoDemand, 1)) * 100));

                  const riskClass = risk === "HIGH" ? "high" : risk === "MEDIUM" ? "medium" : "low";

                  return (
                    <tr key={item.product_id || index}>
                      <td>
                        <div style={{ display: "flex", flexDirection: "column" }}>
                          <strong style={{ fontSize: "14px", color: "#0f172a" }}>
                            {item.product}
                          </strong>
                          <span className="forecast-method-tag">
                            {item.forecast_method || "Moving Average"}
                          </span>
                        </div>
                      </td>

                      <td style={{ textAlign: "center" }}>
                        <span style={{ padding: "4px 10px", background: "#f1f5f9", borderRadius: "10px", fontWeight: 700, color: "#1e293b" }}>
                          {stock} units
                        </span>
                      </td>

                      <td style={{ textAlign: "center", color: "#64748b", fontSize: "13px" }}>
                        {reorderLevel} units
                      </td>

                      <td style={{ textAlign: "center", fontWeight: 600, color: "#6d28d9" }}>
                        ~{m1}
                      </td>

                      <td style={{ textAlign: "center", fontWeight: 600, color: "#6d28d9" }}>
                        ~{m2}
                      </td>

                      <td style={{ textAlign: "center", fontWeight: 600, color: "#6d28d9" }}>
                        ~{m3}
                      </td>

                      <td style={{ textAlign: "center", fontWeight: 800, color: "#0f172a" }}>
                        ~{threeMoDemand} units
                      </td>

                      <td style={{ textAlign: "center" }}>
                        <span className={`risk-pill ${riskClass}`}>
                          {risk === "HIGH" ? "🚨 HIGH" : risk === "MEDIUM" ? "⚠️ MEDIUM" : "✓ LOW"}
                        </span>
                      </td>

                      <td>
                        <div className="runway-wrapper">
                          <div className="runway-track">
                            <div
                              className={`runway-fill ${risk === "HIGH" ? "critical" : risk === "MEDIUM" ? "caution" : "safe"}`}
                              style={{ width: `${Math.max(runwayPct, 8)}%` }}
                            />
                          </div>
                          <span className="runway-text">
                            {runwayMonths} mo supply remaining
                          </span>
                        </div>
                      </td>

                      <td style={{ textAlign: "center" }}>
                        {reorderQty > 0 ? (
                          <strong style={{ color: "#dc2626", fontSize: "15px" }}>
                            +{reorderQty} units
                          </strong>
                        ) : (
                          <span style={{ color: "#059669", fontWeight: 600, fontSize: "13px" }}>
                            0 (Optimal)
                          </span>
                        )}
                      </td>

                      <td style={{ textAlign: "center" }}>
                        {reorderQty > 0 ? (
                          <Link
                            to="/purchases"
                            className="primary-button"
                            style={{ padding: "6px 12px", fontSize: "12px", textDecoration: "none", whiteSpace: "nowrap" }}
                          >
                            + Order {reorderQty}
                          </Link>
                        ) : (
                          <span
                            style={{
                              display: "inline-block",
                              padding: "4px 8px",
                              borderRadius: "6px",
                              background: "#ecfdf5",
                              color: "#047857",
                              fontSize: "12px",
                              fontWeight: 700,
                              border: "1px solid #a7f3d0",
                            }}
                          >
                            ✓ Covered
                          </span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* =====================================================
          6. ALGORITHMIC TRANSPARENCY & METHODOLOGY CARD
      ===================================================== */}
      <div className="forecast-explainer-card">
        <div className="explainer-title">
          <span style={{ fontSize: "20px" }}>🤖</span>
          <h4>How the AI Demand Forecasting Algorithm Works</h4>
        </div>

        <div className="explainer-steps">
          <div className="explainer-step">
            <span className="step-num">Step 1</span>
            <span className="step-heading">Historical Ingestion</span>
            <span className="step-desc">
              Gathers the past 6 rolling months of sales order invoices, aggregating customer purchase patterns per product SKU.
            </span>
          </div>

          <div className="explainer-step">
            <span className="step-num">Step 2</span>
            <span className="step-heading">Trend Regression</span>
            <span className="step-desc">
              Applies Ordinary Least Squares (OLS) Linear Regression to project rising or falling trajectory; uses moving averages for sparse data.
            </span>
          </div>

          <div className="explainer-step">
            <span className="step-num">Step 3</span>
            <span className="step-heading">Target Buffer Target</span>
            <span className="step-desc">
              Calculates safety stock buffer: Target = 90-Day Projected Demand + Safety Reorder Threshold to withstand supply shocks.
            </span>
          </div>

          <div className="explainer-step">
            <span className="step-num">Step 4</span>
            <span className="step-heading">Reorder Automation</span>
            <span className="step-desc">
              Flags stockout danger (High, Medium, Low) and suggests exact replenishment quantities to execute purchase orders instantly.
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Forecast;
