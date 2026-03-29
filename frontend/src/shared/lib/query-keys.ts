export const queryKeys = {
  auth: {
    all: ["auth"] as const,
    currentUser: () => [...queryKeys.auth.all, "currentUser"] as const,
  },
  users: {
    all: ["users"] as const,
    me: () => [...queryKeys.users.all, "me"] as const,
  },
  products: {
    all: ["products"] as const,
    list: (filters?: { search?: string; page?: number; size?: number }) =>
      [...queryKeys.products.all, "list", filters] as const,
    detail: (id: string) => [...queryKeys.products.all, "detail", id] as const,
  },
} as const;
