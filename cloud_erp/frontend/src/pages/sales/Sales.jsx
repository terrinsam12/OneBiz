import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import {
  getSales,
  createSale,
  getProducts,
  getCustomers,
} from "../../services/api";


function getCompanyId() {

  return (
    Number(
      localStorage.getItem(
        "erp_company_id"
      )
    ) || 1
  );

}


function formatDateTime(
  value
) {

  if (!value) {
    return "-";
  }


  const date =
    new Date(value);


  if (
    Number.isNaN(
      date.getTime()
    )
  ) {

    return "-";

  }


  return date.toLocaleString(
    "en-IN",
    {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }
  );

}


function Sales() {

  const [
    sales,
    setSales
  ] = useState([]);


  const [
    products,
    setProducts
  ] = useState([]);


  const [
    customers,
    setCustomers
  ] = useState([]);


  const [
    showForm,
    setShowForm
  ] = useState(false);


  const [
    loading,
    setLoading
  ] = useState(true);


  const [
    saving,
    setSaving
  ] = useState(false);


  const [
    error,
    setError
  ] = useState("");


  const [
    success,
    setSuccess
  ] = useState("");


  const [
    form,
    setForm
  ] = useState({

    product_id: "",

    customer_id: "",

    quantity: "",

  });


  async function loadSalesData() {

    try {

      setLoading(true);

      setError("");

      const companyId =
        getCompanyId();


      const [
        salesData,
        productsData,
        customersData
      ] = await Promise.all([

        getSales(
          companyId
        ),

        getProducts(
          companyId
        ),

        getCustomers(
          companyId
        ),

      ]);


      setSales(

        Array.isArray(
          salesData
        )

          ? salesData

          : salesData?.sales || []

      );


      setProducts(

        Array.isArray(
          productsData
        )

          ? productsData

          : productsData?.products || []

      );


      setCustomers(

        Array.isArray(
          customersData
        )

          ? customersData

          : customersData?.customers || []

      );

    }

    catch (err) {

      setError(
        err.message ||
        "Unable to load sales data."
      );

    }

    finally {

      setLoading(false);

    }

  }


  useEffect(() => {

    loadSalesData();

  }, []);


  function handleChange(
    event
  ) {

    const {
      name,
      value
    } = event.target;


    setForm(
      previous => ({
        ...previous,
        [name]: value,
      })
    );

  }


  function getSelectedProduct() {

    return products.find(
      product =>
        Number(product.id) ===
        Number(form.product_id)
    );

  }


  function openForm() {

    setError("");

    setSuccess("");

    setForm({

      product_id: "",

      customer_id: "",

      quantity: "",

    });

    setShowForm(true);

  }


  function closeForm() {

    if (saving) {
      return;
    }

    setShowForm(false);

  }


  async function handleSubmit(
    event
  ) {

    event.preventDefault();

    setError("");

    setSuccess("");


    if (!form.product_id) {

      setError(
        "Please select a product."
      );

      return;

    }


    if (
      form.quantity === "" ||
      Number(form.quantity) <= 0
    ) {

      setError(
        "Quantity must be greater than 0."
      );

      return;

    }


    const product =
      getSelectedProduct();


    if (!product) {

      setError(
        "Selected product was not found."
      );

      return;

    }


    const stock =
      Number(
        product.quantity || 0
      );


    const quantity =
      Number(
        form.quantity
      );


    if (
      quantity > stock
    ) {

      setError(
        `Insufficient stock. Available stock: ${stock}.`
      );

      return;

    }


    try {

      setSaving(true);


      const companyId =
        getCompanyId();


      await createSale({

        company_id:
          companyId,

        product_id:
          Number(
            form.product_id
          ),

        customer_id:
          form.customer_id
            ? Number(
                form.customer_id
              )
            : null,

        quantity:
          quantity,

      });


      setSuccess(
        "Sale created successfully."
      );


      setForm({

        product_id: "",

        customer_id: "",

        quantity: "",

      });


      setShowForm(false);


      await loadSalesData();

    }

    catch (err) {

      setError(
        err.message ||
        "Unable to create sale."
      );

    }

    finally {

      setSaving(false);

    }

  }


  return (

    <div>

      <div className="page-heading">

        <div>

          <h1>
            Sales
          </h1>

          <p>
            Create and manage your sales transactions
          </p>

        </div>


        <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
          <Link
            to="/receivables"
            className="secondary-button"
            style={{ textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "6px" }}
          >
            💳 Customer Receivables
          </Link>

          <button
            className="primary-button"
            onClick={openForm}
          >
            + Create Sale
          </button>
        </div>

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

              <h3>
                Create Sale
              </h3>

              <p>
                Record a new customer purchase
              </p>

            </div>

          </div>


          <form
            onSubmit={handleSubmit}
          >

            <div className="form-grid">


              <div className="form-group">

                <label>
                  Product
                </label>


                <select
                  name="product_id"
                  value={
                    form.product_id
                  }
                  onChange={
                    handleChange
                  }
                  required
                >

                  <option value="">
                    Select product
                  </option>


                  {products.map(
                    product => {

                      const stock =
                        Number(
                          product.quantity || 0
                        );


                      return (

                        <option
                          key={
                            product.id
                          }
                          value={
                            product.id
                          }
                          disabled={
                            stock <= 0
                          }
                        >

                          {product.name}
                          {" — Stock: "}
                          {stock}

                        </option>

                      );

                    }
                  )}

                </select>

              </div>


              <div className="form-group">

                <label>
                  Customer
                </label>


                <select
                  name="customer_id"
                  value={
                    form.customer_id
                  }
                  onChange={
                    handleChange
                  }
                >

                  <option value="">
                    Walk-in Customer
                  </option>


                  {customers.map(
                    customer => (

                      <option
                        key={
                          customer.id
                        }
                        value={
                          customer.id
                        }
                      >

                        {customer.name}

                        {customer.phone
                          ? ` — ${customer.phone}`
                          : ""}

                      </option>

                    )
                  )}

                </select>

              </div>


              <div className="form-group">

                <label>
                  Quantity
                </label>


                <input
                  type="number"
                  name="quantity"
                  min="1"
                  value={
                    form.quantity
                  }
                  onChange={
                    handleChange
                  }
                  placeholder="Enter quantity"
                  required
                />

              </div>

            </div>


            {form.product_id && (

              <div className="sale-preview">

                {(() => {

                  const product =
                    getSelectedProduct();


                  if (!product) {
                    return null;
                  }


                  const price =
                    Number(
                      product.price || 0
                    );


                  const stock =
                    Number(
                      product.quantity || 0
                    );


                  const quantity =
                    Number(
                      form.quantity || 0
                    );


                  const total =
                    price * quantity;


                  return (

                    <div>

                      <strong>
                        {product.name}
                      </strong>


                      <div>

                        Unit Price: ₹
                        {price.toLocaleString(
                          "en-IN"
                        )}

                      </div>


                      <div>

                        Available Stock:{" "}
                        {stock}

                      </div>


                      {quantity > 0 && (

                        <div>

                          Estimated Total: ₹
                          {total.toLocaleString(
                            "en-IN"
                          )}

                        </div>

                      )}

                    </div>

                  );

                })()}

              </div>

            )}


            <div className="form-actions">

              <button
                type="button"
                className="secondary-button"
                onClick={closeForm}
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
                  : "Create Sale"}

              </button>

            </div>

          </form>

        </section>

      )}


      <section className="dashboard-card">

        <div className="card-header">

          <div>

            <h3>
              Sales History
            </h3>

            <p>
              {sales.length} transactions
            </p>

          </div>


          <button
            className="secondary-button"
            onClick={
              loadSalesData
            }
            disabled={loading}
          >
            ↻ Refresh
          </button>

        </div>


        {loading ? (

          <div className="loading-page">
            Loading sales...
          </div>

        ) : sales.length === 0 ? (

          <div className="empty-state">
            No sales transactions yet.
          </div>

        ) : (

          <div className="table-wrapper">

            <table>

              <thead>

                <tr>

                  <th>
                    Sale ID
                  </th>

                  <th>
                    Date / Time
                  </th>

                  <th>
                    Customer
                  </th>

                  <th>
                    Product
                  </th>

                  <th>
                    Quantity
                  </th>

                  <th>
                    Unit Price
                  </th>

                  <th>
                    Total
                  </th>

                  <th>
                    Payment Status
                  </th>

                  <th>
                    Profit
                  </th>

                </tr>

              </thead>


              <tbody>

                {sales.map(
                  (sale, index) => (

                    <tr
                      key={
                        sale.sale_id ||
                        sale.id ||
                        index
                      }
                    >

                      <td>

                        #
                        {sale.sale_id ||
                          sale.id ||
                          "-"}

                      </td>


                      <td>

                        {formatDateTime(
                          sale.created_at
                        )}

                      </td>


                      <td>

                        <strong>
                          {
                            sale.customer ||
                            "Walk-in Customer"
                          }
                        </strong>

                      </td>


                      <td>

                        {
                          sale.product ||
                          "Unknown Product"
                        }

                      </td>


                      <td>

                        {Number(
                          sale.quantity || 0
                        ).toLocaleString(
                          "en-IN"
                        )}

                      </td>


                      <td className="amount">

                        ₹
                        {Number(
                          sale.unit_price || 0
                        ).toLocaleString(
                          "en-IN"
                        )}

                      </td>


                      <td className="amount">

                        <strong>

                          ₹
                          {Number(
                            sale.total_amount || 0
                          ).toLocaleString(
                            "en-IN"
                          )}

                        </strong>

                      </td>

                      <td style={{ textAlign: "center" }}>
                        <span
                          style={{
                            display: "inline-block",
                            padding: "2px 8px",
                            borderRadius: "10px",
                            fontSize: "11px",
                            fontWeight: 600,
                            background:
                              sale.payment_status === "PAID"
                                ? "#dcfce7"
                                : sale.payment_status === "PARTIALLY_PAID"
                                ? "#fef3c7"
                                : "#fee2e2",
                            color:
                              sale.payment_status === "PAID"
                                ? "#15803d"
                                : sale.payment_status === "PARTIALLY_PAID"
                                ? "#b45309"
                                : "#b91c1c",
                          }}
                        >
                          {sale.payment_status || "UNPAID"}
                        </span>
                        {sale.payment_status !== "PAID" && Number(sale.balance_amount || 0) > 0 && (
                          <div style={{ fontSize: "10px", color: "#64748b", marginTop: "2px" }}>
                            Bal: ₹{Number(sale.balance_amount || 0).toLocaleString("en-IN")}
                          </div>
                        )}
                      </td>


                      <td className="amount">

                        ₹
                        {Number(
                          sale.estimated_profit || 0
                        ).toLocaleString(
                          "en-IN"
                        )}

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


export default Sales;