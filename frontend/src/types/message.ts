export type MessageType = "TEXT" | "IMAGE" | "FILE" | "SYSTEM";

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  senderUsername: string;
  type: MessageType;
  content: string;
  replyToMessageId: string | null;
  isDeleted: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface SendMessageRequest {
  conversationId: string;
  content: string;
  type?: MessageType;
  replyToMessageId?: string | null;
}

