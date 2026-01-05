import { apiClient } from "@/lib/axios";
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  RefreshResponse,
} from "@/types/auth";
import type { User } from "@/types/user";

export const authApi = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>(
      "/auth/login",
      credentials
    );
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>("/auth/register", data);
    return response.data;
  },

  refresh: async (): Promise<RefreshResponse> => {
    const response = await apiClient.post<RefreshResponse>("/auth/refresh", {});
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post("/auth/logout", {});
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<User>("/users/me");
    return response.data;
  },

  oAuthLogin: async (provider: string): Promise<void> => {
    apiClient.get(`/oauth2/authorization/${provider}`);
  },
};
