// Thin fetch wrapper for the TENCO backend, with JWT auth + one-shot refresh.
const ACCESS = "tenco_access";
const REFRESH = "tenco_refresh";
const ROLE = "tenco_role";

export function getAccess(): string | null {
  return localStorage.getItem(ACCESS);
}
export function getRole(): string | null {
  return localStorage.getItem(ROLE);
}
export function isLoggedIn(): boolean {
  return !!getAccess();
}
export function setSession(accessToken: string, refreshToken: string, role: string) {
  localStorage.setItem(ACCESS, accessToken);
  localStorage.setItem(REFRESH, refreshToken);
  localStorage.setItem(ROLE, role);
}
export function clearSession() {
  localStorage.removeItem(ACCESS);
  localStorage.removeItem(REFRESH);
  localStorage.removeItem(ROLE);
}

async function tryRefresh(): Promise<boolean> {
  const r = localStorage.getItem(REFRESH);
  if (!r) return false;
  try {
    const res = await fetch("/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken: r }),
    });
    if (!res.ok) return false;
    const d = await res.json();
    setSession(d.accessToken, d.refreshToken, d.role);
    return true;
  } catch {
    return false;
  }
}

async function raw<T>(path: string, opts: RequestInit = {}, retry = true): Promise<T> {
  const headers: Record<string, string> = { ...(opts.headers as Record<string, string>) };
  if (opts.body && !headers["Content-Type"]) headers["Content-Type"] = "application/json";
  const token = getAccess();
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(path, { ...opts, headers });
  if (res.status === 401 && retry) {
    if (await tryRefresh()) return raw<T>(path, opts, false);
    clearSession();
    throw new Error("Session expired");
  }
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(text || `${res.status} ${res.statusText}`);
  }
  const ct = res.headers.get("content-type") || "";
  return (ct.includes("application/json") ? res.json() : res.text()) as Promise<T>;
}

export const api = {
  get: <T,>(p: string) => raw<T>(p),
  post: <T,>(p: string, body?: unknown) => raw<T>(p, { method: "POST", body: body ? JSON.stringify(body) : undefined }),
  put: <T,>(p: string, body?: unknown) => raw<T>(p, { method: "PUT", body: body ? JSON.stringify(body) : undefined }),

  requestOtp: (phone: string) =>
    raw<{ sent: boolean; devOtp: string | null }>("/auth/otp/request", {
      method: "POST",
      body: JSON.stringify({ phone }),
    }),
  verifyOtp: (phone: string, code: string, name?: string, role = "SUPPLIER") =>
    raw<{ userId: string; role: string; accessToken: string; refreshToken: string }>("/auth/otp/verify", {
      method: "POST",
      body: JSON.stringify({ phone, code, name, role }),
    }),
};
