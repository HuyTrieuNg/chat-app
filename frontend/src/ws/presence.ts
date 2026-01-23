import { Client, type StompSubscription } from "@stomp/stompjs";
import { usePresenceStore, type UserStatus } from "@/store/presenceStore";

interface PresenceMessage {
  userId: string;
  newStatus: UserStatus;
  lastSeen?: string;
}

let presenceSubscription: StompSubscription | null = null;

export function subscribeToPresence(client: Client): void {
  if (!client.connected) {
    console.error("Cannot subscribe: WebSocket not connected");
    return;
  }

  if (presenceSubscription) {
    console.log("Already subscribed to presence");
    return;
  }

  presenceSubscription = client.subscribe(
    "/topic/presence",
    (message) => {
      try {
        const data: PresenceMessage = JSON.parse(message.body);
        console.log("Presence update:", data);

        const store = usePresenceStore.getState();
        store.setUserStatus(data.userId, data.newStatus);

        if (data.lastSeen) {
          store.setUserLastSeen(data.userId, new Date(data.lastSeen));
          console.log(`User ${data.userId} lastSeen:`, data.lastSeen);
        }
      } catch (error) {
        console.error("Failed to parse presence message:", error);
      }
    },
    {
      id: "presence-subscription",
    },
  );

  console.log("Subscribed to /topic/presence");
}

export function unsubscribeFromPresence(): void {
  if (presenceSubscription) {
    presenceSubscription.unsubscribe();
    presenceSubscription = null;
    const store = usePresenceStore.getState();
    store.clearStatuses();
    store.setWsConnected(false);
    console.log("Unsubscribed from presence");
  }
}
