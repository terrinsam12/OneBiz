import { useEffect, useState, useMemo } from "react";
import {
  getSuppliers,
  createSupplier,
  updateSupplier,
  deleteSupplier,
  getSupplierPurchases,
} from "../../services/api";

function Suppliers() {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Filters
  const [search, setSearch] = useState("");

  // Modal states
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState(null);
  const [historySupplier, setHistorySupplier] = useState(null);
  const [supplierPurchases, setSupplierPurchases] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [saving, setSaving] = useState(false);

  // Form states
  const [form, setForm] = useState({
    name: "",
    contact_person: "",
    phone: "",
    email: "",
    address: "",
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
    loadSuppliers();
  }, []);

  async function loadSuppliers() {
    try {
      setLoading(true);
      setError("");
      const data = await getSuppliers({ search });
      setSuppliers(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Failed to load suppliers.");
    } finally {
      setLoading(false);
    }
  }

  function handleSearchSubmit(e) {
    if (e) e.preventDefault();
    loadSuppliers();
  }

  function handleResetSearch() {
    setSearch("");
    setTimeout(() => {
      loadSuppliers();
    }, 0);
  }

  // Add Supplier Submit
  async function handleCreateSubmit(e) {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!form.name.trim()) {
      setError("Supplier name is required.");
      return;
    }

    try {
      setSaving(true);
      await createSupplier({
        name: form.name.trim(),
        contact_person: form.contact_person.trim(),
        phone: form.phone.trim(),
        email: form.email.trim(),
        address: form.address.trim(),
      });

      setSuccess(`Supplier "${form.name}" added successfully.`);
      setShowAddModal(false);
      setForm({
        name: "",
        contact_person: "",
        phone: "",
        email: "",
        address: "",
      });
      await loadSuppliers();
    } catch (err) {
      setError(err.message || "Failed to create supplier.");
    } finally {
      setSaving(false);
    }
  }

  // Open Edit Modal
  function openEditModal(sup) {
    setError("");
    setSuccess("");
    setEditingSupplier(sup);
    setForm({
      name: sup.name || "",
      contact_person: sup.contact_person || "",
      phone: sup.phone || "",
      email: sup.email || "",
      address: sup.address || "",
    });
  }

  // Update Supplier Submit
  async function handleUpdateSubmit(e) {
    e.preventDefault();
    if (!editingSupplier) return;

    setError("");
    setSuccess("");

    if (!form.name.trim()) {
      setError("Supplier name is required.");
      return;
    }

    try {
      setSaving(true);
      await updateSupplier(editingSupplier.id, {
        name: form.name.trim(),
        contact_person: form.contact_person.trim(),
        phone: form.phone.trim(),
        email: form.email.trim(),
        address: form.address.trim(),
      });

      setSuccess(`Supplier "${form.name}" updated successfully.`);
      setEditingSupplier(null);
      await loadSuppliers();
    } catch (err) {
      setError(err.message || "Failed to update supplier.");
    } finally {
      setSaving(false);
    }
  }

  // Delete Supplier
  async function handleDelete(sup) {
    if (!window.confirm(`Are you sure you want to delete supplier "${sup.name}"?`)) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      await deleteSupplier(sup.id);
      setSuccess(`Supplier "${sup.name}" deleted.`);
      await loadSuppliers();
    } catch (err) {
      setError(err.message || "Failed to delete supplier.");
    }
  }

  // View Supplier Purchase History
  async function handleViewHistory(sup) {
    setHistorySupplier(sup);
    setSupplierPurchases([]);
    try {
      setLoadingHistory(true);
      const purchases = await getSupplierPurchases(sup.id);
      setSupplierPurchases(Array.isArray(purchases) ? purchases : []);
    } catch (err) {
      setError(err.message || "Failed to load supplier purchase history.");
    } finally {
      setLoadingHistory(false);
    }
  }

  // Summary Metrics
  const totalSuppliers = suppliers.length;
  const totalOrders = suppliers.reduce((sum, s) => sum + (s.purchase_count || 0), 0);
  const totalSpend = suppliers.reduce((sum, s) => sum + (s.total_spend || 0), 0);

  return (
    <div className="page-content">
      {/* =====================================================
          PAGE HEADER
      ===================================================== */}
      <div className="page-heading">
        <div>
          <h1>Supplier Management</h1>
          <p>Maintain your supplier directory, manage vendor contact details, and track purchase history.</p>
        </div>

        {canManage && (
          <button
            type="button"
            className="primary-button"
            onClick={() => {
              setError("");
              setSuccess("");
              setForm({
                name: "",
                contact_person: "",
                phone: "",
                email: "",
                address: "",
              });
              setShowAddModal(true);
            }}
          >
            + Add Supplier
          </button>
        )}
      </div>

      {/* =====================================================
          OVERVIEW STATS
      ===================================================== */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon revenue" style={{ background: "#e0f2fe", color: "#0284c7" }}>
            🏢
          </div>
          <div>
            <span>ACTIVE SUPPLIERS</span>
            <h2>{totalSuppliers}</h2>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon units" style={{ background: "#ede9fe", color: "#7c3aed" }}>
            📦
          </div>
          <div>
            <span>TOTAL PURCHASE ORDERS</span>
            <h2>{totalOrders}</h2>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon revenue" style={{ background: "#dcfce7", color: "#16a34a" }}>
            ₹
          </div>
          <div>
            <span>TOTAL PROCUREMENT SPEND</span>
            <h2>₹{totalSpend.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</h2>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon transactions" style={{ background: "#fef3c7", color: "#d97706" }}>
            🛡
          </div>
          <div>
            <span>ACCESS LEVEL</span>
            <h2 style={{ fontSize: "16px", marginTop: "10px" }}>
              {canManage ? "Manager / Admin" : "Staff (View Only)"}
            </h2>
          </div>
        </div>
      </div>

      {/* =====================================================
          ALERTS
      ===================================================== */}
      {success && <div className="success-message">✓ {success}</div>}
      {error && <div className="error-message">⚠ {error}</div>}

      {/* =====================================================
          SEARCH TOOLBAR
      ===================================================== */}
      <form onSubmit={handleSearchSubmit} className="filter-toolbar">
        <div className="search-input-wrapper">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            placeholder="Search suppliers by name, contact person, phone, email, address..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <div className="filter-actions">
          <button
            type="submit"
            className="primary-button"
            style={{ padding: "11px 18px", fontSize: "14px" }}
          >
            Search
          </button>

          {search && (
            <button
              type="button"
              className="secondary-button"
              onClick={handleResetSearch}
              style={{ padding: "11px 16px", fontSize: "14px" }}
            >
              Reset
            </button>
          )}
        </div>
      </form>

      {/* =====================================================
          ADD SUPPLIER MODAL / CARD
      ===================================================== */}
      {showAddModal && (
        <section className="form-card" style={{ marginBottom: "25px", border: "2px solid #2563eb" }}>
          <div className="card-header">
            <div>
              <h3>Add New Supplier</h3>
              <p>Register a vendor or supplier for your company's procurement operations.</p>
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
                <label>Supplier / Company Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Apex Industrial Supplies"
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Contact Person</label>
                <input
                  type="text"
                  placeholder="e.g. Rajesh Kumar"
                  value={form.contact_person}
                  onChange={(e) => setForm({ ...form, contact_person: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Phone Number</label>
                <input
                  type="text"
                  placeholder="e.g. 9876543210"
                  value={form.phone}
                  onChange={(e) => setForm({ ...form, phone: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Email Address</label>
                <input
                  type="email"
                  placeholder="e.g. sales@apexsupplies.com"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                />
              </div>

              <div className="form-group" style={{ gridColumn: "1 / -1" }}>
                <label>Business Address</label>
                <input
                  type="text"
                  placeholder="e.g. Plot 14, Phase II, Industrial Area, Bangalore"
                  value={form.address}
                  onChange={(e) => setForm({ ...form, address: e.target.value })}
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
              <button type="submit" className="primary-button" disabled={saving}>
                {saving ? "Saving..." : "Save Supplier"}
              </button>
            </div>
          </form>
        </section>
      )}

      {/* =====================================================
          EDIT SUPPLIER MODAL / CARD
      ===================================================== */}
      {editingSupplier && (
        <section className="form-card" style={{ marginBottom: "25px", border: "2px solid #7c3aed" }}>
          <div className="card-header">
            <div>
              <h3>Edit Supplier: {editingSupplier.name}</h3>
              <p>Update supplier contact information and address details.</p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setEditingSupplier(null)}
            >
              ✕ Cancel
            </button>
          </div>

          <form onSubmit={handleUpdateSubmit}>
            <div className="form-grid">
              <div className="form-group">
                <label>Supplier / Company Name *</label>
                <input
                  type="text"
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Contact Person</label>
                <input
                  type="text"
                  value={form.contact_person}
                  onChange={(e) => setForm({ ...form, contact_person: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Phone Number</label>
                <input
                  type="text"
                  value={form.phone}
                  onChange={(e) => setForm({ ...form, phone: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Email Address</label>
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                />
              </div>

              <div className="form-group" style={{ gridColumn: "1 / -1" }}>
                <label>Business Address</label>
                <input
                  type="text"
                  value={form.address}
                  onChange={(e) => setForm({ ...form, address: e.target.value })}
                />
              </div>
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() => setEditingSupplier(null)}
              >
                Cancel
              </button>
              <button type="submit" className="primary-button" disabled={saving}>
                {saving ? "Saving..." : "Update Supplier"}
              </button>
            </div>
          </form>
        </section>
      )}

      {/* =====================================================
          SUPPLIER PURCHASE HISTORY DRAWER / MODAL
      ===================================================== */}
      {historySupplier && (
        <section className="dashboard-card" style={{ marginBottom: "25px", border: "2px solid #0284c7" }}>
          <div className="card-header">
            <div>
              <h3>Purchase History: {historySupplier.name}</h3>
              <p>
                Showing procurement history for {historySupplier.name} (Contact: {historySupplier.contact_person || "N/A"})
              </p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setHistorySupplier(null)}
            >
              ✕ Close History
            </button>
          </div>

          {loadingHistory ? (
            <div className="loading-page">
              <div className="spinner"></div>
              Loading purchases...
            </div>
          ) : supplierPurchases.length === 0 ? (
            <div className="empty-state">
              No purchase orders recorded for this supplier yet.
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>PO Number</th>
                    <th>Date</th>
                    <th>Items</th>
                    <th>Status</th>
                    <th>Total Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {supplierPurchases.map((po) => (
                    <tr key={po.id}>
                      <td>
                        <strong>{po.purchase_number}</strong>
                      </td>
                      <td>
                        {po.created_at ? new Date(po.created_at).toLocaleDateString() : "N/A"}
                      </td>
                      <td>
                        {po.items?.length || 0} product(s) (
                        {po.items?.reduce((sum, it) => sum + it.quantity, 0) || 0} units)
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
                          {po.status}
                        </span>
                      </td>
                      <td className="amount">
                        ₹{Number(po.total_amount || 0).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
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
          SUPPLIERS DIRECTORY TABLE
      ===================================================== */}
      <section className="dashboard-card">
        <div className="card-header">
          <div>
            <h3>Supplier Directory</h3>
            <p>Showing {suppliers.length} vendor{suppliers.length === 1 ? "" : "s"}</p>
          </div>
        </div>

        {loading ? (
          <div className="loading-page">
            <div className="spinner"></div>
            Loading suppliers...
          </div>
        ) : suppliers.length === 0 ? (
          <div className="empty-state">
            No suppliers found. {canManage ? "Click \"+ Add Supplier\" above to add your first vendor." : ""}
          </div>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Supplier Name</th>
                  <th>Contact Person</th>
                  <th>Contact Info</th>
                  <th>Address</th>
                  <th>Purchase Orders</th>
                  <th>Total Spend</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {suppliers.map((sup) => (
                  <tr key={sup.id}>
                    <td>
                      <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                        <div
                          className="avatar"
                          style={{
                            background: "#e0f2fe",
                            color: "#0284c7",
                            fontWeight: 700,
                          }}
                        >
                          {sup.name ? sup.name.charAt(0).toUpperCase() : "S"}
                        </div>
                        <div>
                          <strong>{sup.name}</strong>
                          <div style={{ fontSize: "11px", color: "#9ca3af" }}>
                            Added {sup.created_at ? new Date(sup.created_at).toLocaleDateString() : ""}
                          </div>
                        </div>
                      </div>
                    </td>

                    <td>{sup.contact_person || "—"}</td>

                    <td>
                      <div style={{ fontSize: "13px" }}>{sup.phone || "—"}</div>
                      <div style={{ fontSize: "11px", color: "#9ca3af" }}>
                        {sup.email || ""}
                      </div>
                    </td>

                    <td style={{ maxWidth: "220px", fontSize: "12px", color: "#6b7280" }}>
                      {sup.address || "—"}
                    </td>

                    <td>
                      <span
                        style={{
                          display: "inline-block",
                          padding: "3px 9px",
                          borderRadius: "12px",
                          background: "#f1f5f9",
                          fontWeight: 600,
                          fontSize: "12px",
                        }}
                      >
                        {sup.purchase_count || 0} order{sup.purchase_count === 1 ? "" : "s"}
                      </span>
                    </td>

                    <td className="amount">
                      ₹{Number(sup.total_spend || 0).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
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
                          onClick={() => handleViewHistory(sup)}
                        >
                          Purchases
                        </button>

                        {canManage && (
                          <>
                            <button
                              type="button"
                              className="secondary-button"
                              style={{ padding: "6px 12px", fontSize: "12px" }}
                              onClick={() => openEditModal(sup)}
                            >
                              Edit
                            </button>

                            <button
                              type="button"
                              className="danger-button"
                              style={{ padding: "6px 12px", fontSize: "12px" }}
                              onClick={() => handleDelete(sup)}
                            >
                              Delete
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default Suppliers;
