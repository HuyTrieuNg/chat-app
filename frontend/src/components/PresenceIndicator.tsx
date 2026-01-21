import { usePresenceStore, type UserStatus } from "@/store/presenceStore";
import { cn } from "@/lib/utils";

interface PresenceIndicatorProps {
  userId: string;
  size?: "sm" | "md" | "lg";
  showOffline?: boolean;
  showLabel?: boolean;
  className?: string;
}

export function PresenceIndicator({
  userId,
  size = "sm",
  showOffline = false,
  showLabel = false,
  className,
}: PresenceIndicatorProps) {
  const userStatuses = usePresenceStore((state) => state.userStatuses);
  const status: UserStatus = userStatuses.get(userId) || "OFFLINE";

  if (status === "OFFLINE" && !showOffline) {
    return null;
  }

  const sizeClasses = {
    sm: "w-2.5 h-2.5",
    md: "w-3 h-3",
    lg: "w-4 h-4",
  };

  const statusConfig = {
    ONLINE: {
      color: "bg-green-500",
      label: "Online",
      ring: "ring-2 ring-green-500/20",
    },
    AWAY: {
      color: "bg-yellow-500",
      label: "Away",
      ring: "ring-2 ring-yellow-500/20",
    },
    OFFLINE: {
      color: "bg-gray-400",
      label: "Offline",
      ring: "ring-2 ring-gray-400/20",
    },
  };

  const config = statusConfig[status];

  return (
    <div className={cn("flex items-center gap-2", className)}>
      <div className="relative">
        <div
          className={cn(
            "rounded-full",
            sizeClasses[size],
            config.color,
            status === "ONLINE" && "animate-pulse",
          )}
        />
        <div className={cn("absolute inset-0 rounded-full", config.ring)} />
      </div>
      {showLabel && (
        <span className="text-xs text-muted-foreground">{config.label}</span>
      )}
    </div>
  );
}
