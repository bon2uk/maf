// Same-origin path. Browser requests go to /api/<path>, which is handled by
// Next.js Route Handlers (catch-all proxy in app/api/[...path]/route.ts and the
// dedicated /api/auth/* handlers). The HTTP-only session cookie is sent
// automatically; this client never touches the JWT itself.
const API_BASE_URL = "/api";

export class ApiError extends Error {
  constructor(
    public status: number,
    public statusText: string,
    public data?: unknown
  ) {
    super(`API Error: ${status} ${statusText}`);
    this.name = "ApiError";
  }

  isUnauthorized(): boolean {
    return this.status === 401;
  }
}

type RequestConfig = {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  headers?: Record<string, string>;
  params?: Record<string, string | number | boolean | undefined>;
  // Kept for source-compatibility with existing callers; ignored. Auth is
  // carried by the HTTP-only `maf_session` cookie attached automatically.
  skipAuth?: boolean;
};

function buildUrl(
  endpoint: string,
  params?: Record<string, string | number | boolean | undefined>
): string {
  const path = endpoint.startsWith("/") ? endpoint : `/${endpoint}`;
  const url = new URL(`${API_BASE_URL}${path}`, "http://placeholder.local");

  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        url.searchParams.append(key, String(value));
      }
    });
  }

  return `${url.pathname}${url.search}`;
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new ApiError(response.status, response.statusText, data);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

export async function apiClient<T>(endpoint: string, config: RequestConfig = {}): Promise<T> {
  const { method = "GET", body, headers = {}, params } = config;

  const requestHeaders: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "application/json",
    ...headers,
  };

  const response = await fetch(buildUrl(endpoint, params), {
    method,
    headers: requestHeaders,
    credentials: "same-origin",
    body: body ? JSON.stringify(body) : undefined,
  });

  return handleResponse<T>(response);
}

export const api = {
  get: <T>(endpoint: string, params?: Record<string, string | number | boolean | undefined>) =>
    apiClient<T>(endpoint, { method: "GET", params }),

  post: <T>(endpoint: string, body?: unknown, skipAuth?: boolean) =>
    apiClient<T>(endpoint, { method: "POST", body, skipAuth }),

  put: <T>(endpoint: string, body?: unknown) => apiClient<T>(endpoint, { method: "PUT", body }),

  patch: <T>(endpoint: string, body?: unknown) => apiClient<T>(endpoint, { method: "PATCH", body }),

  delete: <T>(endpoint: string) => apiClient<T>(endpoint, { method: "DELETE" }),
};
