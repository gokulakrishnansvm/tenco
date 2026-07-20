import { Navigate, Route, Routes } from "react-router-dom";
import { isLoggedIn } from "./lib/api";
import Login from "./pages/Login";
import Shell from "./components/Shell";
import Dashboard from "./pages/Dashboard";
import Vendors from "./pages/Vendors";
import Dealers from "./pages/Dealers";
import Complaints from "./pages/Complaints";
import Orders from "./pages/Orders";
import Approvals from "./pages/Approvals";
import Pricing from "./pages/Pricing";
import Transactions from "./pages/Transactions";

function Protected({ children }: { children: JSX.Element }) {
  return isLoggedIn() ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <Protected>
            <Shell />
          </Protected>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="orders" element={<Orders />} />
        <Route path="approvals" element={<Approvals />} />
        <Route path="vendors" element={<Vendors />} />
        <Route path="pricing" element={<Pricing />} />
        <Route path="dealers" element={<Dealers />} />
        <Route path="transactions" element={<Transactions />} />
        <Route path="complaints" element={<Complaints />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
