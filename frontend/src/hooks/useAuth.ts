import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { authApi } from "@/services/authService";
import { useAuthStore } from "@/store/authStore";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import type { LoginRequest, RegisterRequest } from "@/types/auth";
import type { AxiosError } from "axios";

type ApiError = {
  message?: string;
};

export const authKeys = {
  currentUser: ["currentUser"] as const,
};

export function useLogin() {
  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (credentials: LoginRequest) => authApi.login(credentials),
    onSuccess: (data) => {
      setAuth(
        {
          userId: data.userId,
          username: data.username,
          email: data.email,
        },
        data.accessToken
      );
      toast.success("Welcome back!");
      navigate("/");
    },
    onError: (error: AxiosError<ApiError>) => {
      const message =
        error?.response?.data?.message || "Login failed. Please try again.";
      toast.error(message);
    },
  });
}

export function useRegister() {
  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: RegisterRequest) => authApi.register(data),
    onSuccess: (data) => {
      setAuth(
        {
          userId: data.userId,
          username: data.username,
          email: data.email,
        },
        data.accessToken
      );
      toast.success("Account created successfully!");
      navigate("/");
    },
    onError: (error: AxiosError<ApiError>) => {
      const message =
        error?.response?.data?.message ||
        "Registration failed. Please try again.";
      toast.error(message);
    },
  });
}

export function useLogout() {
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => authApi.logout(),
    onSuccess: () => {
      clearAuth();
      queryClient.clear();
      toast.success("Logged out successfully");
      navigate("/login");
    },
    onError: (error) => {
      console.error("Logout error:", error);
      toast.error("Logout failed");
    },
  });
}

export function useCurrentUser() {
  return useQuery({
    queryKey: authKeys.currentUser,
    queryFn: () => authApi.getCurrentUser(),
    gcTime: 5 * 60 * 1000,
    refetchOnMount: true,
  });
}
