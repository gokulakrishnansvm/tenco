import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { clearSession } from "../lib/api";

const links = [
  { to: "/", label: "Dashboard", icon: "📊", end: true },
  { to: "/vendors", label: "Vendors", icon: "🧑‍🌾" },
  { to: "/dealers", label: "Dealers", icon: "🚚" },
  { to: "/complaints", label: "Complaints", icon: "⚠️" },
];

export default function Shell() {
  const nav = useNavigate();
  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="dot">TC</span> TENCO
        </div>
        {links.map((l) => (
          <NavLink
            key={l.to}
            to={l.to}
            end={l.end}
            className={({ isActive }) => "navlink" + (isActive ? " active" : "")}
          >
            <span>{l.icon}</span> {l.label}
          </NavLink>
        ))}
        <div className="spacer" />
        <button
          className="logout"
          onClick={() => {
            clearSession();
            nav("/login", { replace: true });
          }}
        >
          Log out
        </button>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
