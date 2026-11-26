import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getNotifications,
  getUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
  scanNotifications,
} from "../../services/api";

function NotificationBell() {
  const navigate = useNavigate();
  const dropdownRef = useRef(null);

  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [scanning, setScanning] = useState(false);

  useEffect(() => {
    fetchCount();
    const interval = setInterval(fetchCount, 30000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    }
    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen]);

  async function fetchCount() {
    try {
      const res = await getUnreadCount();
      setUnreadCount(res?.unread_count || 0);
    } catch {
      // silently ignore if auth not ready
    }
  }

  async function openDropdown() {
    const nextState = !isOpen;
    setIsOpen(nextState);

    if (nextState) {
      setLoading(true);
      try {
        const list = await getNotifications();
        setNotifications(Array.isArray(list) ? list.slice(0, 8) : []);
        fetchCount();
      } catch (err) {
        console.error("Failed to load notifications:", err);
      } finally {
        setLoading(false);
      }
    }
  }

  async function handleNotificationClick(item) {
    try {
      if (!item.is_read) {
        await markNotificationRead(item.id);
        setNotifications((prev) =>
          prev.map((n) => (n.id === item.id ? { ...n, is_read: true } : n))
        );
        setUnreadCount((prev) => Math.max(0, prev - 1));
      }
    } catch (err) {
      console.error("Failed to mark read:", err);
    }

    setIsOpen(false);
    if (item.action_url) {
      navigate(item.action_url);
    }
  }

  async function handleMarkAllRead() {
    try {
      await markAllNotificationsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, is_read: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error("Failed to mark all read:", err);
    }
  }

  async function handleTriggerScan() {
    setScanning(true);
    try {
      await scanNotifications();
      const list = await getNotifications();
      setNotifications(Array.isArray(list) ? list.slice(0, 8) : []);
      fetchCount();
    } catch (err) {
      console.error("Failed to scan alerts:", err);
    } finally {
      setScanning(false);
    }
  }

  const getTypeIcon = (type, severity) => {
    switch (type) {
      case "STOCK_ALERT":
        return severity === "DANGER" ? "🚨" : "📦";
      case "INVOICE_OVERDUE":
        return "⏰";
      case "PURCHASE_PENDING":
        return "📥";
      case "NEW_SALE":
        return "💰";
      case "PAYMENT_RECEIVED":
        return "✅";
      default:
        return "🔔";
    }
  };

  const getSeverityColor = (severity) => {
    switch (severity) {
      case "DANGER":
        return "#ef4444";
      case "WARNING":
        return "#f59e0b";
      case "SUCCESS":
        return "#10b981";
      default:
        return "#3b82f6";
    }
  };

  return (
    <div style={{ position: "relative" }} ref={dropdownRef}>
      {/* BELL TRIGGER BUTTON */}
      <button
        type="button"
        onClick={openDropdown}
        style={{
          position: "relative",
          background: isOpen ? "#f1f5f9" : "transparent",
          border: "1px solid #e2e8f0",
          borderRadius: "8px",
          width: "38px",
          height: "38px",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          cursor: "pointer",
          fontSize: "18px",
          transition: "all 0.15s ease",
          color: "#334155",
        }}
        title="Notifications & System Alerts"
        aria-label="Notifications"
      >
        <span>🔔</span>

        {unreadCount > 0 && (
          <span
            style={{
              position: "absolute",
              top: "-5px",
              right: "-5px",
              background: "#ef4444",
              color: "#ffffff",
              fontSize: "11px",
              fontWeight: 700,
              minWidth: "18px",
              height: "18px",
              borderRadius: "9px",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              padding: "0 4px",
              boxShadow: "0 2px 4px rgba(239, 68, 68, 0.4)",
              animation: "pulse 2s infinite",
            }}
          >
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {/* DROPDOWN POPUP */}
      {isOpen && (
        <div
          style={{
            position: "absolute",
            top: "46px",
            right: "0",
            width: "360px",
            maxHeight: "480px",
            background: "#ffffff",
            borderRadius: "12px",
            boxShadow:
              "0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1), 0 0 0 1px rgba(0, 0, 0, 0.05)",
            zIndex: 1000,
            display: "flex",
            flexDirection: "column",
            overflow: "hidden",
            fontFamily: "inherit",
          }}
        >
          {/* HEADER */}
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              padding: "12px 16px",
              borderBottom: "1px solid #f1f5f9",
              background: "#f8fafc",
            }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <span style={{ fontWeight: 700, fontSize: "14px", color: "#1e293b" }}>
                Notifications
              </span>
              {unreadCount > 0 && (
                <span
                  style={{
                    background: "#fee2e2",
                    color: "#dc2626",
                    fontSize: "11px",
                    fontWeight: 600,
                    padding: "1px 6px",
                    borderRadius: "6px",
                  }}
                >
                  {unreadCount} new
                </span>
              )}
            </div>

            {unreadCount > 0 && (
              <button
                type="button"
                onClick={handleMarkAllRead}
                style={{
                  background: "none",
                  border: "none",
                  color: "#2563eb",
                  fontSize: "12px",
                  fontWeight: 600,
                  cursor: "pointer",
                  padding: "2px 6px",
                }}
              >
                Mark all read
              </button>
            )}
          </div>

          {/* LIST ITEMS */}
          <div
            style={{
              flex: 1,
              overflowY: "auto",
              maxHeight: "340px",
            }}
          >
            {loading ? (
              <div
                style={{
                  padding: "30px 16px",
                  textAlign: "center",
                  color: "#64748b",
                  fontSize: "13px",
                }}
              >
                Checking notifications...
              </div>
            ) : notifications.length === 0 ? (
              <div
                style={{
                  padding: "36px 16px",
                  textAlign: "center",
                  color: "#94a3b8",
                  fontSize: "13px",
                }}
              >
                <div style={{ fontSize: "28px", marginBottom: "8px" }}>🎉</div>
                <strong>All caught up!</strong>
                <p style={{ margin: "4px 0 0", fontSize: "12px" }}>
                  No active alerts or notifications right now.
                </p>
              </div>
            ) : (
              notifications.map((item) => (
                <div
                  key={item.id}
                  onClick={() => handleNotificationClick(item)}
                  style={{
                    padding: "10px 14px",
                    borderBottom: "1px solid #f1f5f9",
                    background: item.is_read ? "#ffffff" : "#f0f7ff",
                    borderLeft: `4px solid ${getSeverityColor(item.severity)}`,
                    cursor: "pointer",
                    transition: "background 0.15s ease",
                    display: "flex",
                    gap: "10px",
                    alignItems: "flex-start",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.background = item.is_read
                      ? "#f8fafc"
                      : "#e2efff";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.background = item.is_read
                      ? "#ffffff"
                      : "#f0f7ff";
                  }}
                >
                  <div style={{ fontSize: "18px", marginTop: "1px" }}>
                    {getTypeIcon(item.type, item.severity)}
                  </div>

                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                        gap: "6px",
                      }}
                    >
                      <span
                        style={{
                          fontWeight: item.is_read ? 600 : 700,
                          fontSize: "13px",
                          color: "#1e293b",
                          whiteSpace: "nowrap",
                          overflow: "hidden",
                          textOverflow: "ellipsis",
                        }}
                      >
                        {item.title}
                      </span>
                      <span
                        style={{
                          fontSize: "11px",
                          color: "#94a3b8",
                          flexShrink: 0,
                        }}
                      >
                        {item.time_ago}
                      </span>
                    </div>

                    <p
                      style={{
                        margin: "3px 0 0",
                        fontSize: "12px",
                        color: "#64748b",
                        lineHeight: "1.35",
                        overflow: "hidden",
                        display: "-webkit-box",
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: "vertical",
                      }}
                    >
                      {item.message}
                    </p>
                  </div>

                  {!item.is_read && (
                    <span
                      style={{
                        width: "7px",
                        height: "7px",
                        borderRadius: "50%",
                        background: "#2563eb",
                        marginTop: "6px",
                        flexShrink: 0,
                      }}
                    />
                  )}
                </div>
              ))
            )}
          </div>

          {/* FOOTER ACTIONS */}
          <div
            style={{
              padding: "10px 14px",
              borderTop: "1px solid #f1f5f9",
              background: "#f8fafc",
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
            }}
          >
            <button
              type="button"
              disabled={scanning}
              onClick={handleTriggerScan}
              style={{
                background: "none",
                border: "none",
                color: "#64748b",
                fontSize: "12px",
                cursor: scanning ? "wait" : "pointer",
                display: "flex",
                alignItems: "center",
                gap: "4px",
                fontWeight: 500,
              }}
              title="Audit inventory stock, overdue receivables, and pending orders"
            >
              <span>{scanning ? "⏳" : "🔄"}</span>
              <span>{scanning ? "Auditing..." : "Audit Alerts"}</span>
            </button>

            <button
              type="button"
              onClick={() => {
                setIsOpen(false);
                navigate("/notifications");
              }}
              style={{
                background: "#2563eb",
                color: "#ffffff",
                border: "none",
                borderRadius: "6px",
                padding: "5px 12px",
                fontSize: "12px",
                fontWeight: 600,
                cursor: "pointer",
              }}
            >
              Notification Center →
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default NotificationBell;
