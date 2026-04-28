import Link from "next/link";
import { Package, Plus } from "lucide-react";
import { getProducts } from "@/domains/product/infrastructure/server/product-server-api";
import { ProductTable } from "@/domains/product/presentation/components/product-table";
import { ProductsFiltersUrl } from "@/domains/product/presentation/components/products-filters-url";
import { ProductsPaginationUrl } from "@/domains/product/presentation/components/products-pagination-url";
import { PageHeader } from "@/shared/components/page-header";
import { EmptyState } from "@/shared/components/empty-state";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

interface ProductsPageProps {
  searchParams: {
    search?: string;
    page?: string;
    size?: string;
  };
}

function parsePage(raw: string | undefined): number {
  const parsed = Number(raw);
  if (!Number.isFinite(parsed) || parsed < 0) return 0;
  return Math.floor(parsed);
}

function parseSize(raw: string | undefined): number {
  const parsed = Number(raw);
  if (!Number.isFinite(parsed) || parsed <= 0) return 10;
  return Math.min(100, Math.floor(parsed));
}

export default async function ProductsPage({ searchParams }: ProductsPageProps) {
  const search = searchParams.search?.trim() || undefined;
  const page = parsePage(searchParams.page);
  const size = parseSize(searchParams.size);

  const data = await getProducts({ search, page, size });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Products"
        description="Manage your product inventory"
        actions={
          <Button asChild>
            <Link href="/products/new">
              <Plus className="mr-2 h-4 w-4" />
              Add Product
            </Link>
          </Button>
        }
      />

      <Card>
        <CardContent className="pt-6">
          <div className="space-y-4">
            <ProductsFiltersUrl />

            {data.items.length === 0 ? (
              <EmptyState
                icon={Package}
                title="No products found"
                description={
                  search
                    ? "No products match your current filters. Try adjusting your search criteria."
                    : "Get started by creating your first product."
                }
              />
            ) : (
              <>
                <ProductTable products={data.items} />
                {data.totalPages > 1 && (
                  <ProductsPaginationUrl
                    currentPage={data.page}
                    totalPages={data.totalPages}
                    total={data.total}
                    size={data.size}
                  />
                )}
              </>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
