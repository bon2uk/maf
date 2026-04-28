import { NextResponse, type NextRequest } from "next/server";

const SESSION_COOKIE = "maf_session";

function clearSessionCookie(response: NextResponse): NextResponse {
  response.cookies.set({
    name: SESSION_COOKIE,
    value: "",
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: 0,
  });
  return response;
}

export async function POST() {
  return clearSessionCookie(new NextResponse(null, { status: 204 }));
}

export async function GET(request: NextRequest) {
  const loginUrl = new URL("/login", request.url);
  return clearSessionCookie(NextResponse.redirect(loginUrl));
}
