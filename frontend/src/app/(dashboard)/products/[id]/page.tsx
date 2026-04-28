import { notFound } from "next/navigation";
import { getProduct } from "@/domains/product/infrastructure/server/product-server-api";
import { ProductForm } from "@/domains/product/presentation/components/product-form";
import { PageHeader } from "@/shared/components/page-header";
import { NotFoundError } from "@/shared/api/errors";

interface EditProductPageProps {
  params: { id: string };
}

export default async function EditProductPage({ params }: EditProductPageProps) {
  let product;
  try {
    product = await getProduct(params.id);
  } catch (error) {
    if (error instanceof NotFoundError) {
      notFound();
    }
    throw error;
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <PageHeader title="Edit Product" description={`Editing "${product.name}"`} />
      <ProductForm product={product} mode="edit" />
    </div>
  );
}
