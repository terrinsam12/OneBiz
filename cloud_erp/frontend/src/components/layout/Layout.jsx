import { useEffect, useState } from "react";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { getCompanies } from "../../services/api";
import Sidebar from "./Sidebar";
import NotificationBell from "./NotificationBell";

function Layout() {
  const navigate = useNavigate();
  const location = useLocation();

  const [companies, setCompanies] = useState([]);
  const [companyId, setCompanyId] = useState(
    Number(localStorage.getItem("erp_company_id")) || 1
  );

  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("erp_user")) || null;
    } catch {
      return null;
    }
  })();

  useEffect(() => {
    loadCompanies();
  }, []);

  async function loadCompanies() {
    try {
      const data = await getCompanies();
      const list = Array.isArray(data) ? data : data?.companies || [];
      setCompanies(list);

      if (list.length > 0) {
        const savedId = Number(localStorage.getItem("erp_company_id"));
        const exists = list.some((c) => Number(c.id) === savedId);
        if (!exists) {
          const firstId = Number(list[0].id);
          localStorage.setItem("erp_company_id", String(firstId));
          setCompanyId(firstId);
        }
      }
    } catch (error) {
      console.error("Unable to load companies:", error);
    }
  }

  function handleLogout() {
    localStorage.removeItem("erp_token");
    localStorage.removeItem("erp_user");
    localStorage.removeItem("erp_company_id");
    localStorage.removeItem("erp_company_name");
    navigate("/login");
  }

  const selectedCompany = companies.find(
    (c) => Number(c.id) === Number(companyId)
  );

  // Derive human-readable page name for breadcrumb
  const getPageTitle = () => {
    const path = location.pathname.toLowerCase();
    if (path.includes("dashboard")) return { section: "Operations", page: "Executive Dashboard" };
    if (path.includes("products")) return { section: "Operations", page: "Product Catalog" };
    if (path.includes("inventory")) return { section: "Operations", page: "Inventory Management" };
    if (path.includes("purchases")) return { section: "Operations", page: "Purchase Orders" };
    if (path.includes("sales")) return { section: "Operations", page: "Sales & Billing" };
    if (path.includes("receivables")) return { section: "Operations", page: "Customer Receivables" };
    if (path.includes("expenses")) return { section: "Operations", page: "Expense Management" };
    if (path.includes("customers")) return { section: "Directory", page: "Customers" };
    if (path.includes("suppliers")) return { section: "Directory", page: "Suppliers" };
    if (path.includes("finance")) return { section: "Intelligence", page: "Financial Overview & P&L" };
    if (path.includes("reports")) return { section: "Intelligence", page: "Reports & Exports" };
    if (path.includes("analytics")) return { section: "Intelligence", page: "Business Analytics" };
    if (path.includes("forecast")) return { section: "Intelligence", page: "AI Demand Forecast" };
    if (path.includes("companies")) return { section: "Settings", page: "Company Profiles" };
    if (path.includes("employees")) return { section: "Settings", page: "Employee Management" };
    if (path.includes("notifications")) return { section: "System", page: "Notification Center" };
    return { section: "Workspace", page: "MSME Cloud ERP" };
  };

  const breadcrumb = getPageTitle();

  return (
    <div className="app-layout">
      {/* FIXED DARK NAVY SIDEBAR */}
      <Sidebar />

      {/* WORKSPACE AREA */}
      <main className="main-content">
        {/* STICKY TOP NAVBAR */}
        <header className="topbar">
          <div className="topbar-left">
            <div className="topbar-title-block">
              <h2>{breadcrumb.page}</h2>
              <span>
                MSME ERP &bull; {breadcrumb.section} &bull;{" "}
                <strong style={{ color: "#2563eb" }}>
                  {selectedCompany?.name || `Company #${companyId}`}
                </strong>
              </span>
            </div>
          </div>

          <div className="topbar-user">
            {/* Notification Bell */}

            {/* Notification Bell */}
            <NotificationBell />

            {/* User Profile Pill */}
            <div className="topbar-user-profile">
              <div className="avatar">
                {currentUser?.name ? currentUser.name.charAt(0).toUpperCase() : "A"}
              </div>
              <div className="topbar-user-details" style={{ marginRight: "6px" }}>
                <strong>{currentUser?.name || "Admin"}</strong>
                <small>
                  {currentUser?.role === "COMPANY_ADMIN"
                    ? "Admin"
                    : currentUser?.role === "MANAGER"
                      ? "Manager"
                      : "Staff"}
                </small>
              </div>
              <button
                type="button"
                onClick={handleLogout}
                style={{
                  border: "none",
                  background: "transparent",
                  color: "#94a3b8",
                  fontSize: "16px",
                  cursor: "pointer",
                  padding: "4px 8px",
                  borderRadius: "4px",
                  display: "flex",
                  alignItems: "center",
                }}
                title="Sign out"
              >
                ⇥
              </button>
            </div>
          </div>
        </header>

        {/* PAGE CONTENT CONTAINER */}
        <section className="page-content">
          <Outlet />
        </section>
      </main>
    </div>
  );
}

export default Layout;
