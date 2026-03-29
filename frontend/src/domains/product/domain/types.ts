export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  currency: Currency;
  status: ProductStatus;
  createdAt: Date;
  updatedAt: Date;
}

export type ProductStatus = "ACTIVE" | "INACTIVE";

export type Currency = "USD" | "EUR" | "GBP" | "PLN";

export const CURRENCIES: { value: Currency; label: string; symbol: string }[] = [
  { value: "USD", label: "US Dollar", symbol: "$" },
  { value: "EUR", label: "Euro", symbol: "€" },
  { value: "GBP", label: "British Pound", symbol: "£" },
  { value: "PLN", label: "Polish Zloty", symbol: "zł" },
];

export interface CreateProductData {
  name: string;
  description: string;
  price: number;
  currency: Currency;
}

export interface UpdateProductData extends Partial<CreateProductData> {}

export interface ProductFilters {
  search?: string;
  status?: ProductStatus;
  page?: number;
  size?: number;
}

export interface PaginatedProducts {
  items: Product[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}
