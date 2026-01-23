import { useQuery } from "@tanstack/react-query";
import { userApi } from "@/services/userService";
import { usePresenceStore } from "@/store/presenceStore";
import { useEffect } from "react";

export function useUsers() {
  const query = useQuery({
    queryKey: ["users"],
    queryFn: () => userApi.getUsers(),
    staleTime: 1000 * 60 * 5, // 5 minutes
  });

  const wsConnected = usePresenceStore((state) => state.wsConnected);

  useEffect(() => {
    const fetchPresence = async () => {
      // Wait for WebSocket to be connected before fetching presence
      if (!wsConnected) {
        console.log(
          "Waiting for WebSocket connection before fetching presence...",
        );
        return;
      }

      if (query.data && query.data.length > 0) {
        try {
          const userIds = query.data.map((user) => user.userId);
          const statuses = await userApi.getBulkPresence(userIds);

          const { setUserStatus } = usePresenceStore.getState();
          Object.entries(statuses).forEach(([userId, status]) => {
            setUserStatus(userId, status);
          });
        } catch (error) {
          console.error("Failed to fetch bulk presence:", error);
        }
      }
    };

    fetchPresence();
  }, [query.data, wsConnected]);

  return query;
}

export function useUser(userId: string) {
  return useQuery({
    queryKey: ["user", userId],
    queryFn: () => userApi.getUserById(userId),
    enabled: !!userId,
    staleTime: 1000 * 60 * 5, // 5 minutes
  });
}
