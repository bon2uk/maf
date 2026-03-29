import { api } from "@/shared/lib/api-client";
import {
  Product,
  CreateProductData,
  UpdateProductData,
  ProductFilters,
  PaginatedProducts,
} from "../../domain/types";
import { ProductResponse } from "../dto/product-dto";
import { productMapper } from "../../application/mappers/product-mapper";

export const productApi = {
  getProducts: async (filters: ProductFilters = {}): Promise<PaginatedProducts> => {
    const response = await api.get<ProductResponse[]>("/products/list");

    let items = response.map(productMapper.toDomain);

    if (filters.search) {
      const search = filters.search.toLowerCase();
      items = items.filter(
        (p) => p.name.toLowerCase().includes(search) || p.description.toLowerCase().includes(search)
      );
    }

    if (filters.status) {
      items = items.filter((p) => p.status === filters.status);
    }

    const page = filters.page ?? 0;
    const size = filters.size ?? 10;
    const total = items.length;
    const totalPages = Math.ceil(total / size);
    const paginatedItems = items.slice(page * size, (page + 1) * size);

    return {
      items: paginatedItems,
      total,
      page,
      size,
      totalPages,
    };
  },

  getProductById: async (id: string): Promise<Product> => {
    const response = await api.get<ProductResponse>(`/products/${id}`);
    return productMapper.toDomain(response);
  },

  createProduct: async (data: CreateProductData): Promise<Product> => {
    const request = productMapper.toCreateRequest(data);
    const response = await api.post<ProductResponse>("/products/create", request);
    return productMapper.toDomain(response);
  },

  updateProduct: async (id: string, data: UpdateProductData): Promise<Product> => {
    const request = productMapper.toUpdateRequest(data);
    const response = await api.patch<ProductResponse>(`/products/${id}`, request);
    return productMapper.toDomain(response);
  },

  deleteProduct: async (id: string): Promise<void> => {
    await api.delete(`/products/${id}`);
  },
};
