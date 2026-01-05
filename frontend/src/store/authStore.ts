import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { User } from "@/types/user";
import { apiClient } from "@/lib/axios";

interface AuthState {
  user: User | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  setAuth: (user: User, accessToken: string) => void;
  clearAuth: () => void;
  updateAccessToken: (accessToken: string) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      isAuthenticated: false,

      setAuth: (user: User | null, accessToken: string) => {
        set({ user, accessToken, isAuthenticated: true });
        apiClient.defaults.headers.common[
          "Authorization"
        ] = `Bearer ${accessToken}`;
      },

      clearAuth: () => {
        set({ user: null, accessToken: null, isAuthenticated: false });
        delete apiClient.defaults.headers.common["Authorization"];
      },

      updateAccessToken: (accessToken) => {
        set({ accessToken, isAuthenticated: !!accessToken });
        if (accessToken) {
          apiClient.defaults.headers.common[
            "Authorization"
          ] = `Bearer ${accessToken}`;
        } else {
          delete apiClient.defaults.headers.common["Authorization"];
        }
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        user: state.user,
      }),
    }
  )
);
