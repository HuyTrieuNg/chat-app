export type ConversationType = "PRIVATE" | "GROUP";

export interface Conversation {
  id: string;
  type: ConversationType;
  name: string | null;
  createdBy: string;
  isDeleted: boolean;
  createdAt: string;
  updatedAt: string;
  memberIds: string[];
  unreadCount?: number | null;
}

