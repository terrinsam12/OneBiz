import { useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { getCompanies, getUnreadCount } from "../../services/api";

function Sidebar() {
  const navigate = useNavigate();

  const [companies, setCompanies] = useState([]);
  const [selectedCompanyId, setSelectedCompanyId] = useState(
    localStorage.getItem("erp_company_id") || "1"
  );
  const [loadingCompanies, setLoadingCompanies] = useState(true);
  const [unreadCount, setUnreadCount] = useState(0);

  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("erp_user")) || null;
    } catch {
      return null;
    }
  })();

  const operationsMenu = [
    { path: "/dashboard", icon: "▦", label: "Dashboard" },
    { path: "/products", icon: "◈", label: "Products" },
    { path: "/inventory", icon: "▤", label: "Inventory" },
    { path: "/purchases", icon: "📥", label: "Purchases" },
    { path: "/sales", icon: "₹", label: "Sales Orders" },
    { path: "/receivables", icon: "💳", label: "Receivables" },
    { path: "/expenses", icon: "💸", label: "Expenses" },
    { path: "/customers", icon: "♙", label: "Customers" },
    { path: "/suppliers", icon: "🚚", label: "Suppliers" },
  ];

  const intelligenceMenu = [
    { path: "/finance", icon: "💹", label: "Financial Overview", managerOrAdminOnly: true },
    { path: "/reports", icon: "📑", label: "Reports & Export" },
    { path: "/analytics", icon: "◉", label: "Analytics", managerOrAdminOnly: true },
    { path: "/forecast", icon: "⌁", label: "AI Forecast", managerOrAdminOnly: true },
  ];

  const administrationMenu = [
    { path: "/companies", icon: "▣", label: "Companies" },
    { path: "/employees", icon: "👥", label: "Employees", adminOnly: true },
    { path: "/notifications", icon: "🔔", label: "Notifications & Alerts", hasBadge: true },
  ];

  useEffect(() => {
    loadCompanies();
    loadUnread();
    const interval = setInterval(loadUnread, 30000);
    return () => clearInterval(interval);
  }, []);

  async function loadUnread() {
    try {
      const res = await getUnreadCount();
      setUnreadCount(res?.unread_count || 0);
    } catch {
      // ignore silently
    }
  }

  async function loadCompanies() {
    try {
      setLoadingCompanies(true);
      const data = await getCompanies();
      const companyList = Array.isArray(data) ? data : data?.companies || [];
      setCompanies(companyList);

      const savedCompanyId = localStorage.getItem("erp_company_id");
      if (savedCompanyId) {
        const exists = companyList.some((c) => String(c.id) === String(savedCompanyId));
        if (exists) {
          setSelectedCompanyId(String(savedCompanyId));
          return;
        }
      }

      if (companyList.length > 0) {
        const firstId = String(companyList[0].id);
        setSelectedCompanyId(firstId);
        localStorage.setItem("erp_company_id", firstId);
      }
    } catch (error) {
      console.error("Failed to load companies:", error);
    } finally {
      setLoadingCompanies(false);
    }
  }

  const selectedCompany = companies.find(
    (c) => String(c.id) === String(selectedCompanyId)
  );

  const currentCompanyName =
    localStorage.getItem("erp_company_name") ||
    selectedCompany?.name ||
    "hello company";

  const currentCompanySector =
    selectedCompany?.industry ||
    selectedCompany?.business_type ||
    "Manufacturing";

  const handleLogout = () => {
    localStorage.removeItem("erp_token");
    localStorage.removeItem("erp_user");
    localStorage.removeItem("erp_company_id");
    localStorage.removeItem("erp_company_name");
    navigate("/login");
  };

  function renderNavItems(items) {
    return items
      .filter((item) => {
        if (item.adminOnly && currentUser?.role !== "COMPANY_ADMIN") return false;
        if (item.managerOrAdminOnly && currentUser?.role === "STAFF") return false;
        return true;
      })
      .map((item) => (
        <NavLink
          key={item.path}
          to={item.path}
          className={({ isActive }) => `sidebar-link ${isActive ? "active" : ""}`}
        >
          <div className="sidebar-link-content">
            <span className="sidebar-icon">{item.icon}</span>
            <span>{item.label}</span>
          </div>

          {item.hasBadge && unreadCount > 0 && (
            <span
              style={{
                background: "#ef4444",
                color: "#ffffff",
                fontSize: "10px",
                fontWeight: 700,
                padding: "1px 6px",
                borderRadius: "10px",
                lineHeight: "14px",
              }}
            >
              {unreadCount > 99 ? "99+" : unreadCount}
            </span>
          )}
        </NavLink>
      ));
  }

  return (
    <aside className="sidebar">
      {/* BRAND BANNER */}
      <div className="sidebar-brand">
        <div className="brand-icon">M</div>
        <div>
          <h1>MSME Cloud ERP</h1>
          <span>Enterprise SaaS</span>
        </div>
      </div>

      {/* ACTIVE TENANT / FIXED ORGANIZATION IDENTITY */}
      <div className="sidebar-company-card">
        <label>Active Organization</label>
        <div className="sidebar-company-display">
          <div className="sidebar-company-avatar">🏢</div>
          <div className="sidebar-company-meta">
            <strong className="sidebar-company-name" title={currentCompanyName}>
              {currentCompanyName}
            </strong>
            <span className="sidebar-company-sector">
              Sector: {currentCompanySector}
            </span>
          </div>
        </div>
      </div>

      {/* NAVIGATION SECTIONS */}
      <div className="sidebar-section">
        <span className="section-title">Core Operations</span>
        <nav>{renderNavItems(operationsMenu)}</nav>

        <span className="section-title" style={{ marginTop: "14px" }}>
          Intelligence &amp; Reports
        </span>
        <nav>{renderNavItems(intelligenceMenu)}</nav>

        <span className="section-title" style={{ marginTop: "14px" }}>
          Administration
        </span>
        <nav>{renderNavItems(administrationMenu)}</nav>
      </div>

      {/* USER PROFILE & LOGOUT FOOTER */}
      <div className="sidebar-bottom">
        <div className="sidebar-user-info">
          <div className="sidebar-user-avatar">
            {currentUser?.name ? currentUser.name.charAt(0).toUpperCase() : "U"}
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div className="sidebar-user-name">{currentUser?.name || "Admin Owner"}</div>
            <div className="sidebar-user-role">
              {currentUser?.role === "COMPANY_ADMIN"
                ? "Company Admin"
                : currentUser?.role === "MANAGER"
                ? "Operations Manager"
                : "Staff Member"}
            </div>
          </div>
        </div>

        <button type="button" className="logout-button" onClick={handleLogout}>
          <span>⇥</span>
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
}

export default Sidebar;
