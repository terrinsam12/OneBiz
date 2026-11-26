const API_BASE_URL =
  import.meta.env.VITE_API_URL ||
  "http://127.0.0.1:8000";


async function request(endpoint, options = {}) {
  const token = localStorage.getItem("erp_token");
  const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};

  const response = await fetch(
    `${API_BASE_URL}${endpoint}`,
    {
      ...options,

      headers: {
        "Content-Type": "application/json",
        ...authHeaders,
        ...(options.headers || {}),
      },
    }
  );

  const contentType =
    response.headers.get("content-type");

  let data;

  if (
    contentType &&
    contentType.includes("application/json")
  ) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    if (response.status === 401 && token) {
      if (!endpoint.startsWith("/auth/login") && !endpoint.startsWith("/auth/register")) {
        localStorage.removeItem("erp_token");
        localStorage.removeItem("erp_user");
        localStorage.removeItem("erp_company_id");
        localStorage.removeItem("erp_company_name");
        window.location.href = "/login";
      }
    }

    let message = "Request failed";

    if (typeof data === "object" && data !== null) {
      message =
        data.detail ||
        data.message ||
        "Request failed";
    } else if (data) {
      message = data;
    }

    throw new Error(message);
  }

  return data;
}


/* =====================================================
   AUTHENTICATION
===================================================== */

export const login = (data) =>
  request("/auth/login", {
    method: "POST",
    body: JSON.stringify({
      email: data.email,
      password: data.password,
    }),
  });


export const register = (data) =>
  request("/auth/register", {
    method: "POST",
    body: JSON.stringify({
      company_name: data.company_name || data.companyName,
      owner_name: data.owner_name || data.ownerName,
      email: data.email,
      phone: data.phone || "",
      password: data.password,
      business_type: data.business_type || data.businessType || "",
    }),
  });


export const getCurrentUser = () =>
  request("/auth/me");



/* =====================================================
   HEALTH
===================================================== */

export const getHealth = () =>
  request("/health");


/* =====================================================
   COMPANIES
===================================================== */

export const getCompanies = () =>
  request("/companies");


export const createCompany = (data) =>
  request("/companies", {
    method: "POST",
    body: JSON.stringify({
      name: data.name,
      industry: data.industry || "",
    }),
  });


/* =====================================================
   PRODUCTS
===================================================== */

export const getProducts = (
  companyId = 1
) =>
  request(
    `/products?company_id=${Number(companyId)}`
  );


export const createProduct = (data) =>
  request("/products", {
    method: "POST",

    body: JSON.stringify({
      company_id: Number(data.company_id),
      name: data.name,
      price: Number(data.price),
      quantity: Number(
        data.quantity ??
        data.stock ??
        0
      ),
      cost_price: Number(
        data.cost_price ?? 0
      ),
      reorder_level: Number(
        data.reorder_level ?? 5
      ),
    }),
  });


/* =====================================================
   CUSTOMERS
===================================================== */

export const getCustomers = (
  companyId = 1
) =>
  request(
    `/customers/${Number(companyId)}`
  );


export const createCustomer = (data) =>
  request("/customers", {
    method: "POST",

    body: JSON.stringify({
      company_id: Number(data.company_id),
      name: data.name,
      phone: data.phone || "",
      email: data.email || "",
      address: data.address || "",
    }),
  });


/* =====================================================
   INVENTORY
===================================================== */

export const getInventory = (
  companyId = 1
) =>
  request(
    `/inventory/${Number(companyId)}`
  );


export const stockIn = (data) =>
  request("/inventory/stock-in", {
    method: "POST",

    body: JSON.stringify({
      company_id: Number(data.company_id),
      product_id: Number(data.product_id),
      quantity: Number(data.quantity),
    }),
  });


export const stockOut = (data) =>
  request("/inventory/stock-out", {
    method: "POST",

    body: JSON.stringify({
      company_id: Number(data.company_id),
      product_id: Number(data.product_id),
      quantity: Number(data.quantity),
    }),
  });


/* =====================================================
   SALES
===================================================== */

export const getSales = (
  companyId = 1
) =>
  request(
    `/sales/${Number(companyId)}`
  );


export const createSale = (data) =>
  request("/sales", {
    method: "POST",

    body: JSON.stringify({
      company_id: Number(data.company_id),
      product_id: Number(data.product_id),
      quantity: Number(data.quantity),
      customer_id:
        data.customer_id
          ? Number(data.customer_id)
          : null,
    }),
  });


export const getSalesSummary = (
  companyId = 1
) =>
  request(
    `/sales/${Number(companyId)}/summary`
  );


