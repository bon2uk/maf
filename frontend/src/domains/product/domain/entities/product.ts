import { Product, ProductStatus, Currency, CURRENCIES } from "../types";

export class ProductEntity implements Product {
  constructor(
    public readonly id: string,
    public readonly name: string,
    public readonly description: string,
    public readonly price: number,
    public readonly currency: Currency,
    public readonly status: ProductStatus,
    public readonly createdAt: Date,
    public readonly updatedAt: Date
  ) {}

  get formattedPrice(): string {
    const currencyInfo = CURRENCIES.find((c) => c.value === this.currency);
    const symbol = currencyInfo?.symbol ?? "$";
    return `${symbol}${this.price.toFixed(2)}`;
  }

  isActive(): boolean {
    return this.status === "ACTIVE";
  }
}
