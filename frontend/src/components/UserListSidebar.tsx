import { useState } from "react";
import { Search, Users } from "lucide-react";
import { Input } from "@/components/ui/input";
import { PresenceIndicator } from "@/components/PresenceIndicator";
import { useUsers } from "@/hooks/useUsers";
import { usePresenceStore } from "@/store/presenceStore";
import { cn } from "@/lib/utils";
import type { User } from "@/types/user";

interface UserListSidebarProps {
  selectedUserId: string | null;
  onSelectUser: (user: User) => void;
}

export function UserListSidebar({
  selectedUserId,
  onSelectUser,
}: UserListSidebarProps) {
  const [searchQuery, setSearchQuery] = useState("");
  const { data: users = [], isLoading } = useUsers();
  const userStatuses = usePresenceStore((state) => state.userStatuses);

  const filteredUsers = users.filter(
    (user) =>
      user.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const onlineCount = Array.from(userStatuses.values()).filter(
    (status) => status === "ONLINE" || status === "AWAY",
  ).length;

  return (
    <div className="w-80 border-r border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 flex flex-col">
      {/* Header */}
      <div className="p-4 border-b border-slate-200 dark:border-slate-700">
        <div className="flex items-center gap-2 mb-3">
          <Users className="h-5 w-5 text-primary" />
          <h2 className="font-semibold text-lg">Users</h2>
          <span className="ml-auto text-sm text-muted-foreground">
            {onlineCount > 0 && (
              <span className="text-green-500 font-medium">
                {onlineCount} online
              </span>
            )}
            {onlineCount === 0 && "No one online"}
          </span>
        </div>

        {/* Search */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search users..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>
      </div>

      {/* User List */}
      <div className="flex-1 overflow-y-auto">
        {isLoading ? (
          <div className="p-4 text-center text-muted-foreground">
            Loading users...
          </div>
        ) : filteredUsers.length === 0 ? (
          <div className="p-4 text-center text-muted-foreground">
            No users found
          </div>
        ) : (
          <div className="divide-y divide-slate-100 dark:divide-slate-700">
            {filteredUsers.map((user) => (
              <button
                key={user.userId}
                onClick={() => onSelectUser(user)}
                className={cn(
                  "w-full p-4 flex items-center gap-3 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors text-left",
                  selectedUserId === user.userId &&
                    "bg-slate-100 dark:bg-slate-700",
                )}
              >
                {/* Avatar with Presence */}
                <div className="relative">
                  <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-lg font-semibold text-primary">
                    {user.username[0].toUpperCase()}
                  </div>
                  <div className="absolute bottom-0 right-0">
                    <PresenceIndicator userId={user.userId} size="md" />
                  </div>
                </div>

                {/* User Info */}
                <div className="flex-1 min-w-0">
                  <div className="font-medium truncate">{user.username}</div>
                  <div className="text-sm text-muted-foreground truncate">
                    {user.email}
                  </div>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
