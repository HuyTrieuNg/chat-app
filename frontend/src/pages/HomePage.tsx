import { useState } from "react";
import { UserListSidebar } from "@/components/UserListSidebar";
import { ChatArea } from "@/components/ChatArea";
import type { User } from "@/types/user";

export default function HomePage() {
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

  return (
    <div className="h-[calc(100vh-8rem)] flex">
      <UserListSidebar
        selectedUserId={selectedUser?.userId || null}
        onSelectUser={setSelectedUser}
      />
      <ChatArea selectedUser={selectedUser} />
    </div>
  );
}
