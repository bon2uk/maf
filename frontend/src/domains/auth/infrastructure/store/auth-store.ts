import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import { AuthTokens } from "../../domain/types";

interface AuthStore {
  tokens: AuthTokens | null;
  isAuthenticated: boolean;
  isHydrated: boolean;
  setTokens: (tokens: AuthTokens) => void;
  clearTokens: () => void;
  getAccessToken: () => string | null;
  setHydrated: () => void;
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set, get) => ({
      tokens: null,
      isAuthenticated: false,
      isHydrated: false,

      setTokens: (tokens: AuthTokens) => {
        // Validate token exists before storing
        if (!tokens?.accessToken) {
          console.error("setTokens called with invalid token:", tokens);
          return;
        }

        if (typeof window !== "undefined") {
          localStorage.setItem("access_token", tokens.accessToken);
        }
        set({ tokens, isAuthenticated: true });
      },

      clearTokens: () => {
        if (typeof window !== "undefined") {
          localStorage.removeItem("access_token");
        }
        set({ tokens: null, isAuthenticated: false });
      },

      getAccessToken: () => {
        const state = get();
        return state.tokens?.accessToken ?? null;
      },

      setHydrated: () => {
        set({ isHydrated: true });
      },
    }),
    {
      name: "auth-storage",
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        tokens: state.tokens,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state, error) => {
        if (error) {
          console.error("Auth hydration error:", error);
        }

        if (state) {
          // Sync localStorage access_token with persisted state on hydration
          if (typeof window !== "undefined") {
            if (state.tokens?.accessToken) {
              localStorage.setItem("access_token", state.tokens.accessToken);
            } else {
              localStorage.removeItem("access_token");
            }
          }
          state.setHydrated();
        }
      },
    }
  )
);

// Helper to check if user should be considered authenticated
export function isUserAuthenticated(): boolean {
  const state = useAuthStore.getState();
  return state.isHydrated && state.isAuthenticated && !!state.tokens?.accessToken;
}
