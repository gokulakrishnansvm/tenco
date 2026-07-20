import { useEffect, useState } from "react";
import { tenco, Vendor } from "../lib/tenco";

export default function Vendors() {
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [err, setErr] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);
  const [form, setForm] = useState({ name: "", phone: "", city: "", upiVpa: "" });

  const load = () => tenco.vendors().then(setVendors).catch((e) => setErr(e.message));
  useEffect(() => {
    load();
  }, []);

  const byCity = vendors.reduce<Record<string, Vendor[]>>((acc, v) => {
    const c = v.city && v.city.trim() ? v.city : "No city";
    (acc[c] ||= []).push(v);
    return acc;
  }, {});

  async function save() {
    if (!form.name.trim()) return;
    await tenco.addVendor({
      name: form.name.trim(),
      phone: form.phone,
      city: form.city.trim(),
      upiVpa: form.upiVpa.trim() || null,
    });
    setForm({ name: "", phone: "", city: "", upiVpa: "" });
    setAdding(false);
    load();
  }

  return (
    <>
      <div className="row" style={{ justifyContent: "space-between" }}>
        <div>
          <h1 className="page-title">Vendors</h1>
          <p className="page-sub">{vendors.length} vendors</p>
        </div>
        <button className="btn" onClick={() => setAdding((a) => !a)}>
          {adding ? "Close" : "+ Add vendor"}
        </button>
      </div>
      {err && <div className="error">{err}</div>}

      {adding && (
        <div className="card" style={{ marginBottom: 18, maxWidth: 520 }}>
          <label className="field"><span>Name</span><input className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
          <label className="field"><span>Phone</span><input className="input" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value.replace(/\D/g, "").slice(0, 10) })} /></label>
          <label className="field"><span>City</span><input className="input" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} /></label>
          <label className="field"><span>UPI VPA</span><input className="input" value={form.upiVpa} onChange={(e) => setForm({ ...form, upiVpa: e.target.value })} /></label>
          <button className="btn" disabled={!form.name.trim()} onClick={save}>Save</button>
        </div>
      )}

      {Object.entries(byCity).sort().map(([city, list]) => (
        <div key={city} style={{ marginBottom: 22 }}>
          <div className="section-title">{city} ({list.length})</div>
          <div className="card" style={{ padding: 0 }}>
            <table>
              <thead><tr><th>Name</th><th>Phone</th><th>UPI</th></tr></thead>
              <tbody>
                {list.map((v) => (
                  <tr key={v.id}>
                    <td style={{ fontWeight: 600 }}>{v.name}</td>
                    <td>{v.phone}</td>
                    <td className="muted">{v.upiVpa || "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}
    </>
  );
}
