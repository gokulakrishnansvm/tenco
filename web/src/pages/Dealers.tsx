import { useEffect, useState } from "react";
import { tenco, Dealer } from "../lib/tenco";

export default function Dealers() {
  const [dealers, setDealers] = useState<Dealer[]>([]);
  const [err, setErr] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);
  const [form, setForm] = useState({ name: "", location: "" });

  const load = () => tenco.dealers().then(setDealers).catch((e) => setErr(e.message));
  useEffect(() => {
    load();
  }, []);

  async function save() {
    if (!form.name.trim()) return;
    await tenco.addDealer({ name: form.name.trim(), location: form.location.trim() });
    setForm({ name: "", location: "" });
    setAdding(false);
    load();
  }

  return (
    <>
      <div className="row" style={{ justifyContent: "space-between" }}>
        <div>
          <h1 className="page-title">Dealers</h1>
          <p className="page-sub">{dealers.length} dealers</p>
        </div>
        <button className="btn" onClick={() => setAdding((a) => !a)}>
          {adding ? "Close" : "+ Add dealer"}
        </button>
      </div>
      {err && <div className="error">{err}</div>}

      {adding && (
        <div className="card" style={{ marginBottom: 18, maxWidth: 520 }}>
          <label className="field"><span>Name</span><input className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
          <label className="field"><span>Market / location</span><input className="input" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} /></label>
          <button className="btn" disabled={!form.name.trim()} onClick={save}>Save</button>
        </div>
      )}

      <div className="card" style={{ padding: 0 }}>
        <table>
          <thead><tr><th>Name</th><th>Market</th></tr></thead>
          <tbody>
            {dealers.map((d) => (
              <tr key={d.id}>
                <td style={{ fontWeight: 600 }}>{d.name}</td>
                <td className="muted">{d.location}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
