import { useEffect, useState } from "react";
import { tenco, money, Payment, Vendor } from "../lib/tenco";

export default function Approvals() {
  const [pending, setPending] = useState<Payment[]>([]);
  const [names, setNames] = useState<Record<string, string>>({});
  const [err, setErr] = useState<string | null>(null);

  const load = () =>
    tenco.payments()
      .then((ps) => setPending(ps.filter((p) => p.method === "CASH" && p.status === "PENDING_VERIFICATION")))
      .catch((e) => setErr(e.message));
  useEffect(() => {
    load();
    tenco.vendors().then((vs: Vendor[]) => setNames(Object.fromEntries(vs.map((v) => [v.id, v.name])))).catch(() => {});
  }, []);

  async function act(id: string, status: string) {
    await tenco.setPaymentStatus(id, status);
    load();
  }

  return (
    <>
      <h1 className="page-title">Cash approvals</h1>
      <p className="page-sub">{pending.length} pending</p>
      {err && <div className="error">{err}</div>}

      {pending.length === 0 ? (
        <div className="card muted">No pending cash payments.</div>
      ) : (
        <div className="grid" style={{ gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))" }}>
          {pending.map((p) => (
            <div className="card" key={p.id}>
              <div style={{ fontWeight: 700 }}>{names[p.vendorId] || "—"}</div>
              <div style={{ fontSize: 24, fontWeight: 800, color: "var(--green)", margin: "6px 0 14px" }}>{money(p.amountPaise)}</div>
              <div className="row" style={{ gap: 8 }}>
                <button className="btn ghost" style={{ color: "var(--red)" }} onClick={() => act(p.id, "REJECTED")}>Reject</button>
                <button className="btn" onClick={() => act(p.id, "COMPLETED")}>Approve</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
