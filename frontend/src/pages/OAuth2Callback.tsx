import { authApi } from "@/services/authService";
import { useAuthStore } from "@/store/authStore";
import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { useNavigate } from "react-router-dom";

export default function OAuth2Callback() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const updateAccessToken = useAuthStore((state) => state.updateAccessToken);
  const [isProcessing, setIsProcessing] = useState(true);

  useEffect(() => {
    const handleCallback = async () => {
      console.log("OAuth2Callback: Processing callback...");
      const params = new URLSearchParams(window.location.search);
      const accessToken = params.get("accessToken");
      const error = params.get("error");

      if (error) {
        console.error("OAuth2 Error:", error);
        toast.error("OAuth login failed. Please try again.");
        navigate("/login");
        return;
      }

      if (!accessToken) {
        console.error("No access token in OAuth callback");
        toast.error("Authentication failed. Please login again.");
        navigate("/login");
        return;
      }

      try {
        console.log("OAuth2Callback: Setting token and fetching user...");

        updateAccessToken(accessToken);

        const user = await authApi.getCurrentUser();

        console.log("OAuth2Callback: User fetched:", user);

        setAuth(
          {
            userId: user.userId,
            username: user.username,
            email: user.email,
          },
          accessToken
        );

        toast.success("Login successfully!");

        // Clear URL parameters
        window.history.replaceState(
          {},
          document.title,
          window.location.pathname
        );

        navigate("/");
      } catch (err) {
        console.error("Failed to fetch user after OAuth2 login:", err);
        toast.error("Authentication failed. Please login again.");
        navigate("/login");
      } finally {
        setIsProcessing(false);
      }
    };

    handleCallback();
  }, [navigate, setAuth, updateAccessToken]);

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-gray-100 p-6 space-y-3">
      {isProcessing ? (
        <>
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 dark:border-gray-100"></div>
          <p>Processing OAuth2 callback...</p>
        </>
      ) : (
        <p>Redirecting...</p>
      )}
    </div>
  );
}
