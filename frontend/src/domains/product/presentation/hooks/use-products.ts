"use client";

import { useQuery } from "@tanstack/react-query";
import { productApi } from "../../infrastructure/api/product-api";
import { ProductFilters } from "../../domain/types";
import { queryKeys } from "@/shared/lib/query-keys";
import { useAuthStore } from "@/domains/auth/infrastructure/store/auth-store";

export function useProducts(filters: ProductFilters = {}) {
  const { isAuthenticated, isHydrated } = useAuthStore();

  return useQuery({
    queryKey: queryKeys.products.list(filters),
    queryFn: () => productApi.getProducts(filters),
    enabled: isAuthenticated && isHydrated,
  });
}

export function useProduct(id: string) {
  const { isAuthenticated, isHydrated } = useAuthStore();

  return useQuery({
    queryKey: queryKeys.products.detail(id),
    queryFn: () => productApi.getProductById(id),
    enabled: !!id && isAuthenticated && isHydrated,
  });
}
