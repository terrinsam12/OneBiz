import { useEffect, useState } from "react";
import {
  getCustomers,
  createCustomer,
} from "../../services/api";

function getCompanyId() {
  return (
    Number(localStorage.getItem("erp_company_id")) || 1
  );
}

function Customers() {
  const [customers, setCustomers] = useState([]);

  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [form, setForm] = useState({
    name: "",
    phone: "",
    email: "",
    address: "",
  });

  async function loadCustomers() {
    try {
      setLoading(true);
      setError("");

      const companyId = getCompanyId();

      const data = await getCustomers(companyId);

      setCustomers(
        Array.isArray(data)
          ? data
          : data?.customers || []
      );
    } catch (err) {
      setError(
        err.message || "Unable to load customers."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadCustomers();
  }, []);

  function handleChange(event) {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (!form.name.trim()) {
      setError("Customer name is required.");
      return;
    }

    try {
      setSaving(true);

      const companyId = getCompanyId();

      await createCustomer({
        company_id: companyId,
        name: form.name.trim(),
        phone: form.phone.trim(),
        email: form.email.trim(),
        address: form.address.trim(),
      });

      setSuccess(
        "Customer created successfully."
      );

      setForm({
        name: "",
        phone: "",
        email: "",
        address: "",
      });

      setShowForm(false);

      await loadCustomers();
    } catch (err) {
      setError(
        err.message || "Unable to create customer."
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <div>
      <div className="page-heading">
        <div>
          <h1>Customers</h1>
          <p>Manage your customer information</p>
        </div>

        <button
          className="primary-button"
          onClick={() => {
            setError("");
            setSuccess("");
            setShowForm((value) => !value);
          }}
        >
          + Add Customer
        </button>
      </div>

      {success && (
        <div className="success-message">
          ✓ {success}
        </div>
      )}

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {showForm && (
        <section className="form-card">
          <div className="card-header">
            <div>
              <h3>Add Customer</h3>
              <p>Create a new customer</p>
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="form-grid">
              <div className="form-group">
                <label>Customer Name</label>

                <input
                  type="text"
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  placeholder="Enter customer name"
                  required
                />
              </div>

              <div className="form-group">
                <label>Phone</label>

                <input
                  type="tel"
                  name="phone"
                  value={form.phone}
                  onChange={handleChange}
                  placeholder="Enter phone number"
                />
              </div>

              <div className="form-group">
                <label>Email</label>

                <input
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="Enter email address"
                />
              </div>

              <div className="form-group">
                <label>Address</label>

                <input
                  type="text"
                  name="address"
                  value={form.address}
                  onChange={handleChange}
                  placeholder="Enter address"
                />
              </div>
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() => setShowForm(false)}
              >
                Cancel
              </button>

              <button
                type="submit"
                className="primary-button"
                disabled={saving}
              >
                {saving
                  ? "Creating..."
                  : "Create Customer"}
              </button>
            </div>
          </form>
        </section>
      )}

      <section className="dashboard-card">
        <div className="card-header">
          <div>
            <h3>Customer List</h3>
            <p>
              {customers.length} customers
            </p>
          </div>

          <button
            className="secondary-button"
            onClick={loadCustomers}
            disabled={loading}
          >
            ↻ Refresh
          </button>
        </div>

        {loading ? (
          <div className="loading-page">
            Loading customers...
          </div>
        ) : customers.length === 0 ? (
          <div className="empty-state">
            No customers found.
          </div>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Customer</th>
                  <th>Phone</th>
                  <th>Email</th>
                  <th>Address</th>
                </tr>
              </thead>

              <tbody>
                {customers.map(
                  (customer, index) => (
                    <tr
                      key={
                        customer.id ||
                        customer.customer_id ||
                        index
                      }
                    >
                      <td>
                        {customer.id ||
                          customer.customer_id ||
                          "-"}
                      </td>

                      <td>
                        <strong>
                          {customer.name ||
                            customer.customer_name ||
                            "-"}
                        </strong>
                      </td>

                      <td>
                        {customer.phone || "-"}
                      </td>

                      <td>
                        {customer.email || "-"}
                      </td>

                      <td>
                        {customer.address || "-"}
                      </td>
                    </tr>
                  )
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default Customers;