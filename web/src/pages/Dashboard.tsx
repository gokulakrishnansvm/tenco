import { useEffect, useState } from "react";
import { tenco, money, SupplierDashboard, Pnl } from "../lib/tenco";

export default function Dashboard() {
  const [d, setD] = useState<SupplierDashboard | null>(null);
  const [pnl, setPnl] = useState<Pnl | null>(null);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    tenco.dashboard().then(setD).catch((e) => setErr(e.message));
    tenco.pnl().then(setPnl).catch(() => {});
  }, []);

  return (
    <>
      <h1 className="page-title">Dashboard</h1>
      <p className="page-sub">Your coconut supply chain at a glance</p>
      {err && <div className="error">{err}</div>}

      <div className="grid kpis">
        <div className="card kpi">
          <div className="label">Total earnings</div>
          <div className="value">{d ? money(d.totalEarningsPaise) : "—"}</div>
        </div>
        <div className="card kpi">
          <div className="label">Stock on hand</div>
          <div className="value">{d ? d.stockOnHand.toLocaleString("en-IN") : "—"}</div>
        </div>
        <div className="card kpi">
          <div className="label">Dues receivable</div>
          <div className="value orange">{d ? money(d.duesReceivablePaise) : "—"}</div>
        </div>
        <div className="card kpi">
          <div className="label">Losses</div>
          <div className="value red">{d ? money(d.lossesPaise) : "—"}</div>
        </div>
      </div>

      <div className="section-title">Profit &amp; Loss</div>
      <div className="card">
        {pnl ? (
          <table>
            <tbody>
              <PnlRow label="Revenue" value={money(pnl.revenuePaise)} />
              <PnlRow label="Purchase cost" value={"- " + money(pnl.purchaseCostPaise)} />
              <PnlRow label="Complaint losses" value={"- " + money(pnl.complaintLossesPaise)} />
              <PnlRow label="Net profit" value={money(pnl.netProfitPaise)} bold />
            </tbody>
          </table>
        ) : (
          <span className="muted">Loading…</span>
        )}
      </div>
    </>
  );
}

function PnlRow({ label, value, bold }: { label: string; value: string; bold?: boolean }) {
  return (
    <tr>
      <td style={{ fontWeight: bold ? 800 : 500 }}>{label}</td>
      <td style={{ textAlign: "right", fontWeight: bold ? 800 : 600 }}>{value}</td>
    </tr>
  );
}
