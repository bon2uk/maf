"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { productApi } from "../../infrastructure/api/product-api";
import { CreateProductData, UpdateProductData } from "../../domain/types";
import { queryKeys } from "@/shared/lib/query-keys";
import { toast } from "@/shared/hooks/use-toast";

export function useCreateProduct() {
  const queryClient = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: (data: CreateProductData) => productApi.createProduct(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.products.all });
      toast({
        title: "Product created",
        description: "The product has been created successfully.",
      });
      router.push("/products");
    },
    onError: (error: Error) => {
      toast({
        variant: "destructive",
        title: "Failed to create product",
        description: error.message || "Something went wrong",
      });
    },
  });
}

export function useUpdateProduct(id: string) {
  const queryClient = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: (data: UpdateProductData) => productApi.updateProduct(id, data),
    onSuccess: (updatedProduct) => {
      queryClient.setQueryData(queryKeys.products.detail(id), updatedProduct);
      queryClient.invalidateQueries({ queryKey: queryKeys.products.list() });
      toast({
        title: "Product updated",
        description: "The product has been updated successfully.",
      });
      router.push("/products");
    },
    onError: (error: Error) => {
      toast({
        variant: "destructive",
        title: "Failed to update product",
        description: error.message || "Something went wrong",
      });
    },
  });
}

export function useDeleteProduct() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => productApi.deleteProduct(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.products.all });
      toast({
        title: "Product deleted",
        description: "The product has been deleted successfully.",
      });
    },
    onError: (error: Error) => {
      toast({
        variant: "destructive",
        title: "Failed to delete product",
        description: error.message || "Something went wrong",
      });
    },
  });
}
