import { useEffect, useState } from "react";

import {
  getProducts,
  createProduct,
} from "../../services/api";


/* =========================================================
   ACTIVE COMPANY
========================================================= */

function getCompanyId() {

  return (
    Number(
      localStorage.getItem("erp_company_id")
    ) || 1
  );

}


/* =========================================================
   PRODUCTS PAGE
========================================================= */

function Products() {

  const [products, setProducts] = useState([]);

  const [showForm, setShowForm] = useState(false);

  const [loading, setLoading] = useState(true);

  const [saving, setSaving] = useState(false);

  const [error, setError] = useState("");

  const [success, setSuccess] = useState("");


  /* =======================================================
     FORM
  ======================================================= */

  const [form, setForm] = useState({

    company_id: getCompanyId(),

    name: "",

    price: "",

    cost_price: "",

    quantity: "",

    reorder_level: 5,

  });


  /* =======================================================
     LOAD PRODUCTS
  ======================================================= */

  async function loadProducts() {

    try {

      setLoading(true);

      setError("");

      const companyId = getCompanyId();

      const data =
        await getProducts(companyId);


      setProducts(

        Array.isArray(data)

          ? data

          : data?.products || []

      );

    } catch (err) {

      setError(
        err.message ||
        "Unable to load products."
      );

    } finally {

      setLoading(false);

    }

  }


  /* =======================================================
     INITIAL LOAD + COMPANY CHANGE
  ======================================================= */

  useEffect(() => {

    loadProducts();


    function handleCompanyChanged() {

      setSuccess("");

      setError("");

      setShowForm(false);


      setForm({

        company_id: getCompanyId(),

        name: "",

        price: "",

        cost_price: "",

        quantity: "",

        reorder_level: 5,

      });


      loadProducts();

    }


    window.addEventListener(
      "companyChanged",
      handleCompanyChanged
    );


    return () => {

      window.removeEventListener(
        "companyChanged",
        handleCompanyChanged
      );

    };

  }, []);


  /* =======================================================
     FORM INPUT CHANGE
  ======================================================= */

  function handleChange(event) {

    const {
      name,
      value,
    } = event.target;


    setForm((previous) => ({

      ...previous,

      [name]: value,

    }));

  }


  /* =======================================================
     CREATE PRODUCT
  ======================================================= */

  async function handleSubmit(event) {

    event.preventDefault();

    setError("");

    setSuccess("");


    /* -----------------------------------------------
       PRODUCT NAME
    ----------------------------------------------- */

    if (!form.name.trim()) {

      setError(
        "Product name is required."
      );

      return;

    }


    /* -----------------------------------------------
       SELLING PRICE
    ----------------------------------------------- */

    if (

      form.price === "" ||

      Number(form.price) < 0

    ) {

      setError(
        "Enter a valid selling price."
      );

      return;

    }


    /* -----------------------------------------------
       COST PRICE
    ----------------------------------------------- */

    if (

      form.cost_price === "" ||

      Number(form.cost_price) < 0

    ) {

      setError(
        "Enter a valid cost price."
      );

      return;

    }


    /* -----------------------------------------------
       COST PRICE SHOULD NOT EXCEED SELLING PRICE
    ----------------------------------------------- */

    if (

      Number(form.cost_price) >

      Number(form.price)

    ) {

      setError(
        "Cost price should not be greater than selling price."
      );

      return;

    }


    /* -----------------------------------------------
       INITIAL STOCK
    ----------------------------------------------- */

    if (

      form.quantity === "" ||

      Number(form.quantity) < 0

    ) {

      setError(
        "Enter a valid initial stock quantity."
      );

      return;

    }


    /* -----------------------------------------------
       REORDER LEVEL
    ----------------------------------------------- */

    if (

      form.reorder_level === "" ||

      Number(form.reorder_level) < 0

    ) {

      setError(
        "Enter a valid reorder level."
      );

      return;

    }


    try {

      setSaving(true);


      const companyId =
        getCompanyId();


      /* ---------------------------------------------
         CREATE PRODUCT
      --------------------------------------------- */

      await createProduct({

        company_id: companyId,

        name: form.name.trim(),

        price: Number(form.price),

        cost_price:
          Number(form.cost_price),

        quantity:
          Number(form.quantity),

        reorder_level:
          Number(form.reorder_level),

      });


      /* ---------------------------------------------
         SUCCESS
      --------------------------------------------- */

      setSuccess(
        "Product created successfully."
      );


      /* ---------------------------------------------
         RESET
      --------------------------------------------- */

      setForm({

        company_id: companyId,

        name: "",

        price: "",

        cost_price: "",

        quantity: "",

        reorder_level: 5,

      });


      setShowForm(false);


      /* ---------------------------------------------
         RELOAD
      --------------------------------------------- */

      await loadProducts();

    } catch (err) {

      setError(
        err.message ||
        "Unable to create product."
      );

    } finally {

      setSaving(false);

    }

  }


  /* =======================================================
     RENDER
  ======================================================= */

  return (

    <div>

      {/* =================================================
          PAGE HEADER
      ================================================= */}

      <div className="page-heading">

        <div>

          <h1>
            Products
          </h1>

          <p>
            Manage your products and pricing
          </p>

        </div>


        <button
          className="primary-button"
          onClick={() => {

            setShowForm(
              (value) => !value
            );

            setError("");

            setSuccess("");

          }}
        >
          + Add Product
        </button>

      </div>


      {/* =================================================
          SUCCESS
      ================================================= */}

      {success && (

        <div className="success-message">

          ✓ {success}

        </div>

      )}


      {/* =================================================
          ERROR
      ================================================= */}

      {error && (

        <div className="error-message">

          {error}

        </div>

      )}


      {/* =================================================
          CREATE PRODUCT FORM
      ================================================= */}

      {showForm && (

        <section className="form-card">

          <div className="card-header">

            <div>

              <h3>
                Add Product
              </h3>

              <p>
                Create a new product
              </p>

            </div>

          </div>


          <form
            onSubmit={handleSubmit}
          >

            <div className="form-grid">


              {/* PRODUCT NAME */}

              <div className="form-group">

                <label>
                  Product Name
                </label>

                <input

                  type="text"

                  name="name"

                  value={form.name}

                  onChange={handleChange}

                  placeholder="Enter product name"

                  required

                />

              </div>


              {/* SELLING PRICE */}

              <div className="form-group">

                <label>
                  Selling Price
                </label>

                <input

                  type="number"

                  name="price"

                  min="0"

                  step="0.01"

                  value={form.price}

                  onChange={handleChange}

                  placeholder="Enter selling price"

                  required

                />

              </div>


              {/* COST PRICE */}

              <div className="form-group">

                <label>
                  Cost Price
                </label>

                <input

                  type="number"

                  name="cost_price"

                  min="0"

                  step="0.01"

                  value={form.cost_price}

                  onChange={handleChange}

                  placeholder="Enter cost price"

                  required

                />

              </div>


              {/* INITIAL STOCK */}

              <div className="form-group">

                <label>
                  Initial Stock
                </label>

                <input

                  type="number"

                  name="quantity"

                  min="0"

                  value={form.quantity}

                  onChange={handleChange}

                  placeholder="Enter stock quantity"

                  required

                />

              </div>


              {/* REORDER LEVEL */}

              <div className="form-group">

                <label>
                  Reorder Level
                </label>

                <input

                  type="number"

                  name="reorder_level"

                  min="0"

                  value={form.reorder_level}

                  onChange={handleChange}

                  placeholder="Example: 5"

                  required

                />

              </div>


            </div>


            {/* FORM ACTIONS */}

            <div className="form-actions">

              <button

                type="button"

                className="secondary-button"

                onClick={() => {

                  setShowForm(false);

                  setError("");

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

                  : "Create Product"}

              </button>

            </div>


          </form>

        </section>

      )}


      {/* =================================================
          PRODUCT LIST
      ================================================= */}

      <section className="dashboard-card">

        <div className="card-header">

          <div>

            <h3>
              Product List
            </h3>

            <p>

              {products.length}{" "}

              {products.length === 1

                ? "product"

                : "products"}

            </p>

          </div>


          <button

            className="secondary-button"

            onClick={loadProducts}

            disabled={loading}

          >

            ↻ Refresh

          </button>

        </div>


        {/* =================================================
            LOADING
        ================================================= */}

        {loading ? (

          <div className="loading-page">

            Loading products...

          </div>


        ) : products.length === 0 ? (

          <div className="empty-state">

            No products found.

          </div>


        ) : (

          <div className="table-wrapper">

            <table>

              <thead>

                <tr>

                  <th>ID</th>

                  <th>Product</th>

                  <th>Selling Price</th>

                  <th>Cost Price</th>

                  <th>Stock</th>

                  <th>Reorder Level</th>

                  <th>Status</th>

                </tr>

              </thead>


              <tbody>

                {products.map(
                  (product) => {

                    const stock =
                      Number(
                        product.quantity ?? 0
                      );


                    const reorderLevel =
                      Number(
                        product.reorder_level ?? 5
                      );


                    return (

                      <tr
                        key={product.id}
                      >

                        <td>
                          {product.id}
                        </td>


                        <td>

                          <strong>
                            {product.name}
                          </strong>

                        </td>


                        <td className="amount">

                          ₹
                          {Number(
                            product.price || 0
                          ).toLocaleString(
                            "en-IN"
                          )}

                        </td>


                        <td className="amount">

                          ₹
                          {Number(
                            product.cost_price || 0
                          ).toLocaleString(
                            "en-IN"
                          )}

                        </td>


                        <td>
                          {stock}
                        </td>


                        <td>
                          {reorderLevel}
                        </td>


                        <td>

                          {stock <= reorderLevel ? (

                            <span className="status-badge warning">

                              Low Stock

                            </span>

                          ) : (

                            <span className="status-badge success">

                              In Stock

                            </span>

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


export default Products;