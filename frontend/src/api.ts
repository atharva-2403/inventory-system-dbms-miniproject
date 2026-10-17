export interface Category { id: number; name: string; description: string | null; }
export interface Product {
  id: number; categoryId: number; categoryName: string; name: string; unitPrice: number;
  quantityInStock: number; reorderLevel: number; stockValue: number;
  subtype: "PERISHABLE" | "NON_PERISHABLE"; expiryDate: string | number[] | null;
  storageTemperature: number | null; warrantyPeriodMonths: number | null;
}
export interface Supplier { id: number; name: string; street: string; city: string; state: string; pincode: string; }
export interface Warehouse { id: number; street: string; city: string; state: string; pincode: string; capacity: number; managerId: number | null; }
export interface Customer { id: number; firstName: string; lastName: string; street: string; city: string; state: string; pincode: string; }
export interface Employee { id: number; firstName: string; lastName: string; dob: string; }
export interface OrderDetail { id: number; productId: number; quantity: number; unitPrice: number; }
export interface PurchaseOrder { id: number; supplierId: number; orderDate: string; status: string; approvedByManagerId: number | null; total: number; details: OrderDetail[]; }
export interface SalesOrder { id: number; customerId: number; orderDate: string; status: string; total: number; details: OrderDetail[]; }
export interface StockTransaction {
  id: number; productId: number; transactionType: string; quantity: number; transactionDate: string;
  recordedByEmployeeId: number | null; purchaseOrderId: number | null; salesOrderId: number | null;
}

interface ErrorResponse { error?: { message?: string }; }
async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(path, { headers: { "Content-Type": "application/json" }, ...options });
  const text = await response.text();
  let body: (T & ErrorResponse) | undefined;
  try { body = text ? JSON.parse(text) as T & ErrorResponse : undefined; } catch { body = undefined; }
  if (!response.ok) throw new Error(body?.error?.message ?? "The request failed.");
  return body as T;
}
const resource = <T, C>(path: string) => ({
  list: () => request<T[]>(path),
  create: (value: C) => request<T>(path, { method: "POST", body: JSON.stringify(value) }),
  update: (id: number, value: C) => request<T>(`${path}/${id}`, { method: "PUT", body: JSON.stringify(value) }),
  remove: (id: number) => request<void>(`${path}/${id}`, { method: "DELETE" }),
});
export const categoryApi = resource<Category, { name: string; description: string }>("/api/categories");
export const productApi = {
  list: () => request<Product[]>("/api/products"),
  create: (product: Omit<Product, "id" | "categoryName" | "stockValue">) => request<Product>("/api/products", { method: "POST", body: JSON.stringify(product) }),
  update: (id: number, product: Omit<Product, "id" | "categoryName" | "stockValue">) => request<Product>(`/api/products/${id}`, { method: "PUT", body: JSON.stringify(product) }),
  remove: (id: number) => request<void>(`/api/products/${id}`, { method: "DELETE" }),
};
export const supplierApi = resource<Supplier, Omit<Supplier, "id">>("/api/suppliers");
export const warehouseApi = resource<Warehouse, Omit<Warehouse, "id">>("/api/warehouses");
export const customerApi = resource<Customer, Omit<Customer, "id">>("/api/customers");
export const employeeApi = resource<Employee, Omit<Employee, "id">>("/api/employees");
export const purchaseOrderApi = {
  list: () => request<PurchaseOrder[]>("/api/purchase-orders"),
  create: (value: { supplierId: number; orderDate: string; approvedByManagerId: number | null; details: { productId: number; quantity: number; unitPrice: number }[] }) => request<PurchaseOrder>("/api/purchase-orders", { method: "POST", body: JSON.stringify(value) }),
  receive: (id: number, recordedByEmployeeId: number | null) => request<PurchaseOrder>(`/api/purchase-orders/${id}/receive`, { method: "POST", body: JSON.stringify({ recordedByEmployeeId }) }),
};
export const salesOrderApi = {
  list: () => request<SalesOrder[]>("/api/sales-orders"),
  create: (value: { customerId: number; orderDate: string; details: { productId: number; quantity: number; unitPrice: number }[] }) => request<SalesOrder>("/api/sales-orders", { method: "POST", body: JSON.stringify(value) }),
  complete: (id: number, recordedByEmployeeId: number | null) => request<SalesOrder>(`/api/sales-orders/${id}/complete`, { method: "POST", body: JSON.stringify({ recordedByEmployeeId }) }),
};
export const stockTransactionApi = { list: () => request<StockTransaction[]>("/api/stock-transactions") };
