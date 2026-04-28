import "server-only";

import { serverFetch } from "@/shared/api/server";
import { PaginatedProducts, Product, ProductFilters } from "../../domain/types";
import { ProductResponse } from "../dto/product-dto";
import { productMapper } from "../../application/mappers/product-mapper";

export async function getProducts(filters: ProductFilters = {}): Promise<PaginatedProducts> {
  const response = await serverFetch<ProductResponse[]>("/products/list");

  let items = response.map((dto) => productMapper.toPlain(productMapper.toDomain(dto)));

  if (filters.search) {
    const search = filters.search.toLowerCase();
    items = items.filter(
      (p) =>
        p.name.toLowerCase().includes(search) || p.description.toLowerCase().includes(search),
    );
  }

  if (filters.status) {
    items = items.filter((p) => p.status === filters.status);
  }

  const page = filters.page ?? 0;
  const size = filters.size ?? 10;
  const total = items.length;
  const totalPages = Math.max(1, Math.ceil(total / size));
  const paginatedItems = items.slice(page * size, (page + 1) * size);

  return {
    items: paginatedItems,
    total,
    page,
    size,
    totalPages,
  };
}

export async function getProduct(id: string): Promise<Product> {
  const dto = await serverFetch<ProductResponse>(`/products/${encodeURIComponent(id)}`);
  return productMapper.toPlain(productMapper.toDomain(dto));
}
