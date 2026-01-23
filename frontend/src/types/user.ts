export type UserStatus = "ONLINE" | "OFFLINE" | "AWAY";

export interface User {
  userId: string;
  username: string;
  email: string;
}
