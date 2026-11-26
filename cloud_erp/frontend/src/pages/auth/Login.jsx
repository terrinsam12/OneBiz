import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../../services/api";

function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (!email.trim() || !password.trim()) {
      setError("Please enter both your email address and password.");
      return;
    }

    try {
      setLoading(true);
      const response = await login({
        email: email.trim(),
        password: password.trim(),
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
      setError(err.message || "Invalid email or password. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  function fillDemoCredentials() {
    setEmail("admin@example.com");
    setPassword("Admin@123");
    setError("");
  }

  return (
    <div className="login-page">
      <div className="login-card">
        {/* Brand Header */}
        <div className="login-brand">
          <div className="brand-icon">M</div>
          <h1>MSME Cloud ERP</h1>
          <p>Sign in to access your business operations &amp; finance</p>
        </div>

        {/* Demo Credentials Quick Pill */}
        <div className="demo-credentials-card">
          <div className="demo-credentials-text">
            <strong>Default Demo Admin</strong>
            <span>admin@example.com &bull; Admin@123</span>
          </div>
          <button
            type="button"
            className="demo-autofill-btn"
            onClick={fillDemoCredentials}
            title="Auto-fill credentials"
          >
            ⚡ Auto-Fill
          </button>
        </div>

        {/* Login Form */}
        <form onSubmit={handleSubmit}>
          {error && (
            <div className="error-message">
              <span>⚠️</span>
              <span>{error}</span>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="login-email">
              <span>Email Address</span>
            </label>
            <input
              id="login-email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="e.g. admin@example.com"
              autoComplete="email"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="login-password">
              <span>Password</span>
            </label>
            <div className="input-with-button">
              <input
                id="login-password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="••••••••"
                autoComplete="current-password"
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

          <button
            type="submit"
            className="login-button"
            disabled={loading}
          >
            {loading ? "Authenticating..." : "Sign In to ERP →"}
          </button>
        </form>

        {/* Card Footer */}
        <div className="auth-footer">
          Need to register your business?{" "}
          <Link to="/register">
            Register Company
          </Link>
        </div>

        <div className="auth-badge-trust">
          🔒 Secure Multi-Tenant Enterprise Cloud
        </div>
      </div>
    </div>
  );
}

export default Login;
