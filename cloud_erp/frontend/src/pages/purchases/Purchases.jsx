import { useEffect, useState, useMemo } from "react";
import {
  getPurchases,
  getPurchase,
  getPurchaseSummary,
  createPurchase,
  updatePurchaseStatus,
  getSuppliers,
  getProducts,
} from "../../services/api";

function Purchases() {
  const [purchases, setPurchases] = useState([]);
  const [summary, setSummary] = useState(null);
  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Filters
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [supplierFilter, setSupplierFilter] = useState("");

  // Modals
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [viewingPurchase, setViewingPurchase] = useState(null);
  const [saving, setSaving] = useState(false);
  const [actionLoadingId, setActionLoadingId] = useState(null);

  // Current logged in user
  const currentUser = useMemo(() => {
    try {
      return JSON.parse(localStorage.getItem("erp_user")) || null;
    } catch {
      return null;
    }
  }, []);

  const canManage = currentUser?.role === "COMPANY_ADMIN" || currentUser?.role === "MANAGER";

  // Create Form State
  const [createForm, setCreateForm] = useState({
    supplier_id: "",
    status: "PENDING",
    notes: "",
    items: [
      {
        product_id: "",
        quantity: 1,
        unit_price: 0,
      },
    ],
  });

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    try {
      setLoading(true);
      setError("");
      await Promise.all([
        loadPurchases(),
        loadSummary(),
        loadDependencies(),
      ]);
    } catch (err) {
      setError(err.message || "Failed to load purchases.");
    } finally {
      setLoading(false);
    }
  }

  async function loadPurchases() {
    const params = {};
    if (statusFilter && statusFilter !== "ALL") params.status = statusFilter;
    if (supplierFilter) params.supplier_id = supplierFilter;
    if (search) params.search = search;

    const data = await getPurchases(params);
    setPurchases(Array.isArray(data) ? data : []);
  }

  async function loadSummary() {
    try {
      const sumData = await getPurchaseSummary();
      setSummary(sumData);
    } catch {
      // Summary error shouldn't crash table
    }
  }

  async function loadDependencies() {
    try {
      const [supData, prodData] = await Promise.all([
        getSuppliers(),
        getProducts(),
      ]);
      setSuppliers(Array.isArray(supData) ? supData : []);
      setProducts(Array.isArray(prodData) ? prodData : []);
    } catch {
      // Non-critical
    }
  }

  // Handle Search & Filter trigger
  function handleFilterSubmit(e) {
    if (e) e.preventDefault();
    loadPurchases();
  }

  function handleResetFilters() {
    setSearch("");
    setStatusFilter("ALL");
    setSupplierFilter("");
    setTimeout(() => {
      loadPurchases();
    }, 0);
  }

  // Create Form Line Item Helpers
  function handleAddItem() {
    setCreateForm((prev) => ({
      ...prev,
      items: [
        ...prev.items,
        {
          product_id: "",
          quantity: 1,
          unit_price: 0,
        },
      ],
    }));
  }

  function handleRemoveItem(index) {
    if (createForm.items.length <= 1) return;
    setCreateForm((prev) => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== index),
    }));
  }

  function handleItemChange(index, field, value) {
    setCreateForm((prev) => {
      const updatedItems = [...prev.items];
      const item = { ...updatedItems[index], [field]: value };

      // Auto-fill unit price with product cost price if product selected
      if (field === "product_id" && value) {
        const prod = products.find((p) => String(p.id) === String(value));
        if (prod && (!item.unit_price || item.unit_price === 0)) {
          item.unit_price = prod.cost_price || prod.price || 0;
        }
      }

      updatedItems[index] = item;
      return { ...prev, items: updatedItems };
    });
  }

  // Calculated Grand Total for Create Form
  const calculatedGrandTotal = useMemo(() => {
    return createForm.items.reduce((sum, it) => {
      const qty = Number(it.quantity) || 0;
      const price = Number(it.unit_price) || 0;
      return sum + qty * price;
    }, 0);
  }, [createForm.items]);

  // Handle Create Submit
  async function handleCreateSubmit(e) {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!createForm.supplier_id) {
      setError("Please select a supplier.");
      return;
    }

    if (!createForm.items || createForm.items.length === 0) {
      setError("Please add at least one product item.");
      return;
    }

    for (let i = 0; i < createForm.items.length; i++) {
      const it = createForm.items[i];
      if (!it.product_id) {
        setError(`Please select a product for line ${i + 1}.`);
        return;
      }
      if (Number(it.quantity) <= 0) {
        setError(`Please enter a valid quantity for line ${i + 1}.`);
        return;
      }
      if (Number(it.unit_price) < 0) {
        setError(`Please enter a valid unit price for line ${i + 1}.`);
        return;
      }
    }

    try {
      setSaving(true);
      const payload = {
        supplier_id: Number(createForm.supplier_id),
        status: createForm.status,
        notes: createForm.notes.trim(),
        items: createForm.items.map((it) => ({
          product_id: Number(it.product_id),
          quantity: Number(it.quantity),
          unit_price: Number(it.unit_price),
        })),
      };

      const res = await createPurchase(payload);
      setSuccess(`Purchase Order "${res.purchase_number}" created successfully!`);
      setShowCreateModal(false);
      setCreateForm({
        supplier_id: "",
        status: "PENDING",
        notes: "",
        items: [{ product_id: "", quantity: 1, unit_price: 0 }],
      });
      await Promise.all([loadPurchases(), loadSummary()]);
    } catch (err) {
      setError(err.message || "Failed to create purchase order.");
    } finally {
      setSaving(false);
    }
  }

  // Handle Update Status (Mark Received or Cancel)
  async function handleStatusChange(poId, newStatus) {
    const actionLabel = newStatus === "RECEIVED" ? "receive and increase stock for" : "cancel";
    if (!window.confirm(`Are you sure you want to ${actionLabel} order #${poId}?`)) {
      return;
    }

    try {
      setActionLoadingId(poId);
      setError("");
      setSuccess("");
      const updated = await updatePurchaseStatus(poId, newStatus);
      setSuccess(`Order ${updated.purchase_number} is now marked ${updated.status}.`);
      if (viewingPurchase && viewingPurchase.id === poId) {
        setViewingPurchase(updated);
      }
      await Promise.all([loadPurchases(), loadSummary()]);
    } catch (err) {
      setError(err.message || `Failed to update status to ${newStatus}.`);
    } finally {
      setActionLoadingId(null);
    }
  }

  // Open Details Modal
  async function handleViewDetails(po) {
    try {
      const fullPo = await getPurchase(po.id);
      setViewingPurchase(fullPo);
    } catch {
      setViewingPurchase(po);
    }
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
      {/* =====================================================
          HEADER
      ===================================================== */}
      <div className="page-heading">
        <div>
          <h1>Purchase Management</h1>
          <p>Create purchase orders, track vendor deliveries, and automatically manage inventory replenishment.</p>
        </div>

        {canManage && (
          <button
            type="button"
            className="primary-button"
            onClick={() => {
              setError("");
              setSuccess("");
              setShowCreateModal(true);
            }}
          >
            + New Purchase Order
          </button>
        )}
      </div>

      {/* =====================================================
          METRICS OVERVIEW
      ===================================================== */}
      <div className="stats-grid">
        <div className="stat-card" style={{ borderLeft: "4px solid #8b5cf6" }}>
          <div className="stat-header">
            <span>Total Orders</span>
            <div className="stat-icon units" style={{ background: "#ede9fe", color: "#7c3aed" }}>
              📑
            </div>
          </div>
          <div className="stat-value" style={{ color: "#6d28d9" }}>
            {summary?.total_orders ?? purchases.length}
          </div>
          <div className="stat-change">
            <span className="stat-badge neutral">All Records</span>
            <span>Across all suppliers</span>
          </div>
        </div>

        <div className="stat-card" style={{ borderLeft: "4px solid #f59e0b" }}>
          <div className="stat-header">
            <span>Pending Orders</span>
            <div className="stat-icon transactions" style={{ background: "#fef3c7", color: "#d97706" }}>
              ⏳
            </div>
          </div>
          <div className="stat-value" style={{ color: "#b45309" }}>
            {summary?.pending_orders ?? purchases.filter((p) => p.status === "PENDING").length}
          </div>
          <div className="stat-change">
            <span className="stat-badge warning">Awaiting Delivery</span>
            <span>Stock holds pending intake</span>
          </div>
        </div>

        <div className="stat-card" style={{ borderLeft: "4px solid #16a34a" }}>
          <div className="stat-header">
            <span>Received Orders</span>
            <div className="stat-icon revenue" style={{ background: "#dcfce7", color: "#16a34a" }}>
              ✓
            </div>
          </div>
          <div className="stat-value" style={{ color: "#15803d" }}>
            {summary?.received_orders ?? purchases.filter((p) => p.status === "RECEIVED").length}
          </div>
          <div className="stat-change">
            <span className="stat-badge positive">Fulfilled</span>
            <span>Stock added to inventory</span>
          </div>
        </div>

        <div className="stat-card" style={{ borderLeft: "4px solid #0284c7" }}>
          <div className="stat-header">
            <span>Procurement Value</span>
            <div className="stat-icon revenue" style={{ background: "#e0f2fe", color: "#0284c7" }}>
              ₹
            </div>
          </div>
          <div className="stat-value" style={{ color: "#0369a1" }}>
            ₹
            {Number(summary?.total_spend ?? 0).toLocaleString("en-IN", {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2,
            })}
          </div>
          <div className="stat-change">
            <span className="stat-badge neutral">Total Spend</span>
            <span>Received procurement</span>
          </div>
        </div>
      </div>

      {/* =====================================================
          NOTIFICATIONS
      ===================================================== */}
      {success && <div className="success-message">✓ {success}</div>}
      {error && <div className="error-message">⚠ {error}</div>}

      {/* =====================================================
          FILTER & SEARCH TOOLBAR (CLEAN HORIZONTAL ROW)
      ===================================================== */}
      <form onSubmit={handleFilterSubmit} className="filter-toolbar">
        {/* Search Input with Icon */}
        <div className="search-input-wrapper">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            placeholder="Search by PO number, supplier name, notes..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        {/* Status Dropdown */}
        <div className="filter-select-wrapper" style={{ minWidth: "170px" }}>
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
            }}
          >
            <option value="ALL">All Statuses</option>
            <option value="PENDING">Pending (Ordered)</option>
            <option value="RECEIVED">Received (Delivered)</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        {/* Supplier Dropdown */}
        <div className="filter-select-wrapper" style={{ minWidth: "200px" }}>
          <select
            value={supplierFilter}
            onChange={(e) => {
              setSupplierFilter(e.target.value);
            }}
          >
            <option value="">All Suppliers</option>
            {suppliers.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </div>

        {/* Actions */}
        <div className="filter-actions">
          <button
            type="submit"
            className="primary-button"
            style={{ padding: "11px 18px", fontSize: "14px" }}
          >
            Filter
          </button>

          {(search || statusFilter !== "ALL" || supplierFilter) && (
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
          CREATE PURCHASE ORDER MODAL
      ===================================================== */}
      {showCreateModal && (
        <section className="form-card" style={{ marginBottom: "25px", border: "2px solid #2563eb" }}>
          <div className="card-header">
            <div>
              <h3>New Purchase Order</h3>
              <p>Record a procurement order with vendor details and line item quantities.</p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setShowCreateModal(false)}
            >
              ✕ Cancel
            </button>
          </div>

          <form onSubmit={handleCreateSubmit}>
            <div className="form-grid" style={{ marginBottom: "20px" }}>
              <div className="form-group">
                <label>Select Supplier *</label>
                <select
                  required
                  value={createForm.supplier_id}
                  onChange={(e) => setCreateForm({ ...createForm, supplier_id: e.target.value })}
                >
                  <option value="">-- Choose Supplier --</option>
                  {suppliers.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name} {s.contact_person ? `(${s.contact_person})` : ""}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Initial Status *</label>
                <select
                  value={createForm.status}
                  onChange={(e) => setCreateForm({ ...createForm, status: e.target.value })}
                >
                  <option value="PENDING">PENDING - Ordered (Stock will not increase yet)</option>
                  <option value="RECEIVED">RECEIVED - Delivered (Stock will increase immediately)</option>
                </select>
              </div>

              <div className="form-group" style={{ gridColumn: "1 / -1" }}>
                <label>Order Notes / Reference</label>
                <input
                  type="text"
                  placeholder="e.g. Bulk order for quarterly production batch #4"
                  value={createForm.notes}
                  onChange={(e) => setCreateForm({ ...createForm, notes: e.target.value })}
                />
              </div>
            </div>

            {/* Dynamic Items Table */}
            <div style={{ marginBottom: "20px" }}>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  marginBottom: "10px",
                }}
              >
                <h4 style={{ margin: 0, fontSize: "14px", color: "#374151" }}>
                  Product Line Items ({createForm.items.length})
                </h4>
                <button
                  type="button"
                  className="secondary-button"
                  style={{ padding: "5px 12px", fontSize: "12px" }}
                  onClick={handleAddItem}
                >
                  + Add Product Line
                </button>
              </div>

              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th style={{ width: "40%" }}>Product *</th>
                      <th style={{ width: "20%" }}>Quantity *</th>
                      <th style={{ width: "20%" }}>Purchase Price (₹) *</th>
                      <th style={{ width: "15%" }}>Subtotal (₹)</th>
                      <th style={{ width: "5%" }}></th>
                    </tr>
                  </thead>
                  <tbody>
                    {createForm.items.map((it, idx) => {
                      const lineSubtotal = (Number(it.quantity) || 0) * (Number(it.unit_price) || 0);

                      return (
                        <tr key={idx}>
                          <td>
                            <select
                              required
                              value={it.product_id}
                              onChange={(e) => handleItemChange(idx, "product_id", e.target.value)}
                              style={{ width: "100%", padding: "7px 10px", fontSize: "13px" }}
                            >
                              <option value="">-- Select Product --</option>
                              {products.map((p) => (
                                <option key={p.id} value={p.id}>
                                  {p.name} (Current Stock: {p.quantity})
                                </option>
                              ))}
                            </select>
                          </td>

                          <td>
                            <input
                              type="number"
                              required
                              min="1"
                              value={it.quantity}
                              onChange={(e) => handleItemChange(idx, "quantity", e.target.value)}
                              style={{ width: "100%", padding: "7px 10px", fontSize: "13px" }}
                            />
                          </td>

                          <td>
                            <input
                              type="number"
                              required
                              min="0"
                              step="0.01"
                              value={it.unit_price}
                              onChange={(e) => handleItemChange(idx, "unit_price", e.target.value)}
                              style={{ width: "100%", padding: "7px 10px", fontSize: "13px" }}
                            />
                          </td>

                          <td style={{ fontWeight: 600, color: "#111827" }}>
                            ₹{lineSubtotal.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                          </td>

                          <td>
                            {createForm.items.length > 1 && (
                              <button
                                type="button"
                                style={{
                                  background: "transparent",
                                  border: "none",
                                  color: "#dc2626",
                                  cursor: "pointer",
                                  fontSize: "16px",
                                }}
                                title="Remove line"
                                onClick={() => handleRemoveItem(idx)}
                              >
                                ✕
                              </button>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Total Summary */}
            <div className="sale-total" style={{ marginBottom: "20px" }}>
              <span>Total Purchase Amount:</span>
              <strong>
                ₹{calculatedGrandTotal.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
              </strong>
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() => setShowCreateModal(false)}
              >
                Cancel
              </button>
              <button type="submit" className="primary-button" disabled={saving}>
                {saving ? "Creating Order..." : "Confirm & Save Order"}
              </button>
            </div>
          </form>
        </section>
      )}

      {/* =====================================================
          VIEW PURCHASE DETAILS MODAL
      ===================================================== */}
      {viewingPurchase && (
        <section className="dashboard-card" style={{ marginBottom: "25px", border: "2px solid #2563eb" }}>
          <div className="card-header">
            <div>
              <h3>Purchase Order: {viewingPurchase.purchase_number}</h3>
              <p>
                Supplier: <strong>{viewingPurchase.supplier_name}</strong> | Date:{" "}
                {viewingPurchase.created_at ? new Date(viewingPurchase.created_at).toLocaleString() : "N/A"}
              </p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setViewingPurchase(null)}
            >
              ✕ Close
            </button>
          </div>

          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              padding: "12px 18px",
              background: "#f8fafc",
              borderRadius: "8px",
              marginBottom: "18px",
            }}
          >
            <div>
              <span style={{ fontSize: "12px", color: "#6b7280" }}>Current Status: </span>
              <span
                className={`status-badge ${
                  viewingPurchase.status === "RECEIVED"
                    ? "success"
                    : viewingPurchase.status === "PENDING"
                    ? "warning"
                    : ""
                }`}
                style={
                  viewingPurchase.status === "CANCELLED"
                    ? { background: "#fee2e2", color: "#b91c1c" }
                    : {}
                }
              >
                {viewingPurchase.status}
              </span>
              {viewingPurchase.received_at && (
                <small style={{ marginLeft: "10px", color: "#15803d" }}>
                  (Received on {new Date(viewingPurchase.received_at).toLocaleDateString()})
                </small>
              )}
            </div>

            {canManage && viewingPurchase.status === "PENDING" && (
              <div style={{ display: "flex", gap: "8px" }}>
                <button
                  type="button"
                  className="primary-button"
                  style={{ padding: "6px 14px", fontSize: "12px" }}
                  disabled={actionLoadingId === viewingPurchase.id}
                  onClick={() => handleStatusChange(viewingPurchase.id, "RECEIVED")}
                >
                  ✓ Mark as Received (Add Stock)
                </button>
                <button
                  type="button"
                  className="danger-button"
                  style={{ padding: "6px 14px", fontSize: "12px" }}
                  disabled={actionLoadingId === viewingPurchase.id}
                  onClick={() => handleStatusChange(viewingPurchase.id, "CANCELLED")}
                >
                  Cancel Order
                </button>
              </div>
            )}
          </div>

          {viewingPurchase.notes && (
            <div style={{ marginBottom: "16px", fontSize: "13px", color: "#4b5563" }}>
              <strong>Notes:</strong> {viewingPurchase.notes}
            </div>
          )}

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Quantity</th>
                  <th>Unit Price</th>
                  <th>Total Price</th>
                </tr>
              </thead>
              <tbody>
                {viewingPurchase.items?.map((it) => (
                  <tr key={it.id}>
                    <td>
                      <strong>{it.product_name || `Product #${it.product_id}`}</strong>
                    </td>
                    <td>{it.quantity} units</td>
                    <td>₹{Number(it.unit_price).toLocaleString("en-IN", { minimumFractionDigits: 2 })}</td>
                    <td className="amount">
                      ₹{Number(it.total_price).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="sale-total" style={{ marginTop: "15px" }}>
            <span>Order Total:</span>
            <strong>
              ₹{Number(viewingPurchase.total_amount).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
            </strong>
          </div>
        </section>
      )}

      {/* =====================================================
          PURCHASES DIRECTORY TABLE
      ===================================================== */}
      <section className="dashboard-card">
        <div className="card-header">
          <div>
            <h3>Purchase Orders</h3>
            <p>Showing {purchases.length} order{purchases.length === 1 ? "" : "s"}</p>
          </div>
        </div>

        {loading ? (
          <div className="loading-page">
            <div className="spinner"></div>
            Loading purchase orders...
          </div>
        ) : purchases.length === 0 ? (
          <div className="empty-state">
            No purchase records found. {canManage ? "Click \"+ New Purchase Order\" above to initiate procurement." : ""}
          </div>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>PO Number</th>
                  <th>Supplier</th>
                  <th>Date</th>
                  <th>Items Ordered</th>
                  <th>Status</th>
                  <th>Total Amount</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {purchases.map((po) => {
                  const totalItems = po.items?.length || 0;
                  const totalUnits = po.items?.reduce((s, it) => s + (it.quantity || 0), 0) || 0;

                  return (
                    <tr key={po.id}>
                      <td>
                        <strong>{po.purchase_number}</strong>
                      </td>

                      <td>
                        <strong>{po.supplier_name || "—"}</strong>
                      </td>

                      <td>
                        {po.created_at ? new Date(po.created_at).toLocaleDateString() : "N/A"}
                      </td>

                      <td>
                        {totalItems} item{totalItems === 1 ? "" : "s"} ({totalUnits} unit{totalUnits === 1 ? "" : "s"})
                      </td>

                      <td>
                        <span
                          className={`status-badge ${
                            po.status === "RECEIVED"
                              ? "success"
                              : po.status === "PENDING"
                              ? "warning"
                              : ""
                          }`}
                          style={
                            po.status === "CANCELLED"
                              ? { background: "#fee2e2", color: "#b91c1c" }
                              : {}
                          }
                        >
                          {po.status === "RECEIVED" && "● "}
                          {po.status === "PENDING" && "⏳ "}
                          {po.status === "CANCELLED" && "✕ "}
                          {po.status}
                        </span>
                      </td>

                      <td className="amount">
                        ₹{Number(po.total_amount || 0).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                      </td>

                      <td style={{ textAlign: "right" }}>
                        <div
                          style={{
                            display: "inline-flex",
                            gap: "8px",
                            justifyContent: "flex-end",
                          }}
                        >
                          <button
                            type="button"
                            className="secondary-button"
                            style={{ padding: "6px 12px", fontSize: "12px" }}
                            onClick={() => handleViewDetails(po)}
                          >
                            Details
                          </button>

                          {canManage && po.status === "PENDING" && (
                            <>
                              <button
                                type="button"
                                className="primary-button"
                                style={{ padding: "6px 12px", fontSize: "12px" }}
                                disabled={actionLoadingId === po.id}
                                onClick={() => handleStatusChange(po.id, "RECEIVED")}
                              >
                                Receive
                              </button>

                              <button
                                type="button"
                                className="danger-button"
                                style={{ padding: "6px 12px", fontSize: "12px" }}
                                disabled={actionLoadingId === po.id}
                                onClick={() => handleStatusChange(po.id, "CANCELLED")}
                              >
                                Cancel
                              </button>
                            </>
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

export default Purchases;
