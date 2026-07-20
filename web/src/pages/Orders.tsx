import { useEffect, useState } from "react";
import { tenco, money, Order, Vendor } from "../lib/tenco";

const NEXT: Record<string, string> = {
  CONFIRMED: "IN_PROGRESS",
  IN_PROGRESS: "IN_TRANSIT",
  IN_TRANSIT: "DELIVERED",
};
const NEXT_LABEL: Record<string, string> = {
  IN_PROGRESS: "Start preparing",
  IN_TRANSIT: "Out for delivery",
  DELIVERED: "Mark delivered",
};
function chip(status: string) {
  if (status === "DELIVERED") return "chip green";
  if (status === "CANCELLED" || status === "CANCEL_REQUESTED") return "chip red";
  return "chip amber";
}

export default function Orders() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [names, setNames] = useState<Record<string, string>>({});
  const [err, setErr] = useState<string | null>(null);

  const load = () => tenco.orders().then(setOrders).catch((e) => setErr(e.message));
  useEffect(() => {
    load();
    tenco.vendors().then((vs: Vendor[]) => setNames(Object.fromEntries(vs.map((v) => [v.id, v.name])))).catch(() => {});
  }, []);

  const groups = Object.values(
    orders.reduce<Record<string, Order[]>>((acc, o) => {
      const k = o.groupId || o.id;
      (acc[k] ||= []).push(o);
      return acc;
    }, {})
  ).sort((a, b) => Math.max(...b.map((o) => o.updatedAt)) - Math.max(...a.map((o) => o.updatedAt)));

  async function setPrice(lines: Order[]) {
    const input = prompt("Unit price (₹) for this order:", "");
    if (input == null) return;
    const paise = Math.round((parseFloat(input) || 0) * 100);
    if (paise <= 0) return;
    await Promise.all(lines.map((o) => tenco.setOrderPrice(o.id, paise)));
    load();
  }
  async function advance(lines: Order[], status: string) {
    await Promise.all(lines.map((o) => tenco.setOrderStatus(o.id, status)));
    load();
  }
  async function confirmCancel(lines: Order[]) {
    await Promise.all(lines.map((o) => tenco.setOrderStatus(o.id, "CANCELLED")));
    load();
  }

  return (
    <>
      <h1 className="page-title">Orders</h1>
      <p className="page-sub">{orders.filter((o) => o.status === "PLACED").length} new</p>
      {err && <div className="error">{err}</div>}

      <div className="grid" style={{ gridTemplateColumns: "repeat(auto-fill, minmax(320px, 1fr))" }}>
        {groups.map((lines) => {
          const head = lines[0];
          const priced = lines.every((l) => l.unitPricePaise != null);
          const total = lines.reduce((s, l) => s + (l.unitPricePaise || 0) * l.quantity, 0);
          return (
            <div className="card" key={head.groupId || head.id}>
              <div className="row" style={{ justifyContent: "space-between" }}>
                <strong>{names[head.vendorId] || "—"}</strong>
                <span className={chip(head.status)}>{head.status}</span>
              </div>
              <div style={{ margin: "8px 0" }}>
                {lines.map((l) => (
                  <div key={l.id} className="row" style={{ justifyContent: "space-between", color: "var(--muted)" }}>
                    <span>{l.color} {l.grade} · {l.quantity}</span>
                    {l.unitPricePaise != null && <span>{money(l.unitPricePaise * l.quantity)}</span>}
                  </div>
                ))}
              </div>
              {priced && <div style={{ fontWeight: 800, color: "var(--green)" }}>Total: {money(total)}</div>}
              <div className="row" style={{ marginTop: 12, gap: 8 }}>
                {head.status === "CANCEL_REQUESTED" ? (
                  <button className="btn" style={{ background: "var(--red)" }} onClick={() => confirmCancel(lines)}>Confirm cancellation</button>
                ) : head.status === "CANCELLED" || head.status === "DELIVERED" ? null : !priced ? (
                  <button className="btn" onClick={() => setPrice(lines)}>Set price</button>
                ) : NEXT[head.status] ? (
                  <button className="btn" onClick={() => advance(lines, NEXT[head.status])}>{NEXT_LABEL[NEXT[head.status]]}</button>
                ) : null}
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
}
