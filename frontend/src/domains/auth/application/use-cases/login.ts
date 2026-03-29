import { LoginCredentials } from "../../domain/types";
import { Session } from "../../domain/entities/session";
import { authApi } from "../../infrastructure/api/auth-api";
import { useAuthStore } from "../../infrastructure/store/auth-store";

export async function executeLogin(credentials: LoginCredentials): Promise<Session> {
  const tokens = await authApi.login(credentials);
  const session = Session.fromTokens(tokens);
  
  useAuthStore.getState().setTokens(tokens);
  
  return session;
}

export async function executeLogout(): Promise<void> {
  useAuthStore.getState().clearTokens();
}
