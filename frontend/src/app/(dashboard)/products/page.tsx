"use client";

import { useState, useMemo } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useProducts } from "@/domains/product/presentation/hooks/use-products";
import { ProductTable } from "@/domains/product/presentation/components/product-table";
import { ProductFilters } from "@/domains/product/presentation/components/product-filters";
import { Pagination } from "@/domains/product/presentation/components/pagination";
import { PageHeader } from "@/shared/components/page-header";
import { Loading } from "@/shared/components/loading";
import { EmptyState } from "@/shared/components/empty-state";
import { ErrorState } from "@/shared/components/error-state";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Plus, Package } from "lucide-react";
import { useDebounce } from "@/shared/hooks/use-debounce";

export default function ProductsPage() {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);

  const debouncedSearch = useDebounce(search, 300);

  const filters = useMemo(
    () => ({
      search: debouncedSearch || undefined,
      page,
      size: 10,
    }),
    [debouncedSearch, page]
  );

  const { data, isLoading, isError, refetch } = useProducts(filters);

  const handleClearFilters = () => {
    setSearch("");
    setPage(0);
  };

  const handleSearchChange = (value: string) => {
    setSearch(value);
    setPage(0);
  };

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
            <ProductFilters
              search={search}
              onSearchChange={handleSearchChange}
              onClear={handleClearFilters}
            />

            {isLoading ? (
              <Loading />
            ) : isError ? (
              <ErrorState
                title="Failed to load products"
                message="We couldn't load the products. Please try again."
                onRetry={() => refetch()}
              />
            ) : data?.items.length === 0 ? (
              <EmptyState
                icon={Package}
                title="No products found"
                description={
                  search
                    ? "No products match your current filters. Try adjusting your search criteria."
                    : "Get started by creating your first product."
                }
                action={
                  !search
                    ? {
                        label: "Add Product",
                        onClick: () => router.push("/products/new"),
                      }
                    : undefined
                }
              />
            ) : (
              <>
                <ProductTable products={data?.items || []} />
                {data && data.totalPages > 1 && (
                  <Pagination
                    currentPage={data.page}
                    totalPages={data.totalPages}
                    total={data.total}
                    size={data.size}
                    onPageChange={setPage}
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
