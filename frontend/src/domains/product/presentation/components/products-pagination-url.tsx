"use client";

import { useTransition } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Pagination } from "./pagination";

interface ProductsPaginationUrlProps {
  currentPage: number;
  totalPages: number;
  total: number;
  size: number;
}

export function ProductsPaginationUrl({
  currentPage,
  totalPages,
  total,
  size,
}: ProductsPaginationUrlProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [isPending, startTransition] = useTransition();

  const goToPage = (page: number) => {
    const params = new URLSearchParams(searchParams.toString());
    if (page === 0) {
      params.delete("page");
    } else {
      params.set("page", String(page));
    }
    const next = params.toString();
    const url = next ? `${pathname}?${next}` : pathname;

    startTransition(() => {
      router.push(url);
    });
  };

  return (
    <Pagination
      currentPage={currentPage}
      totalPages={totalPages}
      total={total}
      size={size}
      onPageChange={goToPage}
      isLoading={isPending}
    />
  );
}
