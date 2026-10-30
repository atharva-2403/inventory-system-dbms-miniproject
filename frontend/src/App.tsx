import { FormEvent, useEffect, useState, type ReactNode } from "react";
import { employeeRoleApi, supplierProductApi, EmployeeRole, LinkedProduct } from "./api";
import { Contact, customerContactApi, employeeContactApi, supplierContactApi } from "./api";
import {
  categoryApi, Category, customerApi, Customer, employeeApi, Employee, productApi, Product,
  purchaseOrderApi, PurchaseOrder, salesOrderApi, SalesOrder, stockTransactionApi, StockTransaction,
  supplierApi, Supplier, warehouseApi, Warehouse,
} from "./api";

type Page = "home" | "categories" | "products" | "suppliers" | "warehouses" | "customers" | "employees" | "purchase-orders" | "sales-orders" | "stock-transactions";
const today = () => new Date().toISOString().slice(0, 10);

export default function App() {
  const [page, setPage] = useState<Page>("home");
  return <main className="container">
    <header><p className="eyebrow">Inventory Management System</p><h1>{pageTitle(page)}</h1><p className="subtitle">Simple inventory, order, and master data management.</p><Navigation page={page} onNavigate={setPage} /></header>
    {page === "home" && <HomePage onNavigate={setPage} />}
    {page === "categories" && <CategoriesPage />}
    {page === "products" && <ProductsPage />}
    {["suppliers", "warehouses", "customers", "employees"].includes(page) && <MasterPage kind={page as MasterKind} />}
    {page === "purchase-orders" && <OrdersPage kind="purchase" />}
    {page === "sales-orders" && <OrdersPage kind="sales" />}
    {page === "stock-transactions" && <TransactionsPage />}
  </main>;
}
function pageTitle(page: Page) { return page.split("-").map((word) => word[0].toUpperCase() + word.slice(1)).join(" "); }
function Navigation({ page, onNavigate }: { page: Page; onNavigate: (page: Page) => void }) {
  const links: [Page, string][] = [["home", "Home"], ["categories", "Categories"], ["products", "Products"], ["suppliers", "Suppliers"], ["warehouses", "Warehouses"], ["customers", "Customers"], ["employees", "Employees"], ["purchase-orders", "Purchase orders"], ["sales-orders", "Sales orders"], ["stock-transactions", "Stock transactions"]];
  return <nav className="nav">{links.map(([key, label]) => <button key={key} className={page === key ? "" : "secondary"} onClick={() => onNavigate(key)}>{label}</button>)}</nav>;
}
function HomePage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  const [counts, setCounts] = useState({ categories: 0, products: 0, suppliers: 0, customers: 0, lowStock: 0 });
  const [error, setError] = useState("");
  useEffect(() => {
    Promise.all([categoryApi.list(), productApi.list(), supplierApi.list(), customerApi.list()])
      .then(([categories, products, suppliers, customers]) => setCounts({
        categories: categories.length, products: products.length, suppliers: suppliers.length,
        customers: customers.length, lowStock: products.filter((p) => p.quantityInStock <= p.reorderLevel).length,
      }))
      .catch((e) => setError(errorText(e)));
  }, []);
  return <section className="card">
    <h2>Inventory summary</h2>
    {error && <p className="error">{error}</p>}
    <div className="summary-grid">
      {Object.entries(counts).map(([label, value]) => <div className="summary-tile" key={label}><strong>{value}</strong><span>{label.replace(/([A-Z])/g, " $1")}</span></div>)}
    </div>
    <div className="actions"><button onClick={() => onNavigate("products")}>Manage products</button><button className="secondary" onClick={() => onNavigate("purchase-orders")}>Purchase orders</button><button className="secondary" onClick={() => onNavigate("sales-orders")}>Sales orders</button></div>
  </section>;
}
function useMessages() {
  const [message, setMessage] = useState(""); const [error, setError] = useState("");
  return { message, error, setMessage, setError, clear: () => { setMessage(""); setError(""); } };
}
function Feedback({ message, error }: { message: string; error: string }) { return <>{message && <p className="success">{message}</p>}{error && <p className="error">{error}</p>}</>; }
function CategoriesPage() {
  const [items, setItems] = useState<Category[]>([]); const [form, setForm] = useState({ name: "", description: "" }); const [editingId, setEditingId] = useState<number | null>(null); const msg = useMessages();
  const load = async () => { try { setItems(await categoryApi.list()); } catch (e) { msg.setError(errorText(e)); } };
  useEffect(() => { void load(); }, []);
  async function submit(e: FormEvent) { e.preventDefault(); msg.clear(); try { if (editingId === null) await categoryApi.create(form); else await categoryApi.update(editingId, form); setForm({ name: "", description: "" }); setEditingId(null); msg.setMessage(editingId === null ? "Category created." : "Category updated."); await load(); } catch (x) { msg.setError(errorText(x)); } }
  async function remove(item: Category) { if (!window.confirm(`Delete "${item.name}"?`)) return; try { await categoryApi.remove(item.id); msg.setMessage("Category deleted."); await load(); } catch (x) { msg.setError(errorText(x)); } }
  return <><section className="card"><h2>{editingId === null ? "Add" : "Edit"} category</h2><form onSubmit={submit}><label>Name<input required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} /></label><label>Description<textarea value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} /></label><div className="actions"><button>{editingId === null ? "Add" : "Save"} category</button>{editingId !== null && <button type="button" className="secondary" onClick={() => { setEditingId(null); setForm({ name: "", description: "" }); }}>Cancel</button>}</div></form></section><Feedback {...msg} /><ListTable title="Categories" loading={false} empty={!items.length} headers={["Name", "Description", "Actions"]}>{items.map(x => <tr key={x.id}><td>{x.name}</td><td>{x.description || "—"}</td><td className="actions"><button className="secondary" onClick={() => { setEditingId(x.id); setForm({ name: x.name, description: x.description ?? "" }); }}>Edit</button><button className="danger" onClick={() => void remove(x)}>Delete</button></td></tr>)}</ListTable></>;
}
function ProductsPage() {
  const [categories, setCategories] = useState<Category[]>([]); const [items, setItems] = useState<Product[]>([]); const [editingId, setEditingId] = useState<number | null>(null); const msg = useMessages();
  const [form, setForm] = useState({ categoryId: 0, name: "", unitPrice: 0, quantityInStock: 0, reorderLevel: 0, subtype: "NON_PERISHABLE" as Product["subtype"], expiryDate: "", storageTemperature: 0, warrantyPeriodMonths: 0 });
  const load = async () => { try { const [c, p] = await Promise.all([categoryApi.list(), productApi.list()]); setCategories(c); setItems(p); if (!form.categoryId && c[0]) setForm(f => ({ ...f, categoryId: c[0].id })); } catch (e) { msg.setError(errorText(e)); } };
  useEffect(() => { void load(); }, []);
  async function submit(e: FormEvent) { e.preventDefault(); msg.clear(); const payload = { ...form, expiryDate: form.subtype === "PERISHABLE" ? form.expiryDate : null, storageTemperature: form.subtype === "PERISHABLE" ? form.storageTemperature : null, warrantyPeriodMonths: form.subtype === "NON_PERISHABLE" ? form.warrantyPeriodMonths : null }; try { if (editingId === null) await productApi.create(payload); else await productApi.update(editingId, payload); setEditingId(null); setForm(f => ({ ...f, name: "", unitPrice: 0, quantityInStock: 0, reorderLevel: 0 })); msg.setMessage(editingId === null ? "Product created." : "Product updated."); await load(); } catch (x) { msg.setError(errorText(x)); } }
  async function remove(item: Product) { if (!window.confirm(`Delete "${item.name}"?`)) return; try { await productApi.remove(item.id); msg.setMessage("Product deleted."); await load(); } catch (x) { msg.setError(errorText(x)); } }
  function edit(item: Product) { setEditingId(item.id); setForm({ categoryId: item.categoryId, name: item.name, unitPrice: item.unitPrice, quantityInStock: item.quantityInStock, reorderLevel: item.reorderLevel, subtype: item.subtype, expiryDate: Array.isArray(item.expiryDate) ? item.expiryDate.join("-") : item.expiryDate ?? "", storageTemperature: item.storageTemperature ?? 0, warrantyPeriodMonths: item.warrantyPeriodMonths ?? 0 }); }
  return <><section className="card"><h2>{editingId === null ? "Add" : "Edit"} product</h2><form onSubmit={submit}><label>Name<input required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} /></label><label>Category<select value={form.categoryId} onChange={e => setForm({ ...form, categoryId: Number(e.target.value) })}>{categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select></label><div className="form-grid"><label>Unit price<input type="number" min="0" step=".01" value={form.unitPrice} onChange={e => setForm({ ...form, unitPrice: Number(e.target.value) })} /></label><label>Starting stock<input type="number" min="0" value={form.quantityInStock} onChange={e => setForm({ ...form, quantityInStock: Number(e.target.value) })} /></label><label>Reorder level<input type="number" min="0" value={form.reorderLevel} onChange={e => setForm({ ...form, reorderLevel: Number(e.target.value) })} /></label></div><label>Subtype<select value={form.subtype} onChange={e => setForm({ ...form, subtype: e.target.value as Product["subtype"] })}><option value="NON_PERISHABLE">Non-perishable</option><option value="PERISHABLE">Perishable</option></select></label>{form.subtype === "PERISHABLE" ? <div className="form-grid"><label>Expiry date<input type="date" required value={form.expiryDate} onChange={e => setForm({ ...form, expiryDate: e.target.value })} /></label><label>Storage temperature<input type="number" value={form.storageTemperature} onChange={e => setForm({ ...form, storageTemperature: Number(e.target.value) })} /></label></div> : <label>Warranty months<input type="number" min="0" value={form.warrantyPeriodMonths} onChange={e => setForm({ ...form, warrantyPeriodMonths: Number(e.target.value) })} /></label>}<div className="actions"><button disabled={!categories.length}>{editingId === null ? "Add" : "Save"} product</button>{editingId !== null && <button type="button" className="secondary" onClick={() => setEditingId(null)}>Cancel</button>}</div></form></section><Feedback {...msg} /><ListTable title="Products" loading={false} empty={!items.length} headers={["Name", "Category", "Price", "Stock", "Actions"]}>{items.map(p => <tr key={p.id}><td>{p.name}</td><td>{p.categoryName}</td><td>{p.unitPrice.toFixed(2)}</td><td>{p.quantityInStock}{p.quantityInStock <= p.reorderLevel && <strong className="low-stock">Low stock</strong>}</td><td className="actions"><button className="secondary" onClick={() => edit(p)}>Edit</button><button className="danger" onClick={() => void remove(p)}>Delete</button></td></tr>)}</ListTable></>;
}