/* =====================================================
   ANALYTICS
===================================================== */

export const getAnalyticsOverview = (
  companyId = 1
) =>
  request(
    `/analytics/overview?company_id=${Number(companyId)}`
  );


export const getSalesTrend = (
  companyId = 1
) =>
  request(
    `/analytics/sales-trend/${Number(companyId)}`
  );


export const getTopProducts = (
  companyId = 1
) =>
  request(
    `/analytics/top-products/${Number(companyId)}`
  );


export const getBusinessSummary = (
  companyId = 1
) =>
  request(
    `/analytics/business-summary/${Number(companyId)}`
  );


export const getSalesForecast = (
  companyId = 1
) =>
  request(
    `/analytics/sales-forecast/${Number(companyId)}`
  );


export const getDemandPrediction = (
  companyId = 1
) =>
  request(
    `/analytics/demand-prediction/${Number(companyId)}`
  );


export const getInventoryRisk = (
  companyId = 1
) =>
  request(
    `/analytics/inventory-risk/${Number(companyId)}`
  );


export const getReorderRecommendations = (
  companyId = 1
) =>
  request(
    `/analytics/reorder-recommendations/${Number(companyId)}`
  );


export const getProfitability = (
  companyId = 1
) =>
  request(
    `/analytics/profitability/${Number(companyId)}`
  );


export const getCustomerInsights = (
  companyId = 1
) =>
  request(
    `/analytics/customer-insights/${Number(companyId)}`
  );


export const getAnomalies = (
  companyId = 1
) =>
  request(
    `/analytics/anomalies/${Number(companyId)}`
  );


export const getProductInsights = (
  companyId = 1
) =>
  request(
    `/analytics/product-insights/${Number(companyId)}`
  );


export const getAISummary = (
  companyId = 1
) =>
  request(
    `/analytics/ai-summary/${Number(companyId)}`
  );


/* =====================================================
   LEGACY AI FORECAST
===================================================== */

export const getAIForecast = (
  companyId = 1
) =>
  request(
    `/ai/forecast?company_id=${Number(companyId)}`
  );


export const refreshAIForecast = (
  companyId = 1
) =>
  request(
    `/ai/forecast?company_id=${Number(companyId)}&refresh=true`
  );


/* =====================================================
   EMPLOYEE MANAGEMENT
===================================================== */

export const getEmployees = (params = {}) => {
  const query = new URLSearchParams();
  if (params.search) query.append("search", params.search);
  if (params.role) query.append("role", params.role);
  if (params.active !== undefined && params.active !== "" && params.active !== null) {
    query.append("active", params.active);
  }
  const qs = query.toString();
  return request(`/employees${qs ? `?${qs}` : ""}`);
};

export const getEmployee = (id) => request(`/employees/${id}`);

export const createEmployee = (data) =>
  request("/employees", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const updateEmployee = (id, data) =>
  request(`/employees/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const updateEmployeeStatus = (id, isActive) =>
  request(`/employees/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify({ is_active: isActive }),
  });


/* =====================================================
   SUPPLIER MANAGEMENT
===================================================== */

export const getSuppliers = (params = {}) => {
  const query = new URLSearchParams();
  if (params.search) query.append("search", params.search);
  const qs = query.toString();
  return request(`/suppliers${qs ? `?${qs}` : ""}`);
};

export const getSupplier = (id) => request(`/suppliers/${id}`);

export const getSupplierPurchases = (id) => request(`/suppliers/${id}/purchases`);

export const createSupplier = (data) =>
  request("/suppliers", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const updateSupplier = (id, data) =>
  request(`/suppliers/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteSupplier = (id) =>
  request(`/suppliers/${id}`, {
    method: "DELETE",
  });


/* =====================================================
   PURCHASE MANAGEMENT
===================================================== */

export const getPurchases = (params = {}) => {
  const query = new URLSearchParams();
  if (params.supplier_id || params.supplierId) {
    query.append("supplier_id", params.supplier_id || params.supplierId);
  }
  if (params.status && params.status !== "ALL") {
    query.append("status", params.status);
  }
  if (params.search) query.append("search", params.search);
  const qs = query.toString();
  return request(`/purchases${qs ? `?${qs}` : ""}`);
};

export const getPurchase = (id) => request(`/purchases/${id}`);

export const getPurchaseSummary = () => request("/purchases/summary");

export const createPurchase = (data) =>
  request("/purchases", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const updatePurchaseStatus = (id, status) =>
  request(`/purchases/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status }),
  });


/* =====================================================
   EXPENSE MANAGEMENT
===================================================== */

