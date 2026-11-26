import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  clearReadNotifications,
  deleteNotification,
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
  scanNotifications,
} from "../../services/api";

function Notifications() {
  const navigate = useNavigate();

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [scanning, setScanning] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Filter States
  const [statusFilter, setStatusFilter] = useState("ALL"); // ALL, UNREAD, READ
  const [typeFilter, setTypeFilter] = useState("ALL"); // ALL, STOCK_ALERT, INVOICE_OVERDUE, PURCHASE_PENDING, NEW_SALE, PAYMENT_RECEIVED
  const [search, setSearch] = useState("");

  useEffect(() => {
    loadNotifications();
  }, [statusFilter, typeFilter]);

  async function loadNotifications() {
    setLoading(true);
    setError("");
    try {
      const params = {};
      if (statusFilter === "UNREAD") params.unreadOnly = true;
      if (typeFilter !== "ALL") params.type = typeFilter;
      if (search.trim()) params.search = search.trim();

      const data = await getNotifications(params);
      let list = Array.isArray(data) ? data : [];

      if (statusFilter === "READ") {
        list = list.filter((n) => n.is_read);
      }

      setNotifications(list);
    } catch (err) {
      setError(err.message || "Failed to load notifications");
    } finally {
      setLoading(false);
    }
  }

  async function handleMarkSingleRead(id) {
    try {
      await markNotificationRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, is_read: true } : n))
      );
    } catch (err) {
      setError(err.message || "Failed to mark notification as read");
    }
  }

  async function handleMarkAllRead() {
    try {
      await markAllNotificationsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, is_read: true })));
      setSuccess("All notifications marked as read.");
      setTimeout(() => setSuccess(""), 3000);
    } catch (err) {
      setError(err.message || "Failed to mark all as read");
    }
  }

  async function handleDelete(id) {
    if (!window.confirm("Delete this notification?")) return;
    try {
      await deleteNotification(id);
      setNotifications((prev) => prev.filter((n) => n.id !== id));
      setSuccess("Notification removed.");
      setTimeout(() => setSuccess(""), 3000);
    } catch (err) {
      setError(err.message || "Failed to delete notification");
    }
  }

  async function handleClearRead() {
    if (!window.confirm("Remove all read notifications?")) return;
    try {
      await clearReadNotifications();
      setNotifications((prev) => prev.filter((n) => !n.is_read));
      setSuccess("Read notifications cleared.");
      setTimeout(() => setSuccess(""), 3000);
    } catch (err) {
      setError(err.message || "Failed to clear read notifications");
    }
  }

  async function handleAuditScan() {
    setScanning(true);
    setError("");
    setSuccess("");
    try {
      const res = await scanNotifications();
      setSuccess(
        `System audit complete! ${res?.generated_alerts || 0} new alert(s) detected.`
      );
      loadNotifications();
      setTimeout(() => setSuccess(""), 4000);
    } catch (err) {
      setError(err.message || "Failed to perform system audit scan");
    } finally {
      setScanning(false);
    }
  }

  // Filtered in-memory search
  const filteredNotifications = notifications.filter((item) => {
    if (!search.trim()) return true;
    const q = search.toLowerCase();
    return (
      item.title?.toLowerCase().includes(q) ||
      item.message?.toLowerCase().includes(q) ||
      item.type?.toLowerCase().includes(q)
    );
  });

  // KPI Metrics Calculation
  const totalCount = notifications.length;
  const unreadCount = notifications.filter((n) => !n.is_read).length;
  const stockAlertsCount = notifications.filter((n) => n.type === "STOCK_ALERT").length;
  const overdueCount = notifications.filter((n) => n.type === "INVOICE_OVERDUE").length;

  const getTypeBadge = (type) => {
    switch (type) {
      case "STOCK_ALERT":
        return { label: "Stock Alert", bg: "#fef2f2", color: "#dc2626", border: "#fecaca", icon: "📦" };
      case "INVOICE_OVERDUE":
        return { label: "Overdue Invoice", bg: "#fff1f2", color: "#e11d48", border: "#ffe4e6", icon: "⏰" };
      case "PURCHASE_PENDING":
        return { label: "Purchase Order", bg: "#fffbeb", color: "#d97706", border: "#fde68a", icon: "📥" };
      case "NEW_SALE":
        return { label: "New Sale", bg: "#eff6ff", color: "#2563eb", border: "#bfdbfe", icon: "💰" };
      case "PAYMENT_RECEIVED":
        return { label: "Payment Received", bg: "#ecfdf5", color: "#059669", border: "#a7f3d0", icon: "✅" };
      default:
        return { label: "System", bg: "#f8fafc", color: "#475569", border: "#e2e8f0", icon: "🔔" };
    }
  };

  const getSeverityStyle = (severity) => {
    switch (severity) {
      case "DANGER":
        return { borderLeft: "5px solid #ef4444" };
      case "WARNING":
        return { borderLeft: "5px solid #f59e0b" };
      case "SUCCESS":
        return { borderLeft: "5px solid #10b981" };
      default:
        return { borderLeft: "5px solid #3b82f6" };
    }
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
      {/* =====================================================
          PAGE HEADER
      ===================================================== */}
      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          alignItems: "center",
          justifyContent: "space-between",
          gap: "15px",
          background: "#ffffff",
          padding: "20px 24px",
          borderRadius: "12px",
          border: "1px solid #e2e8f0",
        }}
      >
        <div>
          <h1
            style={{
              margin: 0,
              fontSize: "22px",
              fontWeight: 800,
              color: "#0f172a",
              display: "flex",
              alignItems: "center",
              gap: "8px",
            }}
          >
            <span>🔔</span> Notifications &amp; Alerts Center
          </h1>
          <p style={{ margin: "4px 0 0", fontSize: "13px", color: "#64748b" }}>
            Real-time business notifications, low-stock alerts, overdue invoice reminders, and purchase tracking
          </p>
        </div>

        <div style={{ display: "flex", flexWrap: "wrap", gap: "10px" }}>
          <button
            type="button"
            onClick={handleAuditScan}
            disabled={scanning}
            style={{
              padding: "8px 16px",
              fontSize: "13px",
              fontWeight: 600,
              borderRadius: "8px",
              border: "1px solid #cbd5e1",
              background: "#f8fafc",
              color: "#334155",
              cursor: scanning ? "wait" : "pointer",
              display: "flex",
              alignItems: "center",
              gap: "6px",
            }}
            title="Scan database for low inventory, pending purchase orders, and overdue invoices"
          >
            <span>{scanning ? "⏳" : "🔄"}</span>
            <span>{scanning ? "Auditing System..." : "Audit & Refresh Alerts"}</span>
          </button>

          {unreadCount > 0 && (
            <button
              type="button"
              onClick={handleMarkAllRead}
              style={{
                padding: "8px 16px",
                fontSize: "13px",
                fontWeight: 600,
                borderRadius: "8px",
                border: "none",
                background: "#2563eb",
                color: "#ffffff",
                cursor: "pointer",
              }}
            >
              Mark All as Read
            </button>
          )}

          <button
            type="button"
            onClick={handleClearRead}
            style={{
              padding: "8px 16px",
              fontSize: "13px",
              fontWeight: 500,
              borderRadius: "8px",
              border: "1px solid #e2e8f0",
              background: "#ffffff",
              color: "#64748b",
              cursor: "pointer",
            }}
          >
            Clear Read
          </button>
        </div>
      </div>

      {/* ALERT BANNERS */}
      {error && (
        <div
          style={{
            padding: "12px 16px",
            background: "#fef2f2",
            border: "1px solid #fecaca",
            color: "#b91c1c",
            borderRadius: "8px",
            fontSize: "13px",
          }}
        >
          {error}
        </div>
      )}

      {success && (
        <div
          style={{
            padding: "12px 16px",
            background: "#ecfdf5",
            border: "1px solid #a7f3d0",
            color: "#047857",
            borderRadius: "8px",
            fontSize: "13px",
          }}
        >
          {success}
        </div>
      )}

      {/* =====================================================
          KPI SUMMARY CARDS
      ===================================================== */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
          gap: "15px",
        }}
      >
        <div
          style={{
            background: "#ffffff",
            padding: "18px",
            borderRadius: "12px",
            border: "1px solid #e2e8f0",
            borderLeft: "4px solid #3b82f6",
          }}
        >
          <div style={{ fontSize: "12px", fontWeight: 700, color: "#64748b", textTransform: "uppercase" }}>
            Total Notifications
          </div>
          <div style={{ fontSize: "26px", fontWeight: 800, color: "#1e293b", marginTop: "6px" }}>
            {totalCount}
          </div>
          <div style={{ fontSize: "12px", color: "#64748b", marginTop: "4px" }}>
            Company event &amp; alert history
          </div>
        </div>

        <div
          style={{
            background: "#ffffff",
            padding: "18px",
            borderRadius: "12px",
            border: "1px solid #e2e8f0",
            borderLeft: "4px solid #ef4444",
          }}
        >
          <div style={{ fontSize: "12px", fontWeight: 700, color: "#64748b", textTransform: "uppercase" }}>
            Unread Alerts
          </div>
          <div style={{ fontSize: "26px", fontWeight: 800, color: unreadCount > 0 ? "#dc2626" : "#1e293b", marginTop: "6px" }}>
            {unreadCount}
          </div>
          <div style={{ fontSize: "12px", color: unreadCount > 0 ? "#dc2626" : "#64748b", marginTop: "4px" }}>
            {unreadCount > 0 ? "Requires review or action" : "All alerts caught up"}
          </div>
        </div>

        <div
          style={{
            background: "#ffffff",
            padding: "18px",
            borderRadius: "12px",
            border: "1px solid #e2e8f0",
            borderLeft: "4px solid #f59e0b",
          }}
        >
          <div style={{ fontSize: "12px", fontWeight: 700, color: "#64748b", textTransform: "uppercase" }}>
            Stock Warnings
          </div>
          <div style={{ fontSize: "26px", fontWeight: 800, color: "#b45309", marginTop: "6px" }}>
            {stockAlertsCount}
          </div>
          <div style={{ fontSize: "12px", color: "#64748b", marginTop: "4px" }}>
            Low or depleted product stock
          </div>
        </div>

        <div
          style={{
            background: "#ffffff",
            padding: "18px",
            borderRadius: "12px",
            border: "1px solid #e2e8f0",
            borderLeft: "4px solid #e11d48",
          }}
        >
          <div style={{ fontSize: "12px", fontWeight: 700, color: "#64748b", textTransform: "uppercase" }}>
            Overdue Invoices
          </div>
          <div style={{ fontSize: "26px", fontWeight: 800, color: "#e11d48", marginTop: "6px" }}>
            {overdueCount}
          </div>
          <div style={{ fontSize: "12px", color: "#64748b", marginTop: "4px" }}>
            Past due customer receivables
          </div>
        </div>
      </div>

      {/* =====================================================
          FILTER BAR & SEARCH
      ===================================================== */}
      <div
        style={{
          background: "#ffffff",
          padding: "16px 20px",
          borderRadius: "12px",
          border: "1px solid #e2e8f0",
          display: "flex",
          flexWrap: "wrap",
          alignItems: "center",
          justifyContent: "space-between",
          gap: "14px",
        }}
      >
        {/* Status Pills */}
        <div style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}>
          {[
            { id: "ALL", label: "All Status" },
            { id: "UNREAD", label: `Unread (${unreadCount})` },
            { id: "READ", label: "Read" },
          ].map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setStatusFilter(tab.id)}
              style={{
                padding: "6px 14px",
                fontSize: "12px",
                fontWeight: statusFilter === tab.id ? 700 : 500,
                borderRadius: "6px",
                border: "none",
                background: statusFilter === tab.id ? "#0f172a" : "#f1f5f9",
                color: statusFilter === tab.id ? "#ffffff" : "#475569",
                cursor: "pointer",
                transition: "all 0.15s ease",
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Type Filter Select */}
        <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", alignItems: "center" }}>
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            style={{
              padding: "7px 12px",
              fontSize: "12px",
              borderRadius: "6px",
              border: "1px solid #cbd5e1",
              background: "#ffffff",
              color: "#334155",
              fontWeight: 500,
            }}
          >
            <option value="ALL">All Alert Types</option>
            <option value="STOCK_ALERT">Stock Alerts (Low &amp; Out)</option>
            <option value="INVOICE_OVERDUE">Overdue Invoices</option>
            <option value="PURCHASE_PENDING">Pending Purchases</option>
            <option value="NEW_SALE">New Sales</option>
            <option value="PAYMENT_RECEIVED">Customer Payments</option>
          </select>

          {/* Search Box */}
          <input
            type="text"
            placeholder="Search alerts, products, invoices..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{
              padding: "7px 12px",
              fontSize: "12px",
              borderRadius: "6px",
              border: "1px solid #cbd5e1",
              background: "#ffffff",
              minWidth: "220px",
              color: "#334155",
            }}
          />
        </div>
      </div>

      {/* =====================================================
          NOTIFICATIONS FEED LIST
      ===================================================== */}
      <div
        style={{
          background: "#ffffff",
          borderRadius: "12px",
          border: "1px solid #e2e8f0",
          overflow: "hidden",
        }}
      >
        {loading ? (
          <div style={{ padding: "50px", textAlign: "center", color: "#64748b" }}>
            <div
              style={{
                width: "28px",
                height: "28px",
                border: "3px solid #2563eb",
                borderTopColor: "transparent",
                borderRadius: "50%",
                animation: "spin 1s linear infinite",
                margin: "0 auto 12px",
              }}
            />
            <p style={{ margin: 0, fontSize: "14px" }}>Loading notification center...</p>
          </div>
        ) : filteredNotifications.length === 0 ? (
          <div style={{ padding: "60px 20px", textAlign: "center", color: "#94a3b8" }}>
            <div style={{ fontSize: "36px", marginBottom: "12px" }}>🎉</div>
            <strong style={{ fontSize: "16px", color: "#334155" }}>
              No notifications matching current filters
            </strong>
            <p style={{ margin: "6px 0 0", fontSize: "13px" }}>
              Your system is running smoothly. Use "Audit &amp; Refresh Alerts" to run a fresh scan.
            </p>
          </div>
        ) : (
          <div>
            {filteredNotifications.map((n) => {
              const badge = getTypeBadge(n.type);
              const severityBorder = getSeverityStyle(n.severity);

              return (
                <div
                  key={n.id}
                  style={{
                    padding: "16px 20px",
                    borderBottom: "1px solid #f1f5f9",
                    background: n.is_read ? "#ffffff" : "#f8fbff",
                    display: "flex",
                    flexWrap: "wrap",
                    alignItems: "flex-start",
                    justifyContent: "space-between",
                    gap: "14px",
                    transition: "background 0.15s ease",
                    ...severityBorder,
                  }}
                >
                  <div style={{ display: "flex", gap: "14px", flex: 1, minWidth: "280px" }}>
                    <div
                      style={{
                        fontSize: "24px",
                        lineHeight: 1,
                        paddingTop: "2px",
                      }}
                    >
                      {badge.icon}
                    </div>

                    <div style={{ flex: 1 }}>
                      <div
                        style={{
                          display: "flex",
                          flexWrap: "wrap",
                          alignItems: "center",
                          gap: "8px",
                          marginBottom: "4px",
                        }}
                      >
                        <span
                          style={{
                            fontSize: "14px",
                            fontWeight: n.is_read ? 600 : 700,
                            color: "#0f172a",
                          }}
                        >
                          {n.title}
                        </span>

                        <span
                          style={{
                            fontSize: "11px",
                            fontWeight: 700,
                            padding: "2px 7px",
                            borderRadius: "4px",
                            background: badge.bg,
                            color: badge.color,
                            border: `1px solid ${badge.border}`,
                          }}
                        >
                          {badge.label}
                        </span>

                        {!n.is_read && (
                          <span
                            style={{
                              fontSize: "10px",
                              fontWeight: 700,
                              padding: "2px 6px",
                              borderRadius: "4px",
                              background: "#dbeafe",
                              color: "#1d4ed8",
                              textTransform: "uppercase",
                            }}
                          >
                            Unread
                          </span>
                        )}
                      </div>

                      <p
                        style={{
                          margin: 0,
                          fontSize: "13px",
                          color: "#475569",
                          lineHeight: "1.45",
                        }}
                      >
                        {n.message}
                      </p>

                      <div
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: "12px",
                          marginTop: "8px",
                          fontSize: "11px",
                          color: "#94a3b8",
                        }}
                      >
                        <span>🕒 {n.time_ago}</span>
                        {n.created_at && <span>({n.created_at.slice(0, 10)})</span>}
                        {n.dedup_key && <span>Key: {n.dedup_key}</span>}
                      </div>
                    </div>
                  </div>

                  {/* Actions Column */}
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: "8px",
                      alignSelf: "center",
                    }}
                  >
                    {n.action_url && (
                      <button
                        type="button"
                        onClick={() => {
                          if (!n.is_read) handleMarkSingleRead(n.id);
                          navigate(n.action_url);
                        }}
                        style={{
                          padding: "6px 12px",
                          fontSize: "12px",
                          fontWeight: 600,
                          borderRadius: "6px",
                          border: "1px solid #cbd5e1",
                          background: "#ffffff",
                          color: "#2563eb",
                          cursor: "pointer",
                          display: "flex",
                          alignItems: "center",
                          gap: "4px",
                        }}
                      >
                        <span>View Details</span>
                        <span>→</span>
                      </button>
                    )}

                    {!n.is_read && (
                      <button
                        type="button"
                        onClick={() => handleMarkSingleRead(n.id)}
                        style={{
                          padding: "6px 10px",
                          fontSize: "12px",
                          fontWeight: 500,
                          borderRadius: "6px",
                          border: "1px solid #e2e8f0",
                          background: "#f8fafc",
                          color: "#475569",
                          cursor: "pointer",
                        }}
                        title="Mark as read"
                      >
                        ✓ Read
                      </button>
                    )}

                    <button
                      type="button"
                      onClick={() => handleDelete(n.id)}
                      style={{
                        padding: "6px 9px",
                        fontSize: "12px",
                        borderRadius: "6px",
                        border: "1px solid #fee2e2",
                        background: "#fef2f2",
                        color: "#dc2626",
                        cursor: "pointer",
                      }}
                      title="Delete alert"
                    >
                      🗑️
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

export default Notifications;
