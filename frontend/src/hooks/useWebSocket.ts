import {
  createWsClient,
  disconnectWsClient,
  reconnectWsClient,
} from "@/lib/wsClient";
import { useAuthStore } from "@/store/authStore";
import { usePresenceStore } from "@/store/presenceStore";
import { subscribeToPresence, unsubscribeFromPresence } from "@/ws/presence";
import { trackUserActivity } from "@/ws/activity";
import { useEffect, useRef, useCallback } from "react";

export function useWebSocket() {
  const token = useAuthStore((state) => state.accessToken);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const clientRef = useRef<ReturnType<typeof createWsClient> | null>(null);
  const activityCleanupRef = useRef<(() => void) | null>(null);
  const onConnectCallbackRef = useRef<(() => void) | null>(null);
  const onReconnectCallbackRef = useRef<(() => void) | null>(null);

  // Callback subscribes to presence
  const handleConnect = useCallback(() => {
    console.log("WebSocket onConnect callback - Subscribing to presence");
    const client = clientRef.current;
    if (client) {
      subscribeToPresence(client);
      usePresenceStore.getState().setWsConnected(true);
    }
  }, []);

  const handleReconnect = useCallback(() => {
    console.log("WebSocket reconnection callback triggered");
    // Reconnect with new token after refresh
    const newToken = useAuthStore.getState().accessToken;
    if (newToken) {
      console.log("Reconnecting with refreshed token...");
      const newClient = reconnectWsClient(
        newToken,
        onConnectCallbackRef.current || undefined,
        onReconnectCallbackRef.current || undefined,
      );
      clientRef.current = newClient;
    }
  }, []);

  // Store callbacks in refs
  useEffect(() => {
    onConnectCallbackRef.current = handleConnect;
    onReconnectCallbackRef.current = handleReconnect;
  }, [handleConnect, handleReconnect]);

  const initializeWebSocket = useCallback(() => {
    if (!token) {
      console.log("Cannot initialize WebSocket: no token available");
      return;
    }

    console.log("Initializing WebSocket connection...");

    const client = createWsClient(token, handleConnect, handleReconnect);

    clientRef.current = client;
    client.activate();

    // Start tracking user activity if not already started
    if (!activityCleanupRef.current) {
      console.log("Starting user activity tracking...");
      activityCleanupRef.current = trackUserActivity();
    }
  }, [token, handleConnect, handleReconnect]);

  useEffect(() => {
    console.log(
      "useWebSocket effect - isAuthenticated:",
      isAuthenticated,
      "hasToken:",
      !!token,
    );

    if (!isAuthenticated || !token) {
      console.log("Not authenticated or no token, cleaning up WebSocket");
      if (clientRef.current) {
        unsubscribeFromPresence();
        disconnectWsClient();
        clientRef.current = null;
      }
      if (activityCleanupRef.current) {
        activityCleanupRef.current();
        activityCleanupRef.current = null;
      }
      usePresenceStore.getState().setWsConnected(false);
      return;
    }

    if (clientRef.current?.active) {
      console.log("WebSocket client already active, skipping initialization");
      return;
    }

    initializeWebSocket();

    return () => {
      console.log("useWebSocket cleanup - Disconnecting...");
      if (activityCleanupRef.current) {
        activityCleanupRef.current();
        activityCleanupRef.current = null;
      }
      unsubscribeFromPresence();
      disconnectWsClient();
      clientRef.current = null;
    };
  }, [isAuthenticated, token, initializeWebSocket]);
}
