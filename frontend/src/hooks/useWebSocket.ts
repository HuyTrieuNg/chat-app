import { createWsClient, disconnectWsClient } from "@/lib/wsClient";
import { useAuthStore } from "@/store/authStore";
import { subscribeToPresence, unsubscribeFromPresence } from "@/ws/presence";
import { useEffect, useRef } from "react";

export function useWebSocket() {
  const token = useAuthStore((state) => state.accessToken);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const clientRef = useRef<ReturnType<typeof createWsClient> | null>(null);

  useEffect(() => {
    if (!isAuthenticated || !token) {
      if (clientRef.current) {
        unsubscribeFromPresence();
        disconnectWsClient();
        clientRef.current = null;
      }
      return;
    }

    if (clientRef.current?.active) return;

    const client = createWsClient(token, () => {
      console.log("Subscribing to presence");
      subscribeToPresence(client);
    });
    clientRef.current = client;

    client.activate();

    return () => {
      unsubscribeFromPresence();
      disconnectWsClient();
      clientRef.current = null;
    };
  }, [isAuthenticated, token]);
}
