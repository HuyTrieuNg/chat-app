# Presence System Flow

## Overview

The presence system tracks user online/offline/away status using WebSocket connections, heartbeats, and user activity.

## Components

### Backend

1. **RedisPresenceStore** - Manages presence data in Redis
2. **PresenceEventListener** - Handles WebSocket connect/disconnect events
3. **WebSocketAuthInterceptor** - Updates heartbeat on every incoming message
4. **ActivityController** - Receives activity pings from frontend
5. **PresenceApplicationService** - Business logic for status changes
6. **PresenceScheduler** - Periodic checks for AWAY/OFFLINE status

### Frontend

1. **wsClient.ts** - WebSocket client setup
2. **activity.ts** - Tracks user activity and sends pings
3. **presence.ts** - Subscribes to presence updates
4. **useWebSocket.ts** - React hook integrating everything

## Flow Diagrams

### 1. User Connects (New Tab/Device)

```
User opens tab/device
        ↓
WebSocket CONNECT (with JWT token)
        ↓
WebSocketAuthInterceptor validates token
        ↓
SessionConnectedEvent fired
        ↓
PresenceEventListener.onConnect()
        ↓
RedisPresenceStore.addSocket(userId, sessionId)
    - Add sessionId to presence:user:{id}:sockets (Set)
    - Initialize presence:user:{id}:heartbeat
    - Initialize presence:user:{id}:lastActivity
        ↓
Is first socket? (Set size == 1)
    YES → PresenceApplicationService.userConnected()
        - Set presence:user:{id}:status = "ONLINE"
        - Add to presence:onlineUsers
        - Update DB (with debounce)
        - Broadcast ONLINE to /topic/presence
    NO → Log "Additional connection"
```

### 2. Heartbeat Updates

```
Any STOMP message from client
        ↓
WebSocketAuthInterceptor.preSend()
        ↓
Extract userId from Principal
        ↓
RedisPresenceStore.updateHeartbeat(userId)
    - Set presence:user:{id}:heartbeat = current_timestamp
```

### 3. User Activity Tracking

```
Frontend: User interacts (mouse/keyboard/scroll/touch)
        ↓
activity.ts debounces (30 seconds)
        ↓
Send STOMP message to /app/activity
        ↓
ActivityController.handleActivity()
        ↓
PresenceApplicationService.updateUserActivity()
    - Update presence:user:{id}:lastActivity = current_timestamp
    - If status was AWAY → Set ONLINE and broadcast
```

### 4. Detecting AWAY Status

```
PresenceScheduler.checkAwayStatus() (every 60s)
        ↓
Get all users from presence:onlineUsers
        ↓
For each ONLINE user:
    Check RedisPresenceStore.shouldAway(userId)
        - Get presence:user:{id}:lastActivity
        - If (current_time - lastActivity) > 5 minutes
            ↓
            YES → PresenceApplicationService.userAway()
                - Set presence:user:{id}:status = "AWAY"
                - Update DB
                - Broadcast AWAY to /topic/presence
```

### 5. Detecting OFFLINE Status

```
PresenceScheduler.checkOfflineStatus() (every 30s)
        ↓
Get all users from presence:onlineUsers
        ↓
For each user:
    Check RedisPresenceStore.shouldOffline(userId)
        - Get sockets count from presence:user:{id}:sockets
        - Get presence:user:{id}:heartbeat
        - If sockets.isEmpty() AND (current_time - heartbeat) > 30s
            ↓
            YES → PresenceApplicationService.userDisconnected()
                - Set presence:user:{id}:status = "OFFLINE"
                - Remove from presence:onlineUsers
                - Update DB with lastSeen (with debounce)
                - Broadcast OFFLINE + lastSeen to /topic/presence
```

### 6. User Disconnects (Closes Tab/Device)

```
User closes tab/device
        ↓
WebSocket connection drops
        ↓
SessionDisconnectEvent fired
        ↓
PresenceEventListener.onDisconnect()
        ↓
RedisPresenceStore.removeSocket(userId, sessionId)
    - Remove sessionId from presence:user:{id}:sockets
        ↓
Is last socket? (Set is empty)
    YES → PresenceApplicationService.userDisconnected()
        - Set presence:user:{id}:status = "OFFLINE"
        - Remove from presence:onlineUsers
        - Update DB with lastSeen (with debounce)
        - Broadcast OFFLINE + lastSeen
    NO → Log "Partial disconnect, remaining sockets: X"
```

## Redis Keys

| Key                               | Type   | Purpose                                 | Example              |
| --------------------------------- | ------ | --------------------------------------- | -------------------- |
| `presence:user:{id}:status`       | String | Current status (ONLINE/OFFLINE/AWAY)    | "ONLINE"             |
| `presence:user:{id}:sockets`      | Set    | Active WebSocket session IDs            | ["sess-1", "sess-2"] |
| `presence:user:{id}:heartbeat`    | String | Last heartbeat timestamp (ms)           | "1737547200000"      |
| `presence:user:{id}:lastActivity` | String | Last user activity timestamp (ms)       | "1737547200000"      |
| `presence:onlineUsers`            | Set    | All currently online user IDs           | ["user-1", "user-2"] |
| `presence:debounce:user:{id}`     | String | Debounce flag for DB updates (TTL: 30s) | "1"                  |

## Timeouts & Thresholds

| Parameter              | Value      | Purpose                                  |
| ---------------------- | ---------- | ---------------------------------------- |
| Heartbeat timeout      | 30 seconds | If no heartbeat + no sockets → OFFLINE   |
| Activity timeout       | 5 minutes  | If no activity → AWAY                    |
| Activity debounce      | 30 seconds | Frontend sends activity max once per 30s |
| DB update debounce     | 30 seconds | Backend updates DB max once per 30s      |
| AWAY check interval    | 60 seconds | Scheduler checks AWAY status             |
| OFFLINE check interval | 30 seconds | Scheduler checks OFFLINE status          |

## Status Transitions

```
OFFLINE → ONLINE
    Trigger: First WebSocket connection

ONLINE → AWAY
    Trigger: No activity for 5 minutes

AWAY → ONLINE
    Trigger: User activity detected

ONLINE/AWAY → OFFLINE
    Trigger: All sockets closed OR (heartbeat timeout + no sockets)
```

## Multi-Device Support

- User can have multiple active WebSocket connections (tabs/devices)
- Each connection has a unique sessionId stored in `presence:user:{id}:sockets`
- User is ONLINE as long as at least one socket is active
- User goes OFFLINE only when:
  1. All sockets are closed (last tab/device disconnected), OR
  2. No sockets AND heartbeat timed out (crashed without disconnect event)

## Frontend Implementation

```typescript
// useWebSocket.ts
- Creates WebSocket client with token
- Subscribes to /topic/presence for status updates
- Starts activity tracking

// activity.ts
- Listens to user events (mouse, keyboard, scroll, touch)
- Debounces and sends to /app/activity
- Prevents AWAY status

// wsClient.ts
- Manages WebSocket connection
- Sends heartbeat via STOMP protocol (configured in server)
```
