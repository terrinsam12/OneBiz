import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../../services/api";

function Register() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    companyName: "",
    ownerName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
    businessType: "Manufacturing",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function handleChange(event) {
    const { name, value } = event.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (!formData.companyName.trim()) {
      setError("Company name is required.");
      return;
    }

    if (!formData.ownerName.trim()) {
      setError("Owner name is required.");
      return;
    }

    if (!formData.email.trim()) {
      setError("Email address is required.");
      return;
    }

    if (formData.password.length < 6) {
      setError("Password must be at least 6 characters long.");
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    try {
      setLoading(true);
      const response = await register({
        company_name: formData.companyName.trim(),
        owner_name: formData.ownerName.trim(),
        email: formData.email.trim(),
        phone: formData.phone.trim(),
        password: formData.password,
        business_type: formData.businessType,
      });

      const token = response.token || response.access_token;
      if (token) {
        localStorage.setItem("erp_token", token);
      }

      if (response.user) {
        localStorage.setItem("erp_user", JSON.stringify(response.user));
      }

      if (response.company) {
        localStorage.setItem("erp_company_id", String(response.company.id));
        localStorage.setItem("erp_company_name", response.company.name);
      }

      navigate("/dashboard");
    } catch (err) {
      setError(err.message || "Registration failed. Please verify your details.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-card register-card">
        {/* Brand Header */}
        <div className="login-brand">
          <div className="brand-icon">M</div>
          <h1>Register Your Company</h1>
          <p>Join MSME Cloud ERP &bull; Start managing your enterprise operations</p>
        </div>

        {/* Registration Form */}
        <form onSubmit={handleSubmit}>
          {error && (
            <div className="error-message">
              <span>⚠️</span>
              <span>{error}</span>
            </div>
          )}

          {/* Section: Company Information */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
              gap: "16px",
            }}
          >
            <div className="form-group">
              <label htmlFor="reg-company-name">Company Name *</label>
              <input
                id="reg-company-name"
                type="text"
                name="companyName"
                value={formData.companyName}
                onChange={handleChange}
                placeholder="e.g. Acme Precision Tools Ltd"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="reg-business-type">Business Sector *</label>
              <select
                id="reg-business-type"
                name="businessType"
                value={formData.businessType}
                onChange={handleChange}
                required
              >
                <option value="Manufacturing">Manufacturing &amp; Engineering</option>
                <option value="Retail">Retail &amp; Storefront</option>
                <option value="Wholesale">Wholesale &amp; Distribution</option>
                <option value="Services">Professional Services</option>
                <option value="Technology">Technology &amp; IT</option>
                <option value="Food & Agriculture">Food, Dairy &amp; Agriculture</option>
                <option value="Logistics">Logistics &amp; Transport</option>
                <option value="Trading">Import / Export &amp; Trading</option>
                <option value="Other">Other Business</option>
              </select>
            </div>
          </div>

          {/* Section: Owner & Contact Information */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
              gap: "16px",
            }}
          >
            <div className="form-group">
              <label htmlFor="reg-owner-name">Owner / Admin Full Name *</label>
              <input
                id="reg-owner-name"
                type="text"
                name="ownerName"
                value={formData.ownerName}
                onChange={handleChange}
                placeholder="e.g. John Doe"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="reg-phone">Contact Phone Number</label>
              <input
                id="reg-phone"
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                placeholder="e.g. +91 98765 43210"
              />
            </div>
          </div>

          {/* Section: Login Email */}
          <div className="form-group">
            <label htmlFor="reg-email">Account Email Address *</label>
            <input
              id="reg-email"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="owner@company.com (will be your login email)"
              autoComplete="email"
              required
            />
          </div>

          {/* Section: Passwords */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
              gap: "16px",
            }}
          >
            <div className="form-group">
              <label htmlFor="reg-password">Password *</label>
              <div className="input-with-button">
                <input
                  id="reg-password"
                  type={showPassword ? "text" : "password"}
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Min 6 characters"
                  autoComplete="new-password"
                  required
                />
                <button
                  type="button"
                  className="input-toggle-btn"
                  onClick={() => setShowPassword(!showPassword)}
                  title={showPassword ? "Hide password" : "Show password"}
                >
                  {showPassword ? "👁️" : "👁️‍🗨️"}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="reg-confirm-password">Confirm Password *</label>
              <div className="input-with-button">
                <input
                  id="reg-confirm-password"
                  type={showPassword ? "text" : "password"}
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  placeholder="Repeat your password"
                  autoComplete="new-password"
                  required
                />
              </div>
            </div>
          </div>

          <button
            type="submit"
            className="login-button"
            disabled={loading}
          >
            {loading ? "Registering Company..." : "Register Company & Get Started →"}
          </button>
        </form>

        {/* Card Footer */}
        <div className="auth-footer">
          Already have an account?{" "}
          <Link to="/login">
            Sign In
          </Link>
        </div>

        <div className="auth-badge-trust">
          🛡️ Enterprise Multi-Tenant Isolation &bull; BCrypt Hashing &bull; JWT Security
        </div>
      </div>
    </div>
  );
}

export default Register;
