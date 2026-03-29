import { ProductEntity } from "../../domain/entities/product";
import {
  Product,
  ProductStatus,
  Currency,
  CreateProductData,
  UpdateProductData,
  PaginatedProducts,
} from "../../domain/types";
import {
  ProductResponse,
  CreateProductRequest,
  UpdateProductRequest,
  PaginatedProductsResponse,
} from "../../infrastructure/dto/product-dto";

export const productMapper = {
  toDomain(dto: ProductResponse): Product {
    return new ProductEntity(
      dto.id,
      dto.name,
      dto.description,
      dto.price,
      dto.currency as Currency,
      dto.status as ProductStatus,
      new Date(dto.createdAt),
      new Date(dto.updatedAt)
    );
  },

  toPaginatedDomain(dto: PaginatedProductsResponse): PaginatedProducts {
    return {
      items: dto.content.map(productMapper.toDomain),
      total: dto.total_elements,
      page: dto.page,
      size: dto.size,
      totalPages: dto.total_pages,
    };
  },

  toCreateRequest(data: CreateProductData): CreateProductRequest {
    return {
      name: data.name,
      description: data.description,
      price: data.price,
      currency: data.currency,
    };
  },

  toUpdateRequest(data: UpdateProductData): UpdateProductRequest {
    return {
      name: data.name,
      description: data.description,
      price: data.price,
      currency: data.currency,
    };
  },
};
