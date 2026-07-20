import { useEffect, useState } from "react";
import { tenco, money, Price, Vendor } from "../lib/tenco";

export default function Pricing() {
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [prices, setPrices] = useState<Price[]>([]);
  const [err, setErr] = useState<string | null>(null);

  const load = () => {
    tenco.vendors().then(setVendors).catch((e) => setErr(e.message));
    tenco.prices().then(setPrices).catch(() => {});
  };
  useEffect(() => {
    load();
  }, []);

  const latest = prices.reduce<Record<string, number>>((acc, p) => {
    if (!(p.vendorId in acc) || p.effectiveFrom > (acc[p.vendorId + "_t"] ?? 0)) {
      acc[p.vendorId] = p.unitPricePaise;
      acc[p.vendorId + "_t"] = p.effectiveFrom;
    }
    return acc;
  }, {});

  async function setPrice(v: Vendor) {
    const input = prompt(`Set unit price (₹) for ${v.name}:`, "");
    if (input == null) return;
    const paise = Math.round((parseFloat(input) || 0) * 100);
    if (paise <= 0) return;
    await tenco.setPrice(v.id, paise);
    load();
  }

  return (
    <>
      <h1 className="page-title">Pricing</h1>
      <p className="page-sub">Set the per-vendor unit price</p>
      {err && <div className="error">{err}</div>}

      <div className="card" style={{ padding: 0 }}>
        <table>
          <thead><tr><th>Vendor</th><th>City</th><th>Current price</th><th></th></tr></thead>
          <tbody>
            {vendors.map((v) => (
              <tr key={v.id}>
                <td style={{ fontWeight: 600 }}>{v.name}</td>
                <td className="muted">{v.city || "—"}</td>
                <td>{latest[v.id] ? money(latest[v.id]) : "—"}</td>
                <td style={{ textAlign: "right" }}>
                  <button className="btn ghost" onClick={() => setPrice(v)}>Set price</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
