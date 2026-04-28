import { NextResponse, type NextRequest } from "next/server";

const SESSION_COOKIE = "maf_session";

const FORWARDED_REQUEST_HEADERS = ["content-type", "accept", "accept-language"];

function getInternalBaseUrl(): string {
  const url = process.env.INTERNAL_API_URL;
  if (!url) {
    throw new Error("INTERNAL_API_URL is not set");
  }
  return url.replace(/\/+$/, "");
}

async function proxy(request: NextRequest, params: { path: string[] }) {
  const token = request.cookies.get(SESSION_COOKIE)?.value;
  if (!token) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const subpath = params.path.map(encodeURIComponent).join("/");
  const search = request.nextUrl.search ?? "";
  const targetUrl = `${getInternalBaseUrl()}/${subpath}${search}`;

  const headers: Record<string, string> = {
    Authorization: `Bearer ${token}`,
  };
  for (const name of FORWARDED_REQUEST_HEADERS) {
    const value = request.headers.get(name);
    if (value) {
      headers[name] = value;
    }
  }

  const init: RequestInit = {
    method: request.method,
    headers,
    cache: "no-store",
  };

  if (request.method !== "GET" && request.method !== "HEAD") {
    const body = await request.text();
    if (body.length > 0) {
      init.body = body;
    }
  }

  const upstream = await fetch(targetUrl, init);

  if (upstream.status === 204 || upstream.status === 205) {
    return new NextResponse(null, { status: upstream.status });
  }

  const contentType = upstream.headers.get("content-type") ?? "application/json";
  const responseHeaders = new Headers();
  responseHeaders.set("content-type", contentType);

  const text = await upstream.text();
  return new NextResponse(text, {
    status: upstream.status,
    headers: responseHeaders,
  });
}

export async function GET(request: NextRequest, ctx: { params: { path: string[] } }) {
  return proxy(request, ctx.params);
}
export async function POST(request: NextRequest, ctx: { params: { path: string[] } }) {
  return proxy(request, ctx.params);
}
export async function PUT(request: NextRequest, ctx: { params: { path: string[] } }) {
  return proxy(request, ctx.params);
}
export async function PATCH(request: NextRequest, ctx: { params: { path: string[] } }) {
  return proxy(request, ctx.params);
}
export async function DELETE(request: NextRequest, ctx: { params: { path: string[] } }) {
  return proxy(request, ctx.params);
}
