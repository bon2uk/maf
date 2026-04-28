import "server-only";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { ApiError } from "@/shared/lib/api-client";
import { NotFoundError } from "./errors";

const SESSION_COOKIE = "maf_session";
const CLEAR_SESSION_PATH = "/api/auth/logout";

function getInternalBaseUrl(): string {
  const url = process.env.INTERNAL_API_URL;
  if (!url) {
    throw new Error(
      "INTERNAL_API_URL is not set. Configure it in .env.local (e.g. http://localhost:8080/api).",
    );
  }
  return url.replace(/\/+$/, "");
}

function buildUrl(path: string): string {
  const base = getInternalBaseUrl();
  return path.startsWith("/") ? `${base}${path}` : `${base}/${path}`;
}

export type ServerFetchInit = Omit<RequestInit, "headers"> & {
  headers?: Record<string, string>;
};

/**
 * Server-side fetcher used by RSC pages and layouts.
 *
 * On any 401 response (or missing session cookie) the function does NOT throw —
 * it short-circuits with `redirect("/api/auth/logout")`. The logout route
 * handler clears the stale cookie and bounces the user to `/login`. Callers
 * therefore never have to handle the unauthenticated case explicitly: by the
 * time `serverFetch` returns, the session is known to be valid.
 *
 * Other failure modes still surface as exceptions (`NotFoundError`, `ApiError`)
 * for callers that care to differentiate them (e.g. `notFound()` on 404).
 */
export async function serverFetch<T>(path: string, init: ServerFetchInit = {}): Promise<T> {
  const token = cookies().get(SESSION_COOKIE)?.value;
  if (!token) {
    redirect(CLEAR_SESSION_PATH);
  }

  const headers: Record<string, string> = {
    Accept: "application/json",
    Authorization: `Bearer ${token}`,
    ...(init.body ? { "Content-Type": "application/json" } : {}),
    ...(init.headers ?? {}),
  };

  const response = await fetch(buildUrl(path), {
    cache: "no-store",
    ...init,
    headers,
  });

  if (response.status === 401) {
    redirect(CLEAR_SESSION_PATH);
  }
  if (response.status === 404) {
    throw new NotFoundError();
  }
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new ApiError(response.status, response.statusText, data);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}
