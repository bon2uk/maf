"use client";

import { useQuery } from "@tanstack/react-query";
import { productApi } from "../../infrastructure/api/product-api";
import { ProductFilters } from "../../domain/types";
import { queryKeys } from "@/shared/lib/query-keys";

export function useProducts(filters: ProductFilters = {}) {
  return useQuery({
    queryKey: queryKeys.products.list(filters),
    queryFn: () => productApi.getProducts(filters),
  });
}

export function useProduct(id: string) {
  return useQuery({
    queryKey: queryKeys.products.detail(id),
    queryFn: () => productApi.getProductById(id),
    enabled: !!id,
  });
}