type MasterKind = "suppliers" | "warehouses" | "customers" | "employees";
type MasterItem = Supplier | Warehouse | Customer | Employee;
const masterConfigs: Record<MasterKind, { title: string; fields: { key: string; label: string; type?: string; required?: boolean }[]; api: { list: () => Promise<MasterItem[]>; create: (x: any) => Promise<MasterItem>; update: (id: number, x: any) => Promise<MasterItem>; remove: (id: number) => Promise<void> } }> = {
  suppliers: { title: "Supplier", fields: [{ key: "name", label: "Name", required: true }, ...addressFields()], api: supplierApi },
  warehouses: { title: "Warehouse", fields: [...addressFields(), { key: "capacity", label: "Capacity", type: "number", required: true }, { key: "managerId", label: "Manager (optional)", type: "manager" }], api: warehouseApi },
  customers: { title: "Customer", fields: [{ key: "firstName", label: "First name", required: true }, { key: "lastName", label: "Last name", required: true }, ...addressFields()], api: customerApi },
  employees: { title: "Employee", fields: [{ key: "firstName", label: "First name", required: true }, { key: "lastName", label: "Last name", required: true }, { key: "dob", label: "Date of birth", type: "date", required: true }], api: employeeApi },
};
function addressFields() { return [{ key: "street", label: "Street" }, { key: "city", label: "City" }, { key: "state", label: "State" }, { key: "pincode", label: "Pincode" }]; }
function MasterPage({ kind }: { kind: MasterKind }) {
  const [contactsFor, setContactsFor] = useState<MasterItem | null>(null);
  useEffect(() => { setContactsFor(null); }, [kind]);
  const [panel, setPanel] = useState<{ type: "products" | "role"; item: MasterItem } | null>(null);
  const [managers, setManagers] = useState<Employee[]>([]);
  useEffect(() => { setPanel(null); }, [kind]);
  useEffect(() => { if (kind === "warehouses") employeeRoleApi.managers().then(setManagers).catch(() => setManagers([])); }, [kind]);
  const config = masterConfigs[kind]; const [items, setItems] = useState<MasterItem[]>([]); const [form, setForm] = useState<Record<string, string | number>>({}); const [editingId, setEditingId] = useState<number | null>(null); const [loading, setLoading] = useState(true); const msg = useMessages();
  const load = async () => { setLoading(true); try { setItems(await config.api.list()); } catch (e) { msg.setError(errorText(e)); } finally { setLoading(false); } };
  useEffect(() => { void load(); }, [kind]);
  async function submit(e: FormEvent) { e.preventDefault(); msg.clear(); try { const payload = Object.fromEntries(config.fields.map(f => [f.key, form[f.key] === "" ? null : form[f.key]])); if (editingId === null) await config.api.create(payload); else await config.api.update(editingId, payload); setForm({}); setEditingId(null); msg.setMessage(`${config.title} ${editingId === null ? "created" : "updated"}.`); await load(); } catch (x) { msg.setError(errorText(x)); } }
  async function remove(item: MasterItem) { if (!window.confirm(`Delete this ${config.title.toLowerCase()}?`)) return; try { await config.api.remove(item.id); msg.setMessage(`${config.title} deleted.`); await load(); } catch (x) { msg.setError(errorText(x)); } }
  return <><section className="card"><h2>{editingId === null ? "Add" : "Edit"} {config.title.toLowerCase()}</h2><form onSubmit={submit}>{config.fields.map(f => <label key={f.key}>{f.label}{f.type === "manager" ? <select value={form[f.key] ?? ""} onChange={e => setForm({ ...form, [f.key]: e.target.value === "" ? "" : Number(e.target.value) })}><option value="">No manager</option>{managers.map(m => <option key={m.id} value={m.id}>{m.firstName} {m.lastName} (ID {m.id})</option>)}</select> : <input type={f.type || "text"} required={f.required} value={form[f.key] ?? ""} onChange={e => setForm({ ...form, [f.key]: f.type === "number" ? Number(e.target.value) : e.target.value })} />}</label>)}<div className="actions"><button>{editingId === null ? "Add" : "Save"} {config.title.toLowerCase()}</button>{editingId !== null && <button type="button" className="secondary" onClick={() => { setEditingId(null); setForm({}); }}>Cancel</button>}</div></form></section><Feedback {...msg} />{contactsFor && kind !== "warehouses" && <ContactsPanel key={`${kind}-${contactsFor.id}`} kind={kind} owner={contactsFor} onClose={() => setContactsFor(null)} />}{panel && panel.type === "products" && kind === "suppliers" && <SupplierProductsPanel key={`p-${panel.item.id}`} supplier={panel.item as Supplier} onClose={() => setPanel(null)} />}{panel && panel.type === "role" && kind === "employees" && <EmployeeRolePanel key={`r-${panel.item.id}`} employee={panel.item as Employee} onClose={() => setPanel(null)} />}<ListTable title={`${config.title} list`} loading={loading} empty={!items.length} headers={[...masterHeaders(kind), "Actions"]}>{items.map(item => <tr key={item.id}>{masterCells(kind, item)}<td className="actions">{kind !== "warehouses" && <button className="secondary" onClick={() => setContactsFor(item)}>Contacts</button>}{kind === "suppliers" && <button className="secondary" onClick={() => setPanel({ type: "products", item })}>Products</button>}{kind === "employees" && <button className="secondary" onClick={() => setPanel({ type: "role", item })}>Role</button>}<button className="secondary" onClick={() => { setEditingId(item.id); setForm(Object.fromEntries(config.fields.map(f => [f.key, (item as unknown as Record<string, string | number | null>)[f.key] ?? ""]))); }}>Edit</button><button className="danger" onClick={() => void remove(item)}>Delete</button></td></tr>)}</ListTable></>;
}
function masterHeaders(kind: MasterKind) { return kind === "suppliers" ? ["Name", "City", "State"] : kind === "warehouses" ? ["Address", "City", "Capacity", "Manager"] : kind === "customers" ? ["Name", "City", "State"] : ["Name", "Date of birth"]; }
function masterCells(kind: MasterKind, item: MasterItem) {
  if (kind === "suppliers") { const x = item as Supplier; return <><td>{x.name}</td><td>{x.city}</td><td>{x.state}</td></>; }
  if (kind === "warehouses") { const x = item as Warehouse; return <><td>{x.street}</td><td>{x.city}</td><td>{x.capacity}</td><td>{x.managerId ?? "—"}</td></>; }
  if (kind === "customers") { const x = item as Customer; return <><td>{x.firstName} {x.lastName}</td><td>{x.city}</td><td>{x.state}</td></>; }
  const x = item as Employee; return <><td>{x.firstName} {x.lastName}</td><td>{x.dob}</td></>;
}

