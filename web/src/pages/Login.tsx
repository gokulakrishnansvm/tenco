import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, setSession } from "../lib/api";

export default function Login() {
  const nav = useNavigate();
  const [phase, setPhase] = useState<"phone" | "otp">("phone");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [code, setCode] = useState("");
  const [devOtp, setDevOtp] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function sendOtp() {
    setError(null);
    if (phone.length < 10) return setError("Enter a 10-digit phone number");
    setLoading(true);
    try {
      const r = await api.requestOtp(phone);
      setDevOtp(r.devOtp);
      if (r.devOtp) setCode(r.devOtp); // dev convenience
      setPhase("otp");
    } catch (e) {
      setError((e as Error).message || "Could not send OTP");
    } finally {
      setLoading(false);
    }
  }

  async function verify() {
    setError(null);
    setLoading(true);
    try {
      const d = await api.verifyOtp(phone, code, name.trim(), "SUPPLIER");
      setSession(d.accessToken, d.refreshToken, d.role);
      nav("/", { replace: true });
    } catch (e) {
      setError("Invalid or expired OTP");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-wrap">
      <div className="login-card">
        <div className="login-logo">TC</div>
        <h2 style={{ textAlign: "center", margin: "0 0 4px" }}>TENCO Supplier</h2>
        <p className="muted" style={{ textAlign: "center", marginTop: 0 }}>
          {phase === "phone" ? "Sign in to your dashboard" : `Enter the OTP sent to ${phone}`}
        </p>

        {phase === "phone" ? (
          <>
            <label className="field">
              <span>Your name</span>
              <input className="input" value={name} onChange={(e) => setName(e.target.value)} />
            </label>
            <label className="field">
              <span>Phone number</span>
              <input
                className="input"
                inputMode="numeric"
                value={phone}
                onChange={(e) => setPhone(e.target.value.replace(/\D/g, "").slice(0, 10))}
              />
            </label>
            <button className="btn" style={{ width: "100%" }} disabled={loading} onClick={sendOtp}>
              {loading ? "…" : "Send OTP"}
            </button>
          </>
        ) : (
          <>
            <label className="field">
              <span>OTP{devOtp ? ` (dev: ${devOtp})` : ""}</span>
              <input
                className="input"
                inputMode="numeric"
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/\D/g, "").slice(0, 6))}
              />
            </label>
            <button className="btn" style={{ width: "100%" }} disabled={loading} onClick={verify}>
              {loading ? "…" : "Verify & sign in"}
            </button>
            <button className="btn ghost" style={{ width: "100%", marginTop: 8 }} onClick={() => setPhase("phone")}>
              Back
            </button>
          </>
        )}
        {error && <div className="error">{error}</div>}
      </div>
    </div>
  );
}
