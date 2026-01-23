import { create } from "zustand";

export type UserStatus = "ONLINE" | "OFFLINE" | "AWAY";

interface PresenceState {
  userStatuses: Map<string, UserStatus>;
  userLastSeen: Map<string, Date>;
  wsConnected: boolean;
  setUserStatus: (userId: string, status: UserStatus) => void;
  setUserLastSeen: (userId: string, lastSeen: Date) => void;
  getUserLastSeen: (userId: string) => Date | undefined;
  setWsConnected: (connected: boolean) => void;
  clearStatuses: () => void;
}

export const usePresenceStore = create<PresenceState>((set, get) => ({
  userStatuses: new Map(),
  userLastSeen: new Map(),
  wsConnected: false,

  setUserStatus: (userId, status) =>
    set((state) => {
      const newStatuses = new Map(state.userStatuses);
      newStatuses.set(userId, status);
      return { userStatuses: newStatuses };
    }),

  setUserLastSeen: (userId, lastSeen) =>
    set((state) => {
      const newLastSeen = new Map(state.userLastSeen);
      newLastSeen.set(userId, lastSeen);
      return { userLastSeen: newLastSeen };
    }),

  getUserLastSeen: (userId) => {
    return get().userLastSeen.get(userId);
  },

  setWsConnected: (connected) => set({ wsConnected: connected }),

  clearStatuses: () =>
    set({
      userStatuses: new Map(),
      userLastSeen: new Map(),
      wsConnected: false,
    }),
}));