function ContactsPanel({ kind, owner, onClose }: { kind: MasterKind; owner: MasterItem; onClose: () => void }) {
  const api = kind === "suppliers" ? supplierContactApi : kind === "customers" ? customerContactApi : employeeContactApi;
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [value, setValue] = useState("");
  const msg = useMessages();
  const label = kind === "suppliers" ? (owner as Supplier).name : `${(owner as Customer | Employee).firstName} ${(owner as Customer | Employee).lastName}`;
  const load = async () => { try { setContacts(await api.list(owner.id)); } catch (e) { msg.setError(errorText(e)); } };
  useEffect(() => { void load(); }, [owner.id]);
  async function add(e: FormEvent) {
    e.preventDefault(); msg.clear();
    try { await api.add(owner.id, value); setValue(""); msg.setMessage("Contact added."); await load(); } catch (x) { msg.setError(errorText(x)); }
  }
  async function remove(c: Contact) {
    msg.clear();
    try { await api.remove(owner.id, c.id); msg.setMessage("Contact deleted."); await load(); } catch (x) { msg.setError(errorText(x)); }
  }
  return <section className="card">
    <h2>Contacts for {label}</h2>
    <Feedback {...msg} />
    {contacts.length === 0 ? <p>No contacts yet.</p> : <table><thead><tr><th>Contact (email or phone)</th><th>Actions</th></tr></thead><tbody>
      {contacts.map(c => <tr key={c.id}><td>{c.value}</td><td className="actions"><button className="danger" onClick={() => void remove(c)}>Delete</button></td></tr>)}
    </tbody></table>}
    <form onSubmit={add}>
      <label>New contact (email or phone)<input required maxLength={150} value={value} onChange={e => setValue(e.target.value)} /></label>
      <div className="actions"><button>Add contact</button><button type="button" className="secondary" onClick={onClose}>Close</button></div>
    </form>
  </section>;
}

