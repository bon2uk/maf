"use client";

import { ProductForm } from "@/domains/product/presentation/components/product-form";
import { PageHeader } from "@/shared/components/page-header";

export default function NewProductPage() {
  return (
    <div className="space-y-6 max-w-2xl">
      <PageHeader title="Create Product" description="Add a new product to your inventory" />
      <ProductForm mode="create" />
    </div>
  );
}
