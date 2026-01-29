import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { chatApi } from "@/services/chatService";
import type { Conversation } from "@/types/conversation";
import type { Message, SendMessageRequest } from "@/types/message";

export function useConversations() {
  return useQuery({
    queryKey: ["conversations"],
    queryFn: () => chatApi.getConversations(),
  });
}

export function useConversation(conversationId: string | null) {
  return useQuery({
    queryKey: ["conversation", conversationId],
    queryFn: () => {
      if (!conversationId) throw new Error("Conversation ID is required");
      return chatApi.getConversation(conversationId);
    },
    enabled: !!conversationId,
  });
}

export function useMessages(
  conversationId: string | null,
  limit: number = 50,
  before?: string
) {
  return useQuery({
    queryKey: ["messages", conversationId, limit, before],
    queryFn: () => {
      if (!conversationId) throw new Error("Conversation ID is required");
      return chatApi.getMessages(conversationId, limit, before);
    },
    enabled: !!conversationId,
  });
}

export function useCreatePrivateConversation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (otherUserId: string) =>
      chatApi.getOrCreatePrivateConversation(otherUserId),
    onSuccess: (data) => {
      queryClient.setQueryData(["conversation", data.id], data);
      queryClient.invalidateQueries({ queryKey: ["conversations"] });
    },
  });
}

export function useSendMessage() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: SendMessageRequest) => chatApi.sendMessage(request),
    onSuccess: (data, variables) => {

      queryClient.invalidateQueries({
        queryKey: ["messages", variables.conversationId],
      });
    },
  });
}