function SupplierProductsPanel({ supplier, onClose }: { supplier: Supplier; onClose: () => void }) {
  const [linked, setLinked] = useState<LinkedProduct[]>([]);
  const [all, setAll] = useState<Product[]>([]);
  const [selected, setSelected] = useState(0);
  const msg = useMessages();
  const load = async () => {
    try {
      const [l, p] = await Promise.all([supplierProductApi.list(supplier.id), productApi.list()]);
      setLinked(l); setAll(p);
      setSelected(p.find(x => !l.some(y => y.id === x.id))?.id ?? 0);
    } catch (e) { msg.setError(errorText(e)); }
  };
  useEffect(() => { void load(); }, [supplier.id]);
  const available = all.filter(p => !linked.some(l => l.id === p.id));
  async function add(e: FormEvent) {
    e.preventDefault(); msg.clear();
    try { await supplierProductApi.add(supplier.id, selected); msg.setMessage("Product linked."); await load(); } catch (x) { msg.setError(errorText(x)); }
  }
  async function remove(p: LinkedProduct) {
    msg.clear();
    try { await supplierProductApi.remove(supplier.id, p.id); msg.setMessage("Product unlinked."); await load(); } catch (x) { msg.setError(errorText(x)); }
  }
  return <section className="card">
    <h2>Products supplied by {supplier.name}</h2>
    <Feedback {...msg} />
    {linked.length === 0 ? <p>No products linked yet.</p> : <table><thead><tr><th>Product</th><th>Actions</th></tr></thead><tbody>
      {linked.map(p => <tr key={p.id}><td>{p.name}</td><td className="actions"><button className="danger" onClick={() => void remove(p)}>Unlink</button></td></tr>)}
    </tbody></table>}
    {available.length === 0 ? <p>Every product is already linked to this supplier.</p> : <form onSubmit={add}>
      <label>Link another product<select value={selected} onChange={e => setSelected(Number(e.target.value))}>{available.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}</select></label>
      <div className="actions"><button>Link product</button><button type="button" className="secondary" onClick={onClose}>Close</button></div>
    </form>}
    {available.length === 0 && <div className="actions"><button type="button" className="secondary" onClick={onClose}>Close</button></div>}
  </section>;
}

