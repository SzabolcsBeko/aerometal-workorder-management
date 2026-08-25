import { useEffect, useState } from "react";
import {
  accessRightApi,
  assignmentApi,
  componentApi,
  employeeApi,
} from "../api/api";
export default function OverviewPage() {
  const emptyForm = {
    employeeId: "",
    componentId: "",
    workOrderNumber: "",
    workOrderDate: "",
    accessRightId: "",
  };
  const [employees, setEmployees] = useState([]),
    [components, setComponents] = useState([]),
    [accessRights, setAccessRights] = useState([]),
    [rows, setRows] = useState([]),
    [form, setForm] = useState(emptyForm),
    [editingId, setEditingId] = useState(null),
    [error, setError] = useState("");
  const load = async () => {
    const [e, c, r, a] = await Promise.all([
      employeeApi.list(),
      componentApi.list(),
      accessRightApi.list(),
      assignmentApi.list(),
    ]);
    setEmployees(e.data);
    setComponents(c.data);
    setAccessRights(r.data);
    setRows(a.data);
  };
  useEffect(() => {
    load();
  }, []);
  const save = async () => {
    setError("");
    if (
      !form.employeeId ||
      !form.componentId ||
      !form.accessRightId ||
      !form.workOrderNumber ||
      !form.workOrderDate
    ) {
      setError(
        "Please select employee, component, right, workorder number and date.",
      );
      return;
    }
    try {
      const payload = {
        employeeId: Number(form.employeeId),
        componentId: Number(form.componentId),
        workOrderNumber: form.workOrderNumber.trim(),
        workOrderDate: form.workOrderDate,
        accessRightId: Number(form.accessRightId),
      };
      if (editingId) {
        await assignmentApi.update(editingId, payload);
      } else {
        await assignmentApi.create(payload);
      }
      setForm(emptyForm);
      setEditingId(null);
      await load();
    } catch (err) {
      setError(err.response?.data?.message || "Save failed");
    }
  };
  const edit = (assignment) => {
    setError("");
    setEditingId(assignment.id);
    setForm({
      employeeId: String(assignment.employeeId),
      componentId: String(assignment.componentId),
      workOrderNumber: assignment.workOrderNumber ?? "",
      workOrderDate: assignment.workOrderDate ?? "",
      accessRightId: String(assignment.accessRightId),
    });
  };
  const cancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
    setError("");
  };
  const remove = async (id) => {
    try {
      setError("");
      await assignmentApi.remove(id);
      if (editingId === id) cancelEdit();
      await load();
    } catch (err) {
      setError(err.response?.data?.message || "Delete failed");
    }
  };
  return (
    <section>
      <div className="title-row">
        <h2>Access Overview</h2>
      </div>
      <p>Select one value from each list and save the assignment.</p>
      {error && <div className="error">{error}</div>}
      <div className="combo-grid">
        <label>
          Employee
          <select
            value={form.employeeId}
            onChange={(e) => setForm({ ...form, employeeId: e.target.value })}
          >
            <option value="">Choose employee...</option>
            {employees.map((x) => (
              <option key={x.id} value={x.id}>
                {x.firstName + "  " + x.lastName}
              </option>
            ))}
          </select>
        </label>
        <label>
          Component
          <select
            value={form.componentId}
            onChange={(e) => setForm({ ...form, componentId: e.target.value })}
          >
            <option value="">Choose component...</option>
            {components.map((x) => (
              <option key={x.id} value={x.id}>
                {x.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Right
          <select
            value={form.accessRightId}
            onChange={(e) =>
              setForm({ ...form, accessRightId: e.target.value })
            }
          >
            <option value="">Choose right...</option>
            {accessRights.map((x) => (
              <option key={x.id} value={x.id}>
                {x.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Workorder Number
          <input
            type="text"
            maxLength={120}
            required
            readOnly={editingId !== null}
            className={editingId !== null ? "readonly-field" : ""}
            value={form.workOrderNumber}
            onChange={(e) =>
              setForm({ ...form, workOrderNumber: e.target.value })
            }
          ></input>
        </label>
        <label>
          Workorder Date
          <input
            type="date"
            required
            value={form.workOrderDate}
            onChange={(e) =>
              setForm({ ...form, workOrderDate: e.target.value })
            }
          ></input>
        </label>
        <div className="assignment-form-actions">
          <button type="button" className="small" onClick={save}>
            {editingId ? "Update" : "Save"}
          </button>
          {editingId && (
            <button
              type="button"
              className="small secondary"
              onClick={cancelEdit}
            >
              Cancel
            </button>
          )}
        </div>
      </div>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Employee</th>
            <th>Component</th>
            <th>Workorder Number</th>
            <th>Workorder Date</th>
            <th>Right</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((x) => (
            <tr key={x.id}>
              <td>{x.id}</td>
              <td>{x.employeeFirstName + " " + x.employeeLastName}</td>
              <td>{x.componentName}</td>
              <td>{x.workOrderNumber}</td>
              <td>{x.workOrderDate}</td>
              <td>{x.rightName}</td>
              <td>
                <button className="small" onClick={() => edit(x)}>
                  Edit
                </button>{" "}
                <button className="small danger" onClick={() => remove(x.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
