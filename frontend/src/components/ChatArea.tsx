import { MessageSquare, Send } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { PresenceIndicator } from "@/components/PresenceIndicator";
import type { User } from "@/types/user";
import { useState, useEffect, useRef } from "react";
import { useCreatePrivateConversation, useMessages, useSendMessage } from "@/hooks/useChat";
import { subscribeToConversationMessages, unsubscribeFromConversationMessages, sendMessageViaWebSocket } from "@/ws/messages";
import { useAuthStore } from "@/store/authStore";
import type { Message } from "@/types/message";
import { cn } from "@/lib/utils";

interface ChatAreaProps {
  selectedUser: User | null;
}

export function ChatArea({ selectedUser }: ChatAreaProps) {
  const [message, setMessage] = useState("");
  const [localMessages, setLocalMessages] = useState<Message[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const currentUserId = useAuthStore((state) => state.user?.userId);
  
  const createConversationMutation = useCreatePrivateConversation();
  const sendMessageMutation = useSendMessage();
  
  const conversationId = createConversationMutation.data?.id;
  const { data: messages = [], isLoading: isLoadingMessages } = useMessages(
    conversationId || null,
    50
  );

  useEffect(() => {
    if (selectedUser && !createConversationMutation.data && !createConversationMutation.isPending) {
      createConversationMutation.mutate(selectedUser.userId);
    }
  }, [selectedUser, createConversationMutation]);

  useEffect(() => {
    if (messages.length > 0) {
      const reversed = [...messages].reverse();
      setLocalMessages(reversed);
    } else if (messages.length === 0 && conversationId) {
      setLocalMessages([]);
    }
  }, [messages, conversationId]);

  useEffect(() => {
    if (!conversationId) return;

    const unsubscribe = subscribeToConversationMessages(conversationId, (newMessage) => {
      setLocalMessages((prev) => {
        if (prev.some((m) => m.id === newMessage.id)) {
          return prev;
        }
        return [...prev, newMessage];
      });
    });

    return () => {
      unsubscribe();
      unsubscribeFromConversationMessages(conversationId);
    };
  }, [conversationId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [localMessages]);

  const handleSend = () => {
    if (!message.trim() || !conversationId) return;

    sendMessageViaWebSocket(conversationId, message.trim(), "TEXT");
    
    sendMessageMutation.mutate({
      conversationId,
      content: message.trim(),
      type: "TEXT",
    });

    setMessage("");
  };

  if (!selectedUser) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center bg-slate-50 dark:bg-slate-900">
        <MessageSquare className="h-20 w-20 text-muted-foreground/50 mb-4" />
        <h3 className="text-xl font-semibold text-muted-foreground mb-2">
          No conversation selected
        </h3>
        <p className="text-muted-foreground">
          Select a user from the list to start chatting
        </p>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col bg-white dark:bg-slate-800">
      {/* Chat Header */}
      <div className="p-4 border-b border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800">
        <div className="flex items-center gap-3">
          {/* Avatar */}
          <div className="relative">
            <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-lg font-semibold text-primary">
              {selectedUser.username[0].toUpperCase()}
            </div>
            <div className="absolute bottom-0 right-0">
              <PresenceIndicator userId={selectedUser.userId} size="sm" />
            </div>
          </div>

          {/* User Info */}
          <div className="flex-1">
            <h3 className="font-semibold">{selectedUser.username}</h3>
            <div className="flex items-center gap-2">
              <PresenceIndicator
                userId={selectedUser.userId}
                showLabel
                showOffline
                size="sm"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-4 bg-slate-50 dark:bg-slate-900">
        {isLoadingMessages ? (
          <div className="text-center text-muted-foreground py-8">
            <p>Loading messages...</p>
          </div>
        ) : localMessages.length === 0 ? (
          <div className="text-center text-muted-foreground py-8">
            <p>Start a conversation with {selectedUser.username}</p>
            <p className="text-sm mt-2">Messages will appear here</p>
          </div>
        ) : (
          <div className="space-y-4">
            {localMessages.map((msg) => {
              const isOwnMessage = msg.senderId === currentUserId;
              return (
                <div
                  key={msg.id}
                  className={cn(
                    "flex",
                    isOwnMessage ? "justify-end" : "justify-start"
                  )}
                >
                  <div
                    className={cn(
                      "max-w-[70%] rounded-lg px-4 py-2",
                      isOwnMessage
                        ? "bg-primary text-primary-foreground"
                        : "bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100"
                    )}
                  >
                    {!isOwnMessage && (
                      <div className="text-xs font-semibold mb-1 opacity-80">
                        {msg.senderUsername}
                      </div>
                    )}
                    <div className="text-sm whitespace-pre-wrap break-words">
                      {msg.content}
                    </div>
                    <div
                      className={cn(
                        "text-xs mt-1",
                        isOwnMessage ? "opacity-70" : "opacity-60"
                      )}
                    >
                      {new Date(msg.createdAt).toLocaleTimeString([], {
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </div>
                  </div>
                </div>
              );
            })}
            <div ref={messagesEndRef} />
          </div>
        )}
      </div>

      {/* Message Input */}
      <div className="p-4 border-t border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800">
        <div className="flex gap-2">
          <Input
            placeholder="Type a message..."
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                handleSend();
              }
            }}
            className="flex-1"
          />
          <Button onClick={handleSend} disabled={!message.trim()}>
            <Send className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
