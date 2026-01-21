import { apiClient } from "@/lib/axios";
import type { User } from "@/types/user";
import type { UserStatus } from "@/store/presenceStore";

interface UserResponse {
  content: User[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

interface BulkPresenceResponse {
  statuses: Record<string, UserStatus>;
}

export const userApi = {
  getUsers: async (page = 0, size = 100): Promise<User[]> => {
    const { data } = await apiClient.get<UserResponse>("/users", {
      params: { page, size },
    });
    return data.content;
  },

  getUserById: async (userId: string): Promise<User> => {
    const { data } = await apiClient.get<User>(`/users/${userId}`);
    return data;
  },

  getBulkPresence: async (
    userIds: string[],
  ): Promise<Record<string, UserStatus>> => {
    const { data } = await apiClient.post<BulkPresenceResponse>(
      "/users/presence",
      userIds,
    );
    return data.statuses;
  },
};
