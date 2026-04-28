"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter, useSearchParams } from "next/navigation";
import { useCallback } from "react";
import { LoginCredentials } from "../../domain/types";
import { executeLogin, executeLogout } from "../../application/use-cases/login";
import { useAuthStore } from "../../infrastructure/store/auth-store";
import { toast } from "@/shared/hooks/use-toast";

export function useAuth() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);

  const loginMutation = useMutation({
    mutationFn: (credentials: LoginCredentials) => executeLogin(credentials),
    onSuccess: () => {
      toast({
        title: "Login successful",
        description: "Welcome back!",
      });
      const next = searchParams.get("next") ?? "/";
      router.replace(next);
    },
    onError: (error: Error) => {
      toast({
        variant: "destructive",
        title: "Login failed",
        description: error.message || "Invalid credentials",
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
    user,
    login: loginMutation.mutate,
    loginAsync: loginMutation.mutateAsync,
    logout,
    isLoggingIn: loginMutation.isPending,
    loginError: loginMutation.error,
  };
}
