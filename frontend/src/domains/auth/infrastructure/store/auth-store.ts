import { create } from "zustand";
import { User } from "@/domains/user/domain/types";

interface AuthStore {
  user: User | null;
  setUser: (user: User | null) => void;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthStore>()((set) => ({
  user: null,

  setUser: (user) => {
    set({ user });
  },

  logout: async () => {
    try {
      await fetch("/api/auth/logout", { method: "POST", credentials: "same-origin" });
    } catch {
      // Even if the server call fails, clear local state.
    }
    set({ user: null });
  },
}));
