import { apiClient } from "@/lib/axios";
import type { Conversation } from "@/types/conversation";
import type { Message, SendMessageRequest } from "@/types/message";

export const chatApi = {
  getConversations: async (): Promise<Conversation[]> => {
    const response = await apiClient.get<Conversation[]>("/chat/conversations");
    return response.data;
  },

  getConversation: async (conversationId: string): Promise<Conversation> => {
    const response = await apiClient.get<Conversation>(
      `/chat/conversations/${conversationId}`
    );
    return response.data;
  },

  getOrCreatePrivateConversation: async (
    otherUserId: string
  ): Promise<Conversation> => {
    const response = await apiClient.post<Conversation>(
      `/chat/conversations/private/${otherUserId}`
    );
    return response.data;
  },

  getMessages: async (
    conversationId: string,
    limit: number = 50,
    before?: string
  ): Promise<Message[]> => {
    const params = new URLSearchParams();
    params.append("limit", limit.toString());
    if (before) {
      params.append("before", before);
    }
    const response = await apiClient.get<Message[]>(
      `/chat/conversations/${conversationId}/messages?${params.toString()}`
    );
    return response.data;
  },

  getNewMessages: async (
    conversationId: string,
    after: string
  ): Promise<Message[]> => {
    const response = await apiClient.get<Message[]>(
      `/messages/conversations/${conversationId}/new?after=${after}`
    );
    return response.data;
  },

  sendMessage: async (request: SendMessageRequest): Promise<Message> => {
    const response = await apiClient.post<Message>("/messages", request);
    return response.data;
  },
};

