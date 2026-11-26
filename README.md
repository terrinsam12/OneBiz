# OneBiz — Multi-Tenant Cloud ERP Platform for MSMEs 🚀

**OneBiz** is an intelligent, enterprise-grade Cloud ERP solution engineered to streamline end-to-end operational workflows for Micro, Small, and Medium Enterprises. Built with robust company-level multi-tenant isolation, role-based access control (RBAC), and predictive machine-learning intelligence.

---

### ✨ Core Capabilities

- 🏢 **Multi-Tenant Architecture** — Enforces strict company-level data isolation, allowing independent organizations to manage their operations securely.
- 📦 **Inventory & Stock Protection** — Real-time catalog tracking, safety reorder thresholds, and automated stockout prevention alerts.
- 💰 **Sales Orders & Billing** — Invoicing, customer directory management, and full tax/GST-compliant billing records.
- 💳 **Customer Receivables** — Track invoice payment statuses (Paid, Partial, Unpaid), payment histories, and overdue balances.
- 📥 **Procurement & PO Workflows** — Manage supplier purchase orders and streamline warehouse inventory restocking.
- 📊 **Business Intelligence & Exports** — Comprehensive Profit & Loss reports, expense breakdown, and one-click PDF/Excel audit exports.
- ⚡ **AI Demand Forecasting** — Predictive regression models analyzing historical sales to project 90-day demand and automate purchase recommendations.
- 🔔 **Alerts & Notification Center** — Instant low-stock alerts, overdue payment reminders, and order updates.

---

### 🛠️ Architecture & Tech Stack

- **Backend**: Java 17, Spring Boot 3, Spring Data JPA, Spring Security (JWT)
- **Frontend**: React, Vite, Modern Responsive SaaS Layout
- **Database**: H2 Database (File-based) / PostgreSQL compatible
- **Intelligence**: Linear Trend Regression & Moving Average Models

---

### 🚀 Getting Started

#### 1. Backend (Spring Boot)
```bash
cd cloud_erp/backend-java
mvn clean install
java -jar target/cloud-erp-backend-1.0.0.jar
# Backend runs at http://127.0.0.1:8000
```

#### 2. Frontend (React + Vite)
```bash
cd cloud_erp/frontend
npm install
npm run dev
# Frontend runs at http://localhost:5173
```

#### 3. Quick Launcher
Alternatively, run the root launcher script on Windows:
```cmd
run.bat
```
