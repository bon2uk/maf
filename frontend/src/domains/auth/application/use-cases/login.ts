import { LoginCredentials } from "../../domain/types";
import { authApi } from "../../infrastructure/api/auth-api";
import { useAuthStore } from "../../infrastructure/store/auth-store";
import { User } from "@/domains/user/domain/types";

export async function executeLogin(credentials: LoginCredentials): Promise<User> {
  const user = await authApi.login(credentials);
  useAuthStore.getState().setUser(user);
  return user;
}

export async function executeLogout(): Promise<void> {
  await useAuthStore.getState().logout();
}
