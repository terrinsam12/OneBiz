import { useEffect, useState, useMemo } from "react";
import {
  getEmployees,
  createEmployee,
  updateEmployee,
  updateEmployeeStatus,
} from "../../services/api";

function Employees() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Filters
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");

  // Modals & Form State
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const [saving, setSaving] = useState(false);

  // Current logged in user
  const currentUser = useMemo(() => {
    try {
      return JSON.parse(localStorage.getItem("erp_user")) || null;
    } catch {
      return null;
    }
  }, []);

  const isAdmin = currentUser?.role === "COMPANY_ADMIN";

  const [addForm, setAddForm] = useState({
    name: "",
    email: "",
    phone: "",
    password: "",
    role: "STAFF",
  });

  const [editForm, setEditForm] = useState({
    name: "",
    phone: "",
    role: "STAFF",
    password: "",
    is_active: true,
  });

  useEffect(() => {
    if (isAdmin) {
      loadEmployees();
    } else {
      setLoading(false);
    }
  }, [isAdmin]);

  async function loadEmployees() {
    try {
      setLoading(true);
      setError("");
      const params = {};
      if (search) params.search = search;
      if (roleFilter) params.role = roleFilter;
      if (statusFilter !== "") params.active = statusFilter;

      const data = await getEmployees(params);
      setEmployees(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Failed to load employees.");
    } finally {
      setLoading(false);
    }
  }

  // Handle Search & Filter trigger
  function handleFilterSubmit(e) {
    if (e) e.preventDefault();
    loadEmployees();
  }

  function handleResetFilters() {
    setSearch("");
    setRoleFilter("");
    setStatusFilter("");
    setTimeout(() => {
      loadEmployees();
    }, 0);
  }

  // Handle Create Employee
  async function handleCreateSubmit(e) {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!addForm.name.trim()) {
      setError("Employee name is required.");
      return;
    }
    if (!addForm.email.trim()) {
      setError("Valid email address is required.");
      return;
    }
    if (!addForm.password || addForm.password.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }

    try {
      setSaving(true);
      await createEmployee({
        name: addForm.name.trim(),
        email: addForm.email.trim(),
        phone: addForm.phone.trim(),
        password: addForm.password,
        role: addForm.role,
      });

      setSuccess(`Employee "${addForm.name}" created successfully.`);
      setShowAddModal(false);
      setAddForm({
        name: "",
        email: "",
        phone: "",
        password: "",
        role: "STAFF",
      });
      await loadEmployees();
    } catch (err) {
      setError(err.message || "Failed to create employee.");
    } finally {
      setSaving(false);
    }
  }

  // Handle Edit Employee Open
  function openEditModal(emp) {
    setError("");
    setSuccess("");
    setEditingEmployee(emp);
    setEditForm({
      name: emp.name || "",
      phone: emp.phone || "",
      role: emp.role || "STAFF",
      password: "",
      is_active: emp.is_active !== undefined ? emp.is_active : true,
    });
  }

  // Handle Edit Employee Submit
  async function handleEditSubmit(e) {
    e.preventDefault();
    if (!editingEmployee) return;

    setError("");
    setSuccess("");

    if (!editForm.name.trim()) {
      setError("Employee name is required.");
      return;
    }

    try {
      setSaving(true);
      const payload = {
        name: editForm.name.trim(),
        phone: editForm.phone.trim(),
        role: editForm.role,
        is_active: editForm.is_active,
      };

      if (editForm.password && editForm.password.trim().length >= 6) {
        payload.password = editForm.password.trim();
      }

      await updateEmployee(editingEmployee.id, payload);
      setSuccess(`Employee "${editForm.name}" updated successfully.`);
      setEditingEmployee(null);
      await loadEmployees();
    } catch (err) {
      setError(err.message || "Failed to update employee.");
    } finally {
      setSaving(false);
    }
  }

  // Handle Toggle Active/Inactive Status
  async function handleToggleStatus(emp) {
    if (emp.id === currentUser?.id) {
      setError("You cannot deactivate your own account.");
      return;
    }

    const newStatus = !emp.is_active;
    const actionLabel = newStatus ? "activate" : "deactivate";

    if (!window.confirm(`Are you sure you want to ${actionLabel} ${emp.name}?`)) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      await updateEmployeeStatus(emp.id, newStatus);
      setSuccess(`Employee "${emp.name}" is now ${newStatus ? "Active" : "Deactivated"}.`);
      await loadEmployees();
    } catch (err) {
      setError(err.message || `Failed to ${actionLabel} employee.`);
    }
  }

  // Filtered view
  const totalCount = employees.length;
  const adminCount = employees.filter((e) => e.role === "COMPANY_ADMIN").length;
  const managerCount = employees.filter((e) => e.role === "MANAGER").length;
  const staffCount = employees.filter((e) => e.role === "STAFF").length;

  if (!isAdmin) {
    return (
      <div className="page-content">
        <div className="error-card" style={{ maxWidth: "600px", margin: "40px auto", textAlign: "center" }}>
          <h2>Access Restricted</h2>
          <p>
            Employee Management is restricted to <strong>COMPANY_ADMIN</strong> accounts.
            Please contact your organization administrator if you need access.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-content">
      {/* =====================================================
          HEADER
      ===================================================== */}
      <div className="page-heading">
        <div>
          <h1>Employee Management</h1>
          <p>Manage team members, assign operational roles, and enforce company data security.</p>
        </div>

        <button
          type="button"
          className="primary-button"
          onClick={() => {
            setError("");
            setSuccess("");
            setShowAddModal(true);
          }}
        >
          + Add Employee
        </button>
      </div>

      {/* =====================================================
          STATS OVERVIEW
      ===================================================== */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon units">👥</div>
          <div>
            <span>TOTAL EMPLOYEES</span>
            <h2>{totalCount}</h2>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon customers" style={{ background: "#ede9fe", color: "#7c3aed" }}>
            👑
          </div>
          <div>
            <span>ADMINISTRATORS</span>
            <h2>{adminCount}</h2>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon revenue" style={{ background: "#e0f2fe", color: "#0284c7" }}>
            💼
          </div>
          <div>
            <span>MANAGERS</span>
            <h2>{managerCount}</h2>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon transactions" style={{ background: "#fef3c7", color: "#d97706" }}>
            🛠
          </div>
          <div>
            <span>STAFF MEMBERS</span>
            <h2>{staffCount}</h2>
          </div>
        </div>
      </div>

      {/* =====================================================
          NOTIFICATIONS
      ===================================================== */}
      {success && <div className="success-message">✓ {success}</div>}
      {error && <div className="error-message">⚠ {error}</div>}

      {/* =====================================================
          SEARCH & FILTER TOOLBAR
      ===================================================== */}
      <form onSubmit={handleFilterSubmit} className="filter-toolbar">
        <div className="search-input-wrapper">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            placeholder="Search by name, email, or phone..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <div className="filter-select-wrapper" style={{ minWidth: "170px" }}>
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
          >
            <option value="">All Roles</option>
            <option value="COMPANY_ADMIN">Company Admin</option>
            <option value="MANAGER">Manager</option>
            <option value="STAFF">Staff</option>
          </select>
        </div>

        <div className="filter-select-wrapper" style={{ minWidth: "150px" }}>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Statuses</option>
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </select>
        </div>

        <div className="filter-actions">
          <button
            type="submit"
            className="primary-button"
            style={{ padding: "11px 18px", fontSize: "14px" }}
          >
            Filter
          </button>

          {(search || roleFilter || statusFilter !== "") && (
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
          ADD EMPLOYEE MODAL / CARD
      ===================================================== */}
      {showAddModal && (
        <section className="form-card" style={{ marginBottom: "25px", border: "2px solid #2563eb" }}>
          <div className="card-header">
            <div>
              <h3>Add New Employee</h3>
              <p>Add a user to your company with specific operational permissions.</p>
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
                <label>Full Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Rahul Sharma"
                  value={addForm.name}
                  onChange={(e) => setAddForm({ ...addForm, name: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Email Address *</label>
                <input
                  type="email"
                  required
                  placeholder="e.g. rahul@company.com"
                  value={addForm.email}
                  onChange={(e) => setAddForm({ ...addForm, email: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Phone Number</label>
                <input
                  type="text"
                  placeholder="e.g. 9876543210"
                  value={addForm.phone}
                  onChange={(e) => setAddForm({ ...addForm, phone: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Temporary Password * (Min 6 chars)</label>
                <input
                  type="password"
                  required
                  minLength={6}
                  placeholder="Create employee password"
                  value={addForm.password}
                  onChange={(e) => setAddForm({ ...addForm, password: e.target.value })}
                />
              </div>

              <div className="form-group" style={{ gridColumn: "1 / -1" }}>
                <label>Assign Role *</label>
                <select
                  value={addForm.role}
                  onChange={(e) => setAddForm({ ...addForm, role: e.target.value })}
                >
                  <option value="STAFF">STAFF - Sales transactions, billing, and inventory stock in/out</option>
                  <option value="MANAGER">MANAGER - Products, inventory, sales, customers, analytics & forecasts</option>
                  <option value="COMPANY_ADMIN">COMPANY_ADMIN - Full executive control, team management, and configuration</option>
                </select>
                <small style={{ color: "#6b7280", marginTop: "4px" }}>
                  Role controls backend API permissions and accessible features.
                </small>
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
                {saving ? "Saving..." : "Create Employee Account"}
              </button>
            </div>
          </form>
        </section>
      )}

      {/* =====================================================
          EDIT EMPLOYEE MODAL / CARD
      ===================================================== */}
      {editingEmployee && (
        <section className="form-card" style={{ marginBottom: "25px", border: "2px solid #7c3aed" }}>
          <div className="card-header">
            <div>
              <h3>Edit Employee: {editingEmployee.name}</h3>
              <p>Update contact details, permissions, or reset login password.</p>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setEditingEmployee(null)}
            >
              ✕ Cancel
            </button>
          </div>

          <form onSubmit={handleEditSubmit}>
            <div className="form-grid">
              <div className="form-group">
                <label>Full Name *</label>
                <input
                  type="text"
                  required
                  value={editForm.name}
                  onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Email Address</label>
                <input
                  type="email"
                  disabled
                  value={editingEmployee.email}
                  style={{ background: "#f3f4f6", cursor: "not-allowed" }}
                />
              </div>

              <div className="form-group">
                <label>Phone Number</label>
                <input
                  type="text"
                  value={editForm.phone}
                  onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>New Password (leave blank to keep current)</label>
                <input
                  type="password"
                  placeholder="Enter new password (optional)"
                  value={editForm.password}
                  onChange={(e) => setEditForm({ ...editForm, password: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Assigned Role</label>
                <select
                  value={editForm.role}
                  disabled={editingEmployee.id === currentUser?.id}
                  onChange={(e) => setEditForm({ ...editForm, role: e.target.value })}
                  style={editingEmployee.id === currentUser?.id ? { background: "#f3f4f6", cursor: "not-allowed" } : {}}
                >
                  <option value="STAFF">STAFF</option>
                  <option value="MANAGER">MANAGER</option>
                  <option value="COMPANY_ADMIN">COMPANY_ADMIN</option>
                </select>
                {editingEmployee.id === currentUser?.id && (
                  <small style={{ color: "#d97706" }}>You cannot modify your own role.</small>
                )}
              </div>

              <div className="form-group">
                <label>Account Status</label>
                <select
                  value={String(editForm.is_active)}
                  disabled={editingEmployee.id === currentUser?.id}
                  onChange={(e) => setEditForm({ ...editForm, is_active: e.target.value === "true" })}
                  style={editingEmployee.id === currentUser?.id ? { background: "#f3f4f6", cursor: "not-allowed" } : {}}
                >
                  <option value="true">Active (Can log in)</option>
                  <option value="false">Deactivated (Login blocked)</option>
                </select>
                {editingEmployee.id === currentUser?.id && (
                  <small style={{ color: "#d97706" }}>You cannot deactivate your own account.</small>
                )}
              </div>
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() => setEditingEmployee(null)}
              >
                Cancel
              </button>
              <button type="submit" className="primary-button" disabled={saving}>
                {saving ? "Saving..." : "Save Changes"}
              </button>
            </div>
          </form>
        </section>
      )}

      {/* =====================================================
          EMPLOYEE DIRECTORY TABLE
      ===================================================== */}
      <section className="dashboard-card">
        <div className="card-header">
          <div>
            <h3>Employee Directory</h3>
            <p>Showing {employees.length} member{employees.length === 1 ? "" : "s"}</p>
          </div>
        </div>

        {loading ? (
          <div className="loading-page">
            <div className="spinner"></div>
            Loading employees...
          </div>
        ) : employees.length === 0 ? (
          <div className="empty-state">
            No employees match your search or filter criteria.
          </div>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Contact Info</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th style={{ textAlign: "right" }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {employees.map((emp) => {
                  const isSelf = emp.id === currentUser?.id;
                  const roleBadgeColor =
                    emp.role === "COMPANY_ADMIN"
                      ? { bg: "#ede9fe", color: "#7c3aed", border: "#ddd6fe" }
                      : emp.role === "MANAGER"
                      ? { bg: "#e0f2fe", color: "#0284c7", border: "#bae6fd" }
                      : { bg: "#f1f5f9", color: "#475569", border: "#e2e8f0" };

                  return (
                    <tr key={emp.id}>
                      <td>
                        <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                          <div
                            className="avatar"
                            style={{
                              background: roleBadgeColor.bg,
                              color: roleBadgeColor.color,
                              fontWeight: 700,
                            }}
                          >
                            {emp.name ? emp.name.charAt(0).toUpperCase() : "U"}
                          </div>
                          <div>
                            <strong>{emp.name}</strong>
                            {isSelf && (
                              <span
                                style={{
                                  display: "inline-block",
                                  marginLeft: "6px",
                                  fontSize: "10px",
                                  padding: "2px 6px",
                                  borderRadius: "4px",
                                  background: "#dcfce7",
                                  color: "#15803d",
                                  fontWeight: 600,
                                }}
                              >
                                YOU
                              </span>
                            )}
                          </div>
                        </div>
                      </td>

                      <td>
                        <div style={{ fontSize: "13px" }}>{emp.email}</div>
                        <div style={{ fontSize: "11px", color: "#9ca3af" }}>
                          {emp.phone || "No phone provided"}
                        </div>
                      </td>

                      <td>
                        <span
                          style={{
                            display: "inline-block",
                            padding: "4px 10px",
                            borderRadius: "12px",
                            fontSize: "11px",
                            fontWeight: 700,
                            background: roleBadgeColor.bg,
                            color: roleBadgeColor.color,
                            border: `1px solid ${roleBadgeColor.border}`,
                          }}
                        >
                          {emp.role}
                        </span>
                      </td>

                      <td>
                        <span
                          className={`status-badge ${
                            emp.is_active ? "success" : "warning"
                          }`}
                          style={
                            !emp.is_active
                              ? { background: "#fee2e2", color: "#b91c1c" }
                              : {}
                          }
                        >
                          {emp.is_active ? "● Active" : "○ Inactive"}
                        </span>
                      </td>

                      <td style={{ color: "#6b7280", fontSize: "12px" }}>
                        {emp.created_at
                          ? new Date(emp.created_at).toLocaleDateString()
                          : "N/A"}
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
                            onClick={() => openEditModal(emp)}
                          >
                            Edit
                          </button>

                          <button
                            type="button"
                            className={emp.is_active ? "danger-button" : "primary-button"}
                            style={{ padding: "6px 12px", fontSize: "12px" }}
                            disabled={isSelf}
                            title={isSelf ? "You cannot deactivate your own account" : ""}
                            onClick={() => handleToggleStatus(emp)}
                          >
                            {emp.is_active ? "Deactivate" : "Activate"}
                          </button>
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

export default Employees;
