import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { authApi } from "@/services/authService";
import { useAuthStore } from "@/store/authStore";

let stompClient: Client | null = null;

const getBaseUrl = () => {
  const envUrl = import.meta.env.VITE_API_BASE_URL;
  if (envUrl) {
    return envUrl.replace(/\/api$/, "");
  }
  return "http://localhost:8080";
};

const WS_ENDPOINT = getBaseUrl() + "/ws";

export function createWsClient(
  token: string,
  onConnectCallback?: () => void,
  onReconnectCallback?: () => void,
): Client {
  if (stompClient?.active) {
    console.log("WebSocket already connected");
    return stompClient;
  }

  console.log(
    "Creating new WebSocket client with token:",
    token ? "✓ Token present" : "✗ No token",
  );

  stompClient = new Client({
    webSocketFactory: () => new SockJS(WS_ENDPOINT),

    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },

    debug: (str) => {
      console.log("[STOMP]", str);
    },

    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,

    onConnect: () => {
      console.log("✅ WebSocket connected successfully");
      onConnectCallback?.();
    },

    onDisconnect: () => {
      console.log("🔌 WebSocket disconnected");
    },

    onStompError: async (frame) => {
      const errorMessage = frame.headers["message"] || "";
      console.error("STOMP error:", errorMessage);
      console.error("Details:", frame.body);

      if (
        errorMessage.includes("ExecutorSubscribableChannel") ||
        errorMessage.includes("Authentication") ||
        errorMessage.includes("Unauthorized") ||
        errorMessage.includes("Access Denied")
      ) {
        console.log("🔄 Token may have expired, attempting refresh...");
        await handleTokenRefreshAndReconnect(onReconnectCallback);
      }
    },

    onWebSocketError: (event) => {
      console.error("WebSocket error:", event);
    },
  });

  return stompClient;
}

export function getWsClient(): Client | null {
  return stompClient;
}

export function disconnectWsClient(): void {
  if (stompClient?.active) {
    stompClient.deactivate();
    stompClient = null;
    console.log("WebSocket client deactivated");
  }
}

async function handleTokenRefreshAndReconnect(
  onReconnectCallback?: () => void,
): Promise<void> {
  try {
    console.log("Attempting to refresh access token...");
    const response = await authApi.refresh();
    const newToken = response.accessToken;

    if (!newToken) {
      throw new Error("No access token received");
    }

    // Update token in store
    useAuthStore.getState().updateAccessToken(newToken);
    console.log("✅ Token refreshed successfully");

    // Reconnect WebSocket with new token
    if (stompClient) {
      console.log("Disconnecting old WebSocket connection...");
      stompClient.deactivate();
      stompClient = null;
    }

    // Trigger reconnection callback to re-initialize WebSocket
    console.log("Triggering WebSocket reconnection with new token...");
    onReconnectCallback?.();
  } catch (error) {
    console.error("❌ Failed to refresh token:", error);

    // Clear auth and redirect to login
    useAuthStore.getState().clearAuth();

    if (!window.location.pathname.includes("/login")) {
      console.log("Redirecting to login page...");
      window.location.href = "/login";
    }
  }
}

export function reconnectWsClient(
  token: string,
  onConnectCallback?: () => void,
  onReconnectCallback?: () => void,
): Client {
  if (stompClient?.active) {
    stompClient.deactivate();
    stompClient = null;
  }

  const client = createWsClient(token, onConnectCallback, onReconnectCallback);
  client.activate();
  return client;
}
