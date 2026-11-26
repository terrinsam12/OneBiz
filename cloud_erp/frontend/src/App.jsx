import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import Layout from "./components/layout/Layout";
import ProtectedRoute from "./components/auth/ProtectedRoute";

import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import Dashboard from "./pages/dashboard/Dashboard";
import Products from "./pages/products/Products";
import Inventory from "./pages/inventory/Inventory";
import Customers from "./pages/customers/Customers";
import Suppliers from "./pages/suppliers/Suppliers";
import Purchases from "./pages/purchases/Purchases";
import Sales from "./pages/sales/Sales";
import Receivables from "./pages/receivables/Receivables";
import Expenses from "./pages/expenses/Expenses";
import Companies from "./pages/companies/Companies";
import Employees from "./pages/employees/Employees";
import Analytics from "./pages/analytics/Analytics";
import Finance from "./pages/finance/Finance";
import Forecast from "./pages/forecast/Forecast";
import Reports from "./pages/reports/Reports";
import Notifications from "./pages/notifications/Notifications";

import "./App.css";

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* PUBLIC */}
        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />

        {/* PROTECTED APPLICATION */}
        <Route element={<ProtectedRoute />}>

          <Route element={<Layout />}>

            <Route
              path="/dashboard"
              element={<Dashboard />}
            />

            <Route
              path="/products"
              element={<Products />}
            />

            <Route
              path="/inventory"
              element={<Inventory />}
            />

            <Route
              path="/purchases"
              element={<Purchases />}
            />

            <Route
              path="/customers"
              element={<Customers />}
            />

            <Route
              path="/suppliers"
              element={<Suppliers />}
            />

            <Route
              path="/sales"
              element={<Sales />}
            />

            <Route
              path="/receivables"
              element={<Receivables />}
            />

            <Route
              path="/expenses"
              element={<Expenses />}
            />

            <Route
              path="/companies"
              element={<Companies />}
            />

            <Route
              path="/employees"
              element={<Employees />}
            />

            <Route
              path="/analytics"
              element={<Analytics />}
            />

            <Route
              path="/finance"
              element={<Finance />}
            />

            <Route
              path="/forecast"
              element={<Forecast />}
            />

            <Route
              path="/reports"
              element={<Reports />}
            />

            <Route
              path="/notifications"
              element={<Notifications />}
            />

          </Route>

        </Route>

        {/* ROOT */}
        <Route
          path="/"
          element={
            <Navigate
              to="/dashboard"
              replace
            />
          }
        />

        {/* UNKNOWN ROUTES */}
        <Route
          path="*"
          element={
            <Navigate
              to="/dashboard"
              replace
            />
          }
        />

      </Routes>
    </BrowserRouter>
  );
}

export default App;