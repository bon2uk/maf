"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useCallback } from "react";
import { LoginCredentials } from "../../domain/types";
import { executeLogin, executeLogout } from "../../application/use-cases/login";
import { useAuthStore } from "../../infrastructure/store/auth-store";
import { toast } from "@/shared/hooks/use-toast";
import { ApiError } from "@/shared/lib/api-client";

export function useAuth() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { isAuthenticated, isHydrated } = useAuthStore();

  const loginMutation = useMutation({
    mutationFn: (credentials: LoginCredentials) => executeLogin(credentials),
    onSuccess: () => {
      toast({
        title: "Login successful",
        description: "Welcome back!",
      });
      // Navigate first, then queries will be enabled on the dashboard
      router.replace("/");
    },
    onError: (error: Error) => {
      const message =
        error instanceof ApiError && error.data
          ? (error.data as { message?: string })?.message || error.message
          : error.message || "Invalid credentials";

      toast({
        variant: "destructive",
        title: "Login failed",
        description: message,
      });
    },
  });

  const logout = useCallback(async () => {
    await executeLogout();
    queryClient.clear();
    router.replace("/login");

    toast({
      title: "Logged out",
      description: "You have been logged out successfully.",
    });
  }, [queryClient, router]);

  return {
    isAuthenticated,
    isHydrated,
    login: loginMutation.mutate,
    loginAsync: loginMutation.mutateAsync,
    logout,
    isLoggingIn: loginMutation.isPending,
    loginError: loginMutation.error,
  };
}