function EmployeeRolePanel({ employee, onClose }: { employee: Employee; onClose: () => void }) {
  const [role, setRole] = useState<EmployeeRole>({ type: "NONE", department: "", teamSize: 0, warehouseId: null, shiftTiming: "" });
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const msg = useMessages();
  const load = async () => {
    try {
      const [r, w] = await Promise.all([employeeRoleApi.get(employee.id), warehouseApi.list()]);
      setWarehouses(w);
      setRole({ type: r.type, department: r.department ?? "", teamSize: r.teamSize ?? 0, warehouseId: r.warehouseId ?? w[0]?.id ?? null, shiftTiming: r.shiftTiming ?? "" });
    } catch (e) { msg.setError(errorText(e)); }
  };
  useEffect(() => { void load(); }, [employee.id]);
  async function save(e: FormEvent) {
    e.preventDefault(); msg.clear();
    try { await employeeRoleApi.set(employee.id, role); msg.setMessage("Role saved."); await load(); } catch (x) { msg.setError(errorText(x)); }
  }
  return <section className="card">
    <h2>Role of {employee.firstName} {employee.lastName}</h2>
    <p><small>An employee can be a manager, or warehouse staff, or neither - never both.</small></p>
    <Feedback {...msg} />
    <form onSubmit={save}>
      <label>Role<select value={role.type} onChange={e => setRole({ ...role, type: e.target.value as EmployeeRole["type"] })}>
        <option value="NONE">None</option><option value="MANAGER">Manager</option><option value="WAREHOUSE_STAFF">Warehouse staff</option>
      </select></label>
      {role.type === "MANAGER" && <div className="form-grid">
        <label>Department<input required maxLength={100} value={role.department ?? ""} onChange={e => setRole({ ...role, department: e.target.value })} /></label>
        <label>Team size<input type="number" min="0" value={role.teamSize ?? 0} onChange={e => setRole({ ...role, teamSize: Number(e.target.value) })} /></label>
      </div>}
      {role.type === "WAREHOUSE_STAFF" && <div className="form-grid">
        <label>Warehouse<select required value={role.warehouseId ?? ""} onChange={e => setRole({ ...role, warehouseId: Number(e.target.value) })}>
          {warehouses.map(w => <option key={w.id} value={w.id}>{`Warehouse ${w.id} - ${w.street ?? ""} ${w.city ?? ""}`}</option>)}
        </select></label>
        <label>Shift timing<input required maxLength={100} placeholder="09:00-17:00" value={role.shiftTiming ?? ""} onChange={e => setRole({ ...role, shiftTiming: e.target.value })} /></label>
      </div>}
      <div className="actions"><button>Save role</button><button type="button" className="secondary" onClick={onClose}>Close</button></div>
    </form>
  </section>;
}

