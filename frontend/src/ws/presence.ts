import { Client, type StompSubscription } from "@stomp/stompjs";
import { usePresenceStore, type UserStatus } from "@/store/presenceStore";

interface PresenceMessage {
  userId: string;
  newStatus: UserStatus;
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

        usePresenceStore.getState().setUserStatus(data.userId, data.newStatus);
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
    usePresenceStore.getState().clearStatuses();
    console.log("Unsubscribed from presence");
  }
}
