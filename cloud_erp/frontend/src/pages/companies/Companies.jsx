
import { useEffect, useState } from "react";
import {
  getCompanies,
  createCompany,
} from "../../services/api";
import { useNavigate } from "react-router-dom";

function getActiveCompanyId() {
  return (
    Number(
      localStorage.getItem("erp_company_id")
    ) || 1
  );
}

function Companies() {
  const navigate = useNavigate();

  const [companies, setCompanies] = useState([]);

  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [name, setName] = useState("");
  const [industry, setIndustry] = useState("");

  const [
    activeCompanyId,
    setActiveCompanyId,
  ] = useState(getActiveCompanyId());

  async function loadCompanies() {
    try {
      setLoading(true);
      setError("");

      const data = await getCompanies();

      setCompanies(
        Array.isArray(data)
          ? data
          : data?.companies || []
      );
    } catch (err) {
      setError(
        err.message ||
          "Unable to load companies"
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadCompanies();
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (!name.trim()) {
      setError("Company name is required.");
      return;
    }

    if (!industry.trim()) {
      setError("Industry is required.");
      return;
    }

    try {
      setSaving(true);

      await createCompany({
        name: name.trim(),
        industry: industry.trim(),
      });

      setSuccess(
        "Company created successfully."
      );

      setName("");
      setIndustry("");
      setShowForm(false);

      await loadCompanies();
    } catch (err) {
      setError(
        err.message ||
          "Unable to create company"
      );
    } finally {
      setSaving(false);
    }
  }

  function selectCompany(company) {
    const companyId =
      company.id ||
      company.company_id;

    if (!companyId) {
      setError(
        "Unable to select this company."
      );
      return;
    }

    localStorage.setItem(
      "erp_company_id",
      String(companyId)
    );

    setActiveCompanyId(
      Number(companyId)
    );

    setSuccess(
      `${company.name || "Company"} is now the active company.`
    );

    setError("");
  }

  function goToDashboard() {
    navigate("/dashboard");
  }

  const activeCompany = companies.find(
    (company) =>
      Number(
        company.id ||
          company.company_id
      ) === Number(activeCompanyId)
  );

  return (
    <div>
      {/* PAGE HEADER */}

      <div className="page-heading">
        <div>
          <h1>Companies</h1>

          <p>
            Manage business organizations
          </p>
        </div>

        <button
          className="primary-button"
          onClick={() =>
            setShowForm(
              (value) => !value
            )
          }
        >
          + Add Company
        </button>
      </div>

      {/* ACTIVE COMPANY */}

      {activeCompany && (
        <section className="dashboard-card">
          <div className="card-header">
            <div>
              <h3>Active Company</h3>

              <p>
                All products, customers,
                inventory and sales will use
                this company.
              </p>
            </div>

            <button
              className="secondary-button"
              onClick={goToDashboard}
            >
              Go to Dashboard
            </button>
          </div>

          <div className="company-active-card">
            <div>
              <strong>
                {activeCompany.name ||
                  activeCompany.company_name}
              </strong>

              <span>
                Company ID:{" "}
                {activeCompany.id ||
                  activeCompany.company_id}
              </span>

              {activeCompany.industry && (
                <span>
                  Industry:{" "}
                  {activeCompany.industry}
                </span>
              )}
            </div>

            <span className="status-badge success">
              Active
            </span>
          </div>
        </section>
      )}

      {/* SUCCESS */}

      {success && (
        <div className="success-message">
          ✓ {success}
        </div>
      )}

      {/* ERROR */}

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {/* CREATE COMPANY FORM */}

      {showForm && (
        <section className="form-card">
          <div className="card-header">
            <div>
              <h3>Add Company</h3>

              <p>
                Create a new business
                organization
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="form-grid">
              {/* COMPANY NAME */}

              <div className="form-group">
                <label>
                  Company Name
                </label>

                <input
                  type="text"
                  value={name}
                  onChange={(event) =>
                    setName(
                      event.target.value
                    )
                  }
                  placeholder="Enter company name"
                  required
                />
              </div>

              {/* INDUSTRY */}

              <div className="form-group">
                <label>
                  Industry
                </label>

                <input
                  type="text"
                  value={industry}
                  onChange={(event) =>
                    setIndustry(
                      event.target.value
                    )
                  }
                  placeholder="Example: Retail, Textile, Manufacturing"
                  required
                />
              </div>
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() => {
                  setShowForm(false);
                  setName("");
                  setIndustry("");
                }}
                disabled={saving}
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
                  : "Create Company"}
              </button>
            </div>
          </form>
        </section>
      )}

      {/* COMPANY LIST */}

      <section className="dashboard-card">
        <div className="card-header">
          <div>
            <h3>Company List</h3>

            <p>
              {companies.length} companies
            </p>
          </div>

          <button
            className="secondary-button"
            onClick={loadCompanies}
            disabled={loading}
          >
            ↻ Refresh
          </button>
        </div>

        {loading ? (
          <div className="loading-page">
            Loading companies...
          </div>
        ) : companies.length === 0 ? (
          <div className="empty-state">
            No companies found.
          </div>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Company Name</th>
                  <th>Industry</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {companies.map(
                  (company, index) => {
                    const companyId =
                      company.id ||
                      company.company_id;

                    const isActive =
                      Number(companyId) ===
                      Number(
                        activeCompanyId
                      );

                    return (
                      <tr
                        key={
                          companyId ||
                          index
                        }
                      >
                        <td>
                          {companyId ||
                            "-"}
                        </td>

                        <td>
                          <strong>
                            {company.name ||
                              company.company_name ||
                              "-"}
                          </strong>
                        </td>

                        <td>
                          {company.industry ||
                            "-"}
                        </td>

                        <td>
                          {isActive ? (
                            <span className="status-badge success">
                              Active
                            </span>
                          ) : (
                            <span className="status-badge">
                              Available
                            </span>
                          )}
                        </td>

                        <td>
                          {isActive ? (
                            <button
                              className="secondary-button"
                              onClick={
                                goToDashboard
                              }
                            >
                              Open
                            </button>
                          ) : (
                            <button
                              className="primary-button"
                              onClick={() =>
                                selectCompany(
                                  company
                                )
                              }
                            >
                              Select
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  }
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default Companies;

