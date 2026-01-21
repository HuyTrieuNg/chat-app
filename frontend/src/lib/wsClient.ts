import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

let stompClient: Client | null = null;

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL as string;
const WS_ENDPOINT = API_BASE_URL + "/ws";

export function createWsClient(
  token: string,
  onConnectCallback?: () => void,
): Client {
  if (stompClient?.active) {
    console.log("WebSocket already connected");
    return stompClient;
  }

  stompClient = new Client({
    webSocketFactory: () => new SockJS(WS_ENDPOINT),

    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },

    debug: (str) => {
      console.log("[STOMP]", str);
    },

    // reconnectDelay: 5000,
    // heartbeatIncoming: 4000,
    // heartbeatOutgoing: 4000,

    onConnect: () => {
      console.log("WebSocket connected");
      onConnectCallback?.();
    },

    onDisconnect: () => {
      console.log("WebSocket disconnected");
    },

    onStompError: (frame) => {
      console.error("STOMP error:", frame.headers["message"]);
      console.error("Details:", frame.body);
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