export const getExpenses = (params = {}) => {
  const query = new URLSearchParams();
  if (params.search) query.append("search", params.search);
  if (params.category && params.category !== "ALL") query.append("category", params.category);
  if (params.startDate || params.start_date) query.append("startDate", params.startDate || params.start_date);
  if (params.endDate || params.end_date) query.append("endDate", params.endDate || params.end_date);
  const qs = query.toString();
  return request(`/expenses${qs ? `?${qs}` : ""}`);
};

export const getExpense = (id) => request(`/expenses/${id}`);

export const getExpenseSummary = () => request("/expenses/summary");

export const createExpense = (data) =>
  request("/expenses", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const updateExpense = (id, data) =>
  request(`/expenses/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteExpense = (id) =>
  request(`/expenses/${id}`, {
    method: "DELETE",
  });

export const uploadReceipt = async (file) => {
  const token = localStorage.getItem("erp_token");
  const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`${API_BASE_URL}/expenses/upload`, {
    method: "POST",
    headers: {
      ...authHeaders,
    },
    body: formData,
  });

  if (!response.ok) {
    let msg = "Failed to upload receipt";
    try {
      const errData = await response.json();
      msg = errData.detail || errData.message || msg;
    } catch {}
    throw new Error(msg);
  }

  return response.json();
};


/* =====================================================
   FINANCIAL OVERVIEW & PROFITABILITY
===================================================== */

export const getFinancialOverview = (params = {}) => {
  const query = new URLSearchParams();
  if (params.startDate || params.start_date) {
    query.append("startDate", params.startDate || params.start_date);
  }
  if (params.endDate || params.end_date) {
    query.append("endDate", params.endDate || params.end_date);
  }
  const qs = query.toString();
  return request(`/finance/overview${qs ? `?${qs}` : ""}`);
};


/* =====================================================
   CUSTOMER PAYMENTS & RECEIVABLES
===================================================== */

export const getReceivablesSummary = () => request("/payments/receivables/summary");

export const getInvoiceReceivables = (params = {}) => {
  const query = new URLSearchParams();
  if (params.status && params.status !== "ALL") query.append("status", params.status);
  if (params.customerId || params.customer_id) query.append("customerId", params.customerId || params.customer_id);
  if (params.search) query.append("search", params.search);
  const qs = query.toString();
  return request(`/payments/receivables/invoices${qs ? `?${qs}` : ""}`);
};

export const getCustomerReceivables = (params = {}) => {
  const query = new URLSearchParams();
  if (params.search) query.append("search", params.search);
  const qs = query.toString();
  return request(`/payments/receivables/customers${qs ? `?${qs}` : ""}`);
};

export const recordPayment = (data) =>
  request("/payments", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const getPayments = (params = {}) => {
  const query = new URLSearchParams();
  if (params.customerId || params.customer_id) query.append("customerId", params.customerId || params.customer_id);
  if (params.saleId || params.sale_id) query.append("saleId", params.saleId || params.sale_id);
  const qs = query.toString();
  return request(`/payments${qs ? `?${qs}` : ""}`);
};

export const getSalePayments = (saleId) => request(`/payments/sale/${saleId}`);

export const getCustomerPayments = (customerId) => request(`/payments/customer/${customerId}`);


/* =====================================================
   BUSINESS REPORTS & EXPORT MANAGEMENT
===================================================== */

export const getSalesReport = (params = {}) => {
  const query = new URLSearchParams();
  if (params.startDate) query.append("startDate", params.startDate);
  if (params.endDate) query.append("endDate", params.endDate);
  if (params.preset) query.append("preset", params.preset);
  const qs = query.toString();
  return request(`/reports/sales${qs ? `?${qs}` : ""}`);
};

export const getPurchaseReport = (params = {}) => {
  const query = new URLSearchParams();
  if (params.startDate) query.append("startDate", params.startDate);
  if (params.endDate) query.append("endDate", params.endDate);
  if (params.preset) query.append("preset", params.preset);
  const qs = query.toString();
  return request(`/reports/purchases${qs ? `?${qs}` : ""}`);
};

export const getInventoryReport = () => request("/reports/inventory");

export const getExpenseReport = (params = {}) => {
  const query = new URLSearchParams();
  if (params.startDate) query.append("startDate", params.startDate);
  if (params.endDate) query.append("endDate", params.endDate);
  if (params.preset) query.append("preset", params.preset);
  const qs = query.toString();
  return request(`/reports/expenses${qs ? `?${qs}` : ""}`);
};

export const getProfitLossReport = (params = {}) => {
  const query = new URLSearchParams();
  if (params.startDate) query.append("startDate", params.startDate);
  if (params.endDate) query.append("endDate", params.endDate);
  if (params.preset) query.append("preset", params.preset);
  const qs = query.toString();
  return request(`/reports/profit-loss${qs ? `?${qs}` : ""}`);
};

export const getReceivablesReport = (params = {}) => {
  const query = new URLSearchParams();
  if (params.startDate) query.append("startDate", params.startDate);
  if (params.endDate) query.append("endDate", params.endDate);
  if (params.preset) query.append("preset", params.preset);
  const qs = query.toString();
  return request(`/reports/receivables${qs ? `?${qs}` : ""}`);
};

export const downloadReportExport = async (reportType, format = "pdf", params = {}) => {
  const token = localStorage.getItem("erp_token");
  const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};

  const query = new URLSearchParams();
  query.append("format", format);
  if (params.startDate) query.append("startDate", params.startDate);
  if (params.endDate) query.append("endDate", params.endDate);
  if (params.preset) query.append("preset", params.preset);

  const response = await fetch(`${API_BASE_URL}/reports/${reportType}/export?${query.toString()}`, {
    headers: { ...authHeaders },
  });

  if (!response.ok) {
    const errorText = await response.text();
    let msg = "Export download failed";
    try {
      const parsed = JSON.parse(errorText);
      msg = parsed.detail || parsed.message || msg;
    } catch {
      msg = errorText || msg;
    }
    throw new Error(msg);
  }

  const blob = await response.blob();
  const ext = (format === "excel" || format === "xlsx" || format === "xls") ? "xls" : "pdf";
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `${reportType}_report_${new Date().toISOString().slice(0, 10)}.${ext}`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
};

/* =====================================================
   NOTIFICATIONS & ALERTS MANAGEMENT
===================================================== */

export const getNotifications = (params = {}) => {
  const query = new URLSearchParams();
  if (params.unreadOnly) query.append("unreadOnly", "true");
  if (params.type && params.type !== "ALL") query.append("type", params.type);
  if (params.search) query.append("search", params.search);

  const qs = query.toString();
  return request(`/notifications${qs ? `?${qs}` : ""}`);
};

export const getUnreadCount = () => {
  return request("/notifications/unread-count");
};

export const markNotificationRead = (id) => {
  return request(`/notifications/${id}/read`, {
    method: "PUT",
  });
};

export const markAllNotificationsRead = () => {
  return request("/notifications/read-all", {
    method: "PUT",
  });
};

export const deleteNotification = (id) => {
  return request(`/notifications/${id}`, {
    method: "DELETE",
  });
};

export const clearReadNotifications = () => {
  return request("/notifications/clear-read", {
    method: "DELETE",
  });
};

export const scanNotifications = () => {
  return request("/notifications/scan", {
    method: "POST",
  });
};


/* =====================================================
   DEFAULT API OBJECT
===================================================== */

const api = {
  getHealth,

  login,
  register,
  getCurrentUser,

  getCompanies,
  createCompany,

  getEmployees,
  getEmployee,
  createEmployee,
  updateEmployee,
  updateEmployeeStatus,

  getSuppliers,
  getSupplier,
  getSupplierPurchases,
  createSupplier,
  updateSupplier,
  deleteSupplier,

  getPurchases,
  getPurchase,
  getPurchaseSummary,
  createPurchase,
  updatePurchaseStatus,

  getExpenses,
  getExpense,
  getExpenseSummary,
  createExpense,
  updateExpense,
  deleteExpense,
  uploadReceipt,

  getFinancialOverview,

  getReceivablesSummary,
  getInvoiceReceivables,
  getCustomerReceivables,
  recordPayment,
  getPayments,
  getSalePayments,
  getCustomerPayments,

  getSalesReport,
  getPurchaseReport,
  getInventoryReport,
  getExpenseReport,
  getProfitLossReport,
  getReceivablesReport,
  downloadReportExport,

  getNotifications,
  getUnreadCount,
  markNotificationRead,
  markAllNotificationsRead,
  deleteNotification,
  clearReadNotifications,
  scanNotifications,

  getProducts,
  createProduct,

  getCustomers,
  createCustomer,

  getInventory,
  stockIn,
  stockOut,

  getSales,
  createSale,
  getSalesSummary,

  getAnalyticsOverview,
  getSalesTrend,
  getTopProducts,
  getBusinessSummary,
  getSalesForecast,
  getDemandPrediction,
  getInventoryRisk,
  getReorderRecommendations,
  getProfitability,
  getCustomerInsights,
  getAnomalies,
  getProductInsights,
  getAISummary,

  getAIForecast,
  refreshAIForecast,
};

export default api;