import { useEffect, useState } from "react";

import {
  getInventory,
  stockIn,
  stockOut,
  getProducts,
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


function Inventory() {

  const [
    inventory,
    setInventory
  ] = useState([]);


  const [
    products,
    setProducts
  ] = useState([]);


  const [
    showStockForm,
    setShowStockForm
  ] = useState(false);


  const [
    operation,
    setOperation
  ] = useState("in");


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

    quantity: "",

  });


  async function loadInventory() {

    try {

      setLoading(true);

      setError("");

      const companyId =
        getCompanyId();


      const [
        inventoryData,
        productsData
      ] = await Promise.all([

        getInventory(
          companyId
        ),

        getProducts(
          companyId
        ),

      ]);


      setInventory(

        Array.isArray(
          inventoryData
        )

          ? inventoryData

          : inventoryData?.inventory || []

      );


      setProducts(

        Array.isArray(
          productsData
        )

          ? productsData

          : productsData?.products || []

      );

    }

    catch (err) {

      setError(
        err.message ||
        "Unable to load inventory."
      );

    }

    finally {

      setLoading(false);

    }

  }


  useEffect(() => {

    loadInventory();

  }, []);


  function openStockForm(
    type
  ) {

    setOperation(type);

    setForm({

      product_id: "",

      quantity: "",

    });

    setError("");

    setSuccess("");

    setShowStockForm(true);

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


    const selectedProduct =
      products.find(
        product =>
          Number(product.id) ===
          Number(form.product_id)
      );


    if (!selectedProduct) {

      setError(
        "Selected product was not found."
      );

      return;

    }


    const currentStock =
      Number(
        selectedProduct.quantity || 0
      );


    if (
      operation === "out" &&
      Number(form.quantity) >
        currentStock
    ) {

      setError(
        `Insufficient stock. Available stock: ${currentStock}.`
      );

      return;

    }


    try {

      setSaving(true);


      const companyId =
        getCompanyId();


      const payload = {

        company_id:
          companyId,

        product_id:
          Number(form.product_id),

        quantity:
          Number(form.quantity),

      };


      if (
        operation === "in"
      ) {

        await stockIn(
          payload
        );

        setSuccess(
          "Stock added successfully."
        );

      }

      else {

        await stockOut(
          payload
        );

        setSuccess(
          "Stock removed successfully."
        );

      }


      setForm({

        product_id: "",

        quantity: "",

      });


      setShowStockForm(false);


      await loadInventory();

    }

    catch (err) {

      setError(
        err.message ||
        "Unable to update stock."
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
            Inventory
          </h1>

          <p>
            Monitor and manage your stock
          </p>

        </div>


        <div className="button-group">

          <button
            className="primary-button"
            onClick={() =>
              openStockForm("in")
            }
          >
            + Stock In
          </button>


          <button
            className="danger-button"
            onClick={() =>
              openStockForm("out")
            }
          >
            − Stock Out
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


      {showStockForm && (

        <section className="form-card">

          <div className="card-header">

            <div>

              <h3>

                {operation === "in"
                  ? "Add Stock"
                  : "Remove Stock"}

              </h3>

              <p>

                {operation === "in"
                  ? "Increase product inventory"
                  : "Decrease product inventory"}

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
                  value={
                    form.product_id
                  }
                  onChange={
                    event =>
                      setForm(
                        previous => ({
                          ...previous,
                          product_id:
                            event.target.value
                        })
                      )
                  }
                  required
                >

                  <option value="">
                    Select product
                  </option>


                  {products.map(
                    product => (

                      <option
                        key={product.id}
                        value={product.id}
                      >

                        {product.name}
                        {" — Stock: "}
                        {product.quantity}

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
                  min="1"
                  value={
                    form.quantity
                  }
                  onChange={
                    event =>
                      setForm(
                        previous => ({
                          ...previous,
                          quantity:
                            event.target.value
                        })
                      )
                  }
                  placeholder="Enter quantity"
                  required
                />

              </div>

            </div>


            <div className="form-actions">

              <button
                type="button"
                className="secondary-button"
                onClick={() =>
                  setShowStockForm(false)
                }
                disabled={saving}
              >
                Cancel
              </button>


              <button
                type="submit"
                className={
                  operation === "in"
                    ? "primary-button"
                    : "danger-button"
                }
                disabled={saving}
              >

                {saving
                  ? "Processing..."
                  : operation === "in"
                  ? "Add Stock"
                  : "Remove Stock"}

              </button>

            </div>

          </form>

        </section>

      )}


      <section className="dashboard-card">

        <div className="card-header">

          <div>

            <h3>
              Current Inventory
            </h3>

            <p>
              Live stock levels
            </p>

          </div>


          <button
            className="secondary-button"
            onClick={loadInventory}
            disabled={loading}
          >
            ↻ Refresh
          </button>

        </div>


        {loading ? (

          <div className="loading-page">
            Loading inventory...
          </div>

        ) : inventory.length === 0 ? (

          <div className="empty-state">
            No inventory records found.
          </div>

        ) : (

          <div className="table-wrapper">

            <table>

              <thead>

                <tr>

                  <th>
                    Product
                  </th>

                  <th>
                    Current Stock
                  </th>

                  <th>
                    Reorder Level
                  </th>

                  <th>
                    Stock Value
                  </th>

                  <th>
                    Status
                  </th>

                </tr>

              </thead>


              <tbody>

                {inventory.map(
                  item => {

                    const stock =
                      Number(
                        item.quantity || 0
                      );


                    const reorder =
                      Number(
                        item.reorder_level ?? 5
                      );


                    return (

                      <tr
                        key={
                          item.product_id
                        }
                      >

                        <td>

                          <strong>
                            {item.product}
                          </strong>

                        </td>


                        <td>
                          {stock}
                        </td>


                        <td>
                          {reorder}
                        </td>


                        <td className="amount">

                          ₹
                          {Number(
                            item.stock_value || 0
                          ).toLocaleString(
                            "en-IN"
                          )}

                        </td>


                        <td>

                          {stock <= reorder ? (

                            <span className="status-badge warning">
                              Low Stock
                            </span>

                          ) : (

                            <span className="status-badge success">
                              Healthy
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


export default Inventory;