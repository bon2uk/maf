"use client";

import { useEffect, useRef, useState, useTransition } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useDebounce } from "@/shared/hooks/use-debounce";
import { ProductFilters } from "./product-filters";

export function ProductsFiltersUrl() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [isPending, startTransition] = useTransition();

  const initialSearch = searchParams.get("search") ?? "";
  const [search, setSearch] = useState(initialSearch);
  const debouncedSearch = useDebounce(search, 300);
  const isFirstRun = useRef(true);

  useEffect(() => {
    if (isFirstRun.current) {
      isFirstRun.current = false;
      return;
    }

    const params = new URLSearchParams(searchParams.toString());
    if (debouncedSearch) {
      params.set("search", debouncedSearch);
    } else {
      params.delete("search");
    }
    params.delete("page");
    const next = params.toString();
    const url = next ? `${pathname}?${next}` : pathname;

    startTransition(() => {
      router.replace(url);
    });
    // We intentionally exclude searchParams from deps to avoid replacing the
    // URL on every external navigation; debouncedSearch is the trigger.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedSearch]);

  const handleClear = () => {
    setSearch("");
  };

  return (
    <ProductFilters
      search={search}
      onSearchChange={setSearch}
      onClear={handleClear}
      isLoading={isPending}
    />
  );
}
