import { useCurrentUser, useRefreshToken } from "@/hooks/useAuth";
import { useAuthStore } from "@/store/authStore";
import { useEffect, useRef, useState } from "react";
import { useLocation, Outlet } from "react-router-dom";

export function AuthInitializer({ children }: { children: React.ReactNode }) {
  const [isInitialized, setIsInitialized] = useState(false);
  const location = useLocation();
  const accessToken = useAuthStore((state) => state.accessToken);
  const setAuth = useAuthStore((state) => state.setAuth);
  const hasInitialized = useRef(false);

  const refreshMutation = useRefreshToken();
  const userQuery = useCurrentUser(isInitialized);

  // Skip refresh on OAuth callback route
  const isOAuthCallback = location.pathname === "/oauth2/callback";

  useEffect(() => {
    if (hasInitialized.current) {
      return;
    }

    const init = async () => {
      hasInitialized.current = true;
      console.log(
        "isOAuthCallback:",
        isOAuthCallback,
        "accessToken:",
        !!accessToken
      );

      try {
        if (!accessToken && !isOAuthCallback) {
          console.log("Calling refresh token...");
          await refreshMutation.mutateAsync();
        } else {
          console.log("Skipping refresh - OAuth callback or token exists");
        }
      } catch (error) {
        console.log("No valid session, continuing to app: ", error);
      } finally {
        setIsInitialized(true);
        console.log(
          "Auth initialization complete - isInitialized will be true"
        );
      }
    };

    init();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (isInitialized && userQuery.data && !userQuery.isLoading) {
      const token = accessToken || refreshMutation.data?.accessToken;
      if (token) {
        console.log("Setting auth with user:", userQuery.data);
        setAuth(userQuery.data, token);
      }
    }
  }, [
    isInitialized,
    userQuery.data,
    userQuery.isLoading,
    accessToken,
    refreshMutation.data?.accessToken,
    setAuth,
  ]);

  if (!isInitialized) {
    return <div>Loading...</div>;
  }

  return (
    <>
      {children}
      <Outlet />
    </>
  );
}
