import { getWsClient } from "@/lib/wsClient";

let lastActivitySent: number = 0;
const ACTIVITY_THROTTLE_MS = 30000; // 30 seconds

// Send activity ping to server (throttled).
// This updates the user's lastActivity timestamp to prevent AWAY status.
// Uses throttle: sends immediately, then ignores calls for the next 30 seconds.
export function sendActivity(): void {
  const now = Date.now();

  if (now - lastActivitySent < ACTIVITY_THROTTLE_MS) {
    return;
  }

  const client = getWsClient();
  if (client?.connected) {
    try {
      client.publish({
        destination: "/app/activity",
        body: "",
      });
      lastActivitySent = now;
      console.log("📡 Sent activity ping");
    } catch (error) {
      console.error("Failed to send activity:", error);
    }
  }
}

// Track user activity events and send pings to server.
export function trackUserActivity(): () => void {
  const events = ["mousedown", "keydown", "scroll", "touchstart"];

  const handleActivity = () => {
    sendActivity();
  };

  events.forEach((event) => {
    window.addEventListener(event, handleActivity, { passive: true });
  });

  const onVisibilityChange = () => {
    if (document.visibilityState === "visible") {
      sendActivity();
    }
  };

  document.addEventListener("visibilitychange", onVisibilityChange);

  // Send initial activity ping
  sendActivity();

  // Return cleanup function
  return () => {
    events.forEach((event) => {
      window.removeEventListener(event, handleActivity);
    });
  };
}
