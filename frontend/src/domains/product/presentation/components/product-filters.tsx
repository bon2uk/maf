"use client";

import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Loader2, Search, X } from "lucide-react";

interface ProductFiltersProps {
  search: string;
  onSearchChange: (value: string) => void;
  onClear: () => void;
  isLoading?: boolean;
}

export function ProductFilters({
  search,
  onSearchChange,
  onClear,
  isLoading = false,
}: ProductFiltersProps) {
  const hasFilters = !!search;

  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
      <div className="relative flex-1 max-w-sm">
        {isLoading ? (
          <Loader2 className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground animate-spin" />
        ) : (
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        )}
        <Input
          placeholder="Search products..."
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          className="pl-9"
        />
      </div>
      {hasFilters && (
        <Button variant="ghost" size="sm" onClick={onClear} disabled={isLoading}>
          <X className="mr-2 h-4 w-4" />
          Clear filters
        </Button>
      )}
    </div>
  );
}
