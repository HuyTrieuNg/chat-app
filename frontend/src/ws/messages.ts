import { Client } from "@stomp/stompjs";
import { getWsClient } from "@/lib/wsClient";
import type { Message } from "@/types/message";

let messageSubscriptions: Map<string, () => void> = new Map();

export function subscribeToConversationMessages(
  conversationId: string,
  callback: (message: Message) => void
): () => void {
  const client = getWsClient();
  if (!client || !client.connected) {
    console.warn("WebSocket not connected, cannot subscribe to messages");
    return () => {};
  }

  const destination = `/topic/conversations/${conversationId}/messages`;
  const subscription = client.subscribe(destination, (message) => {
    try {
      const data: Message = JSON.parse(message.body);
      callback(data);
    } catch (error) {
      console.error("Error parsing message:", error);
    }
  });

  const unsubscribe = () => {
    subscription.unsubscribe();
    messageSubscriptions.delete(conversationId);
  };

  messageSubscriptions.set(conversationId, unsubscribe);
  console.log(`Subscribed to messages for conversation ${conversationId}`);

  return unsubscribe;
}

export function unsubscribeFromConversationMessages(
  conversationId: string
): void {
  const unsubscribe = messageSubscriptions.get(conversationId);
  if (unsubscribe) {
    unsubscribe();
  }
}

export function sendMessageViaWebSocket(
  conversationId: string,
  content: string,
  type: string = "TEXT",
  replyToMessageId?: string | null
): void {
  const client = getWsClient();
  if (!client || !client.connected) {
    console.warn("WebSocket not connected, cannot send message");
    return;
  }

  const destination = "/app/chat/send";
  const payload = {
    conversationId,
    content,
    type,
    replyToMessageId: replyToMessageId || null,
  };

  client.publish({
    destination,
    body: JSON.stringify(payload),
  });

  console.log(`Sent message via WebSocket to conversation ${conversationId}`);
}

