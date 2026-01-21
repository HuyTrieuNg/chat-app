import { create } from "zustand";

export type UserStatus = "ONLINE" | "OFFLINE" | "AWAY";

interface PresenceState {
  userStatuses: Map<string, UserStatus>;
  setUserStatus: (userId: string, status: UserStatus) => void;
  clearStatuses: () => void;
}

export const usePresenceStore = create<PresenceState>((set) => ({
  userStatuses: new Map(),

  setUserStatus: (userId, status) =>
    set((state) => {
      const newStatuses = new Map(state.userStatuses);
      newStatuses.set(userId, status);
      return { userStatuses: newStatuses };
    }),

  clearStatuses: () => set({ userStatuses: new Map() }),
}));
