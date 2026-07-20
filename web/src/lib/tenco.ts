import { api } from "./api";

export interface SupplierDashboard {
  totalEarningsPaise: number;
  stockOnHand: number;
  duesReceivablePaise: number;
  lossesPaise: number;
}
export interface Pnl {
  revenuePaise: number;
  purchaseCostPaise: number;
  complaintLossesPaise: number;
  netProfitPaise: number;
}
export interface Vendor {
  id: string;
  name: string;
  phone: string;
  upiVpa?: string | null;
  city?: string | null;
}
export interface Dealer {
  id: string;
  name: string;
  location: string;
}
export interface Complaint {
  id: string;
  vendorId: string;
  reason: string;
  status: string;
  adjustmentPaise: number;
  createdAt: number;
  shortQuantity?: number;
}
export interface Payment {
  id: string;
  vendorId: string;
  amountPaise: number;
  method: string;
  status: string;
  createdAt: number;
}

export const tenco = {
  dashboard: () => api.get<SupplierDashboard>("/api/suppliers/me/dashboard"),
  pnl: () => api.get<Pnl>("/api/reports/pnl"),
  vendors: () => api.get<Vendor[]>("/api/vendors"),
  dealers: () => api.get<Dealer[]>("/api/dealers"),
  complaints: () => api.get<Complaint[]>("/api/complaints"),
  payments: () => api.get<Payment[]>("/api/payments"),
  addVendor: (b: { name: string; phone: string; upiVpa?: string | null; city?: string }) =>
    api.post<Vendor>("/api/vendors", b),
  addDealer: (b: { name: string; location: string }) => api.post<Dealer>("/api/dealers", b),
  resolveComplaint: (id: string, adjustmentPaise: number) =>
    api.put<Complaint>(`/api/complaints/${id}/resolve`, { adjustmentPaise }),
};

export const money = (paise: number) =>
  "₹" + (paise / 100).toLocaleString("en-IN", { maximumFractionDigits: 2 });
