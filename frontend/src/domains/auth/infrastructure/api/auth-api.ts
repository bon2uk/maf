import { LoginCredentials } from "../../domain/types";
import { User } from "@/domains/user/domain/types";

interface LoginResponse {
  user: User;
}

export const authApi = {
  // Calls the same-origin Next.js Route Handler which proxies to auth-service
  // and stores the JWT in an HTTP-only cookie. The browser never sees the token.
  login: async (credentials: LoginCredentials): Promise<User> => {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      credentials: "same-origin",
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      const data = (await response.json().catch(() => null)) as { error?: string } | null;
      throw new Error(data?.error ?? "Login failed");
    }

    const payload = (await response.json()) as LoginResponse;
    return payload.user;
  },

  logout: async (): Promise<void> => {
    await fetch("/api/auth/logout", { method: "POST", credentials: "same-origin" });
  },
};
