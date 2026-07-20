import { useEffect, useState } from "react";
import { tenco, money, Delivery, Payment, Vendor } from "../lib/tenco";

interface Row {
  time: number;
  vendor: string;
  detail: string;
  amount: string;
  status: string;
}

export default function Transactions() {
  const [rows, setRows] = useState<Row[]>([]);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([tenco.vendors(), tenco.deliveries(), tenco.payments()])
      .then(([vs, ds, ps]: [Vendor[], Delivery[], Payment[]]) => {
        const names = Object.fromEntries(vs.map((v) => [v.id, v.name]));
        const r: Row[] = [
          ...ds.map((d) => ({ time: d.createdAt, vendor: names[d.vendorId] || "—", detail: `Sale · ${d.quantity} coconuts`, amount: "+" + money(d.quantity * d.unitPricePaise), status: d.status })),
          ...ps.map((p) => ({ time: p.createdAt, vendor: names[p.vendorId] || "—", detail: `Payment · ${p.method}`, amount: "-" + money(p.amountPaise), status: p.status })),
        ].sort((a, b) => b.time - a.time);
        setRows(r);
      })
      .catch((e) => setErr(e.message));
  }, []);

  return (
    <>
      <h1 className="page-title">Transactions</h1>
      <p className="page-sub">Sales &amp; payments</p>
      {err && <div className="error">{err}</div>}
      <div className="card" style={{ padding: 0 }}>
        <table>
          <thead><tr><th>Date</th><th>Vendor</th><th>Detail</th><th>Amount</th><th>Status</th></tr></thead>
          <tbody>
            {rows.map((r, i) => (
              <tr key={i}>
                <td className="muted">{new Date(r.time).toLocaleDateString("en-IN")}</td>
                <td style={{ fontWeight: 600 }}>{r.vendor}</td>
                <td>{r.detail}</td>
                <td>{r.amount}</td>
                <td><span className={r.status === "COMPLETED" || r.status === "CONFIRMED" ? "chip green" : "chip amber"}>{r.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
