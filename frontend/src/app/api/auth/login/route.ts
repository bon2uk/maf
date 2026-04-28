import { NextResponse, type NextRequest } from "next/server";

const SESSION_COOKIE = "maf_session";
const DEFAULT_TTL_SECONDS = Number(process.env.JWT_EXPIRATION_MS ?? 3600_000) / 1000;

interface LoginBody {
  email?: string;
  password?: string;
}

interface BackendLoginResponse {
  token: string;
  refreshToken?: string;
}

function getInternalBaseUrl(): string {
  const url = process.env.INTERNAL_API_URL;
  if (!url) {
    throw new Error("INTERNAL_API_URL is not set");
  }
  return url.replace(/\/+$/, "");
}

export async function POST(request: NextRequest) {
  let body: LoginBody;
  try {
    body = (await request.json()) as LoginBody;
  } catch {
    return NextResponse.json({ error: "Invalid JSON body" }, { status: 400 });
  }
  if (!body.email || !body.password) {
    return NextResponse.json({ error: "Email and password are required" }, { status: 400 });
  }

  const baseUrl = getInternalBaseUrl();

  const loginRes = await fetch(`${baseUrl}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify({ email: body.email, password: body.password }),
    cache: "no-store",
  });

  if (loginRes.status === 401) {
    return NextResponse.json({ error: "Invalid credentials" }, { status: 401 });
  }
  if (!loginRes.ok) {
    return NextResponse.json({ error: "Authentication failed" }, { status: loginRes.status });
  }

  const loginPayload = (await loginRes.json()) as BackendLoginResponse;
  const token = loginPayload.token;
  if (!token) {
    return NextResponse.json({ error: "Auth service returned no token" }, { status: 502 });
  }

  const meRes = await fetch(`${baseUrl}/users/me`, {
    method: "GET",
    headers: { Accept: "application/json", Authorization: `Bearer ${token}` },
    cache: "no-store",
  });

  if (!meRes.ok) {
    return NextResponse.json({ error: "Could not load user profile" }, { status: 502 });
  }
  const user = await meRes.json();

  const isProd = process.env.NODE_ENV === "production";
  const response = NextResponse.json({ user }, { status: 200 });
  response.cookies.set({
    name: SESSION_COOKIE,
    value: token,
    httpOnly: true,
    sameSite: "lax",
    secure: isProd,
    path: "/",
    maxAge: Math.max(60, Math.floor(DEFAULT_TTL_SECONDS)),
  });
  return response;
}