type OrderKind = "purchase" | "sales";
function OrdersPage({ kind }: { kind: OrderKind }) {
  const [orders, setOrders] = useState<(PurchaseOrder | SalesOrder)[]>([]); const [products, setProducts] = useState<Product[]>([]); const [partners, setPartners] = useState<(Supplier | Customer)[]>([]); const [employees, setEmployees] = useState<Employee[]>([]); const msg = useMessages();
  const [partnerId, setPartnerId] = useState(0); const [orderDate, setOrderDate] = useState(today()); const [employeeId, setEmployeeId] = useState<number | null>(null); const [lines, setLines] = useState([{ productId: 0, quantity: 1 }]);
  const load = async () => { try { const [p, e, o, m] = await Promise.all([productApi.list(), employeeApi.list(), kind === "purchase" ? purchaseOrderApi.list() : salesOrderApi.list(), kind === "purchase" ? supplierApi.list() : customerApi.list()]); setProducts(p); setEmployees(e); setOrders(o); setPartners(m); if (!partnerId && m[0]) setPartnerId(m[0].id); if (!lines[0].productId && p[0]) setLines([{ productId: p[0].id, quantity: 1 }]); } catch (x) { msg.setError(errorText(x)); } };
  useEffect(() => { void load(); }, [kind]);
  function setLine(index: number, key: "productId" | "quantity", value: number) { setLines(old => old.map((line, i) => i === index ? { ...line, [key]: value } : line)); }
  async function submit(e: FormEvent) { e.preventDefault(); msg.clear(); try { const details = lines.map(line => ({ ...line, unitPrice: products.find(p => p.id === line.productId)?.unitPrice ?? 0 })); if (kind === "purchase") await purchaseOrderApi.create({ supplierId: partnerId, orderDate, approvedByManagerId: employeeId, details }); else await salesOrderApi.create({ customerId: partnerId, orderDate, details }); msg.setMessage(`${kind === "purchase" ? "Purchase" : "Sales"} order created.`); await load(); } catch (x) { msg.setError(errorText(x)); } }
  async function action(id: number) { msg.clear(); try { if (kind === "purchase") await purchaseOrderApi.receive(id, employeeId); else await salesOrderApi.complete(id, employeeId); msg.setMessage("Order updated."); await load(); } catch (x) { msg.setError(errorText(x)); } }
  return <><section className="card"><h2>Create {kind === "purchase" ? "purchase" : "sales"} order</h2><form onSubmit={submit}><label>{kind === "purchase" ? "Supplier" : "Customer"}<select required value={partnerId} onChange={e => setPartnerId(Number(e.target.value))}>{partners.map(p => <option key={p.id} value={p.id}>{kind === "purchase" ? (p as Supplier).name : `${(p as Customer).firstName} ${(p as Customer).lastName}`}</option>)}</select></label><div className="form-grid"><label>Order date<input type="date" required value={orderDate} onChange={e => setOrderDate(e.target.value)} /></label><label>Record with employee (optional)<select value={employeeId ?? ""} onChange={e => setEmployeeId(e.target.value ? Number(e.target.value) : null)}><option value="">Not specified</option>{employees.map(x => <option key={x.id} value={x.id}>{x.firstName} {x.lastName}</option>)}</select></label></div><h3>Products</h3>{lines.map((line, i) => <div className="line-item" key={i}><select required value={line.productId} onChange={e => setLine(i, "productId", Number(e.target.value))}>{products.map(p => <option key={p.id} value={p.id}>{p.name} (${p.unitPrice.toFixed(2)})</option>)}</select><input type="number" min="1" required value={line.quantity} onChange={e => setLine(i, "quantity", Number(e.target.value))} /><button type="button" className="danger" disabled={lines.length === 1} onClick={() => setLines(old => old.filter((_, index) => index !== i))}>Remove</button></div>)}<div className="actions"><button type="button" className="secondary" disabled={!products.length} onClick={() => setLines(old => [...old, { productId: products[0]?.id ?? 0, quantity: 1 }])}>Add another product</button><button disabled={!partners.length || !products.length}>Create order</button></div></form></section><Feedback {...msg} /><ListTable title={`${kind === "purchase" ? "Purchase" : "Sales"} orders`} loading={false} empty={!orders.length} headers={["ID", kind === "purchase" ? "Supplier" : "Customer", "Date", "Status", "Total", "Items", "Action"]}>{orders.map(order => { const partner = partners.find(x => x.id === (kind === "purchase" ? (order as PurchaseOrder).supplierId : (order as SalesOrder).customerId)); return <tr key={order.id}><td>{order.id}</td><td>{partner ? kind === "purchase" ? (partner as Supplier).name : `${(partner as Customer).firstName} ${(partner as Customer).lastName}` : "ID " + (kind === "purchase" ? (order as PurchaseOrder).supplierId : (order as SalesOrder).customerId)}</td><td>{order.orderDate}</td><td>{order.status}</td><td>{Number(order.total).toFixed(2)}</td><td>{order.details.map(d => <div key={d.id}>{productName(products, d.productId)} × {d.quantity}</div>)}</td><td>{order.status === "PENDING" && <button onClick={() => void action(order.id)}>{kind === "purchase" ? "Receive" : "Complete"}</button>}</td></tr>; })}</ListTable></>;
}
function productName(products: Product[], id: number) { return products.find(p => p.id === id)?.name ?? `Product ${id}`; }
function TransactionsPage() {
  const [items, setItems] = useState<StockTransaction[]>([]); const msg = useMessages();
  useEffect(() => { stockTransactionApi.list().then(setItems).catch(e => msg.setError(errorText(e))); }, []);
  return <><Feedback {...msg} /><ListTable title="Stock transactions" loading={false} empty={!items.length} headers={["ID", "Product", "Type", "Quantity", "Date", "Employee", "Purchase order", "Sales order"]}>{items.map(x => <tr key={x.id}><td>{x.id}</td><td>{x.productId}</td><td>{x.transactionType}</td><td>{x.quantity}</td><td>{x.transactionDate.replace("T", " ")}</td><td>{x.recordedByEmployeeId ?? "—"}</td><td>{x.purchaseOrderId ?? "—"}</td><td>{x.salesOrderId ?? "—"}</td></tr>)}</ListTable></>;
}
function ListTable({ title, headers, children, loading, empty }: { title: string; headers: string[]; children: ReactNode; loading: boolean; empty: boolean }) {
  return <section className="card"><h2>{title}</h2>{loading ? <p>Loading...</p> : empty ? <p>No records found.</p> : <table><thead><tr>{headers.map(h => <th key={h}>{h}</th>)}</tr></thead><tbody>{children}</tbody></table>}</section>;
}
function errorText(error: unknown) { return error instanceof Error ? error.message : "Something went wrong."; }
