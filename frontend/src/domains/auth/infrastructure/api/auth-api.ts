import { api } from "@/shared/lib/api-client";
import { LoginCredentials, AuthTokens } from "../../domain/types";

interface LoginResponse {
  token: string;
  refreshToken?: string;
}

interface RefreshTokenResponse {
  token: string;
  refreshToken?: string;
}

export const authApi = {
  login: async (credentials: LoginCredentials): Promise<AuthTokens> => {
    const response = await api.post<LoginResponse>(
      "/auth/login",
      {
        email: credentials.email,
        password: credentials.password,
      },
      true
    );

    return {
      accessToken: response.token,
      refreshToken: response.refreshToken,
    };
  },

  refreshToken: async (refreshToken: string): Promise<AuthTokens> => {
    const response = await api.post<RefreshTokenResponse>(
      "/auth/refresh",
      {
        refreshToken: refreshToken,
      },
      true
    );

    return {
      accessToken: response.token,
      refreshToken: response.refreshToken,
    };
  },
};
