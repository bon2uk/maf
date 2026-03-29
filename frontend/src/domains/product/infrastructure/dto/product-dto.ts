export interface ProductResponse {
  id: string;
  name: string;
  description: string;
  price: number;
  currency: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  currency: string;
}

export interface UpdateProductRequest {
  name?: string;
  description?: string;
  price?: number;
  currency?: string;
}

export interface PaginatedProductsResponse {
  content: ProductResponse[];
  total_elements: number;
  page: number;
  size: number;
  total_pages: number;
}
