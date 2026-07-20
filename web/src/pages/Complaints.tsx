import { useEffect, useState } from "react";
import { tenco, money, Complaint, Vendor } from "../lib/tenco";

const REASON: Record<string, string> = {
  spoiled: "Spoiled coconuts",
  damaged: "Damaged in transit",
  short: "Short quantity",
  other: "Other",
};
function chipClass(status: string) {
  if (status === "RESOLVED") return "chip green";
  if (status === "REJECTED") return "chip red";
  return "chip amber";
}

export default function Complaints() {
  const [items, setItems] = useState<Complaint[]>([]);
  const [names, setNames] = useState<Record<string, string>>({});
  const [err, setErr] = useState<string | null>(null);

  const load = () => tenco.complaints().then(setItems).catch((e) => setErr(e.message));
  useEffect(() => {
    load();
    tenco.vendors().then((vs: Vendor[]) => setNames(Object.fromEntries(vs.map((v) => [v.id, v.name])))).catch(() => {});
  }, []);

  async function resolve(c: Complaint) {
    const input = prompt("Price adjustment (₹) to resolve this complaint:", "0");
    if (input == null) return;
    const rupees = parseFloat(input) || 0;
    await tenco.resolveComplaint(c.id, Math.round(rupees * 100));
    load();
  }

  return (
    <>
      <h1 className="page-title">Complaints</h1>
      <p className="page-sub">{items.filter((c) => c.status === "OPEN").length} open</p>
      {err && <div className="error">{err}</div>}

      <div className="card" style={{ padding: 0 }}>
        <table>
          <thead>
            <tr><th>Vendor</th><th>Reason</th><th>Qty affected</th><th>Status</th><th>Adjustment</th><th></th></tr>
          </thead>
          <tbody>
            {items.map((c) => (
              <tr key={c.id}>
                <td style={{ fontWeight: 600 }}>{names[c.vendorId] || "—"}</td>
                <td>{REASON[c.reason] || c.reason}</td>
                <td>{c.shortQuantity ? `${c.shortQuantity} coconuts` : "—"}</td>
                <td><span className={chipClass(c.status)}>{c.status}</span></td>
                <td>{c.adjustmentPaise ? money(c.adjustmentPaise) : "—"}</td>
                <td style={{ textAlign: "right" }}>
                  {c.status === "OPEN" || c.status === "UNDER_REVIEW" ? (
                    <button className="btn ghost" onClick={() => resolve(c)}>Resolve</button>
                  ) : null}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
