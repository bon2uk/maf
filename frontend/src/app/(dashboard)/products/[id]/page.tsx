"use client";

import { useProduct } from "@/domains/product/presentation/hooks/use-products";
import { ProductForm } from "@/domains/product/presentation/components/product-form";
import { PageHeader } from "@/shared/components/page-header";
import { Loading } from "@/shared/components/loading";
import { ErrorState } from "@/shared/components/error-state";

interface EditProductPageProps {
  params: { id: string };
}

export default function EditProductPage({ params }: EditProductPageProps) {
  const { id } = params;
  const { data: product, isLoading, isError, refetch } = useProduct(id);

  if (isLoading) {
    return <Loading />;
  }

  if (isError || !product) {
    return (
      <ErrorState
        title="Product not found"
        message="The product you're looking for doesn't exist or couldn't be loaded."
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <PageHeader
        title="Edit Product"
        description={`Editing "${product.name}"`}
      />
      <ProductForm product={product} mode="edit" />
    </div>
  );
}
