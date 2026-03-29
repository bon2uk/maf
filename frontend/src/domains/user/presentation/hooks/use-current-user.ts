"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { userApi } from "../../infrastructure/api/user-api";
import { UpdateUserData } from "../../domain/types";
import { queryKeys } from "@/shared/lib/query-keys";
import { useAuthStore } from "@/domains/auth/infrastructure/store/auth-store";
import { toast } from "@/shared/hooks/use-toast";

export function useCurrentUser() {
  const { isAuthenticated, isHydrated } = useAuthStore();

  return useQuery({
    queryKey: queryKeys.users.me(),
    queryFn: userApi.getCurrentUser,
    enabled: isAuthenticated && isHydrated,
    staleTime: 5 * 60 * 1000,
  });
}

export function useUpdateCurrentUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UpdateUserData) => userApi.updateCurrentUser(data),
    onSuccess: (updatedUser) => {
      queryClient.setQueryData(queryKeys.users.me(), updatedUser);
      toast({
        title: "Profile updated",
        description: "Your profile has been updated successfully.",
      });
    },
    onError: (error: Error) => {
      toast({
        variant: "destructive",
        title: "Update failed",
        description: error.message || "Failed to update profile",
      });
    },
  });
}
