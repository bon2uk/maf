import { NextResponse, type NextRequest } from "next/server";

const SESSION_COOKIE = "maf_session";

export function middleware(request: NextRequest) {
  const { pathname, search } = request.nextUrl;
  const hasSession = !!request.cookies.get(SESSION_COOKIE)?.value;

  if (!hasSession) {
    const url = request.nextUrl.clone();
    url.pathname = "/login";
    url.search = `?next=${encodeURIComponent(pathname + search)}`;
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/|favicon.ico|api/auth/|login).*)"],
};
