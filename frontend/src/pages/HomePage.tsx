import { useAuthStore } from "@/store/authStore";
import { authApi } from "@/services/authService";
import toast from "react-hot-toast";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { LogOut, MessageCircle, RefreshCw } from "lucide-react";
import { useEffect, useState } from "react";
import type { User } from "@/types/auth";

export default function HomePage() {
  const { user, clearAuth } = useAuthStore();
  const navigate = useNavigate();
  const [userData, setUserData] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchUserData = async () => {
    setLoading(true);
    try {
      const data = await authApi.getCurrentUser();
      setUserData(data);
      toast.success("User data loaded successfully");
    } catch (error) {
      console.error("Fetch user error:", error);
      toast.error("Failed to load user data");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUserData();
  }, []);

  const handleLogout = async () => {
    try {
      await authApi.logout();
      clearAuth();
      toast.success("Logged out successfully");
      navigate("/login");
    } catch (error) {
      console.error("Logout error:", error);
      toast.error("Logout failed");
    }
  };

  return (
    <div className="min-h-screen bg-linear-to-br from-slate-50 to-slate-100 dark:from-slate-950 dark:to-slate-900">
      <div className="container mx-auto p-4">
        <div className="flex items-center justify-between mb-8 bg-white dark:bg-slate-800 rounded-lg shadow-md p-4">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-primary/10 rounded-full">
              <MessageCircle className="h-6 w-6 text-primary" />
            </div>
            <div>
              <h1 className="text-2xl font-bold">Chat App</h1>
              <p className="text-sm text-muted-foreground">
                Welcome, {user?.username}!
              </p>
            </div>
          </div>
          <Button onClick={handleLogout} variant="outline" size="sm">
            <LogOut className="mr-2 h-4 w-4" />
            Logout
          </Button>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-lg shadow-md p-8">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold">🎉 User Profile</h2>
            <Button
              onClick={fetchUserData}
              variant="outline"
              size="sm"
              disabled={loading}
            >
              <RefreshCw
                className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`}
              />
              Refresh Data
            </Button>
          </div>

          {loading && !userData ? (
            <div className="text-center py-8">
              <RefreshCw className="h-8 w-8 animate-spin mx-auto text-primary" />
              <p className="mt-4 text-muted-foreground">Loading user data...</p>
            </div>
          ) : userData ? (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-lg">
                  <p className="text-sm text-muted-foreground mb-1">User ID</p>
                  <p className="font-mono text-sm">{userData.userId}</p>
                </div>
                <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-lg">
                  <p className="text-sm text-muted-foreground mb-1">Username</p>
                  <p className="font-semibold">{userData.username}</p>
                </div>
                <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-lg">
                  <p className="text-sm text-muted-foreground mb-1">
                    Last Fetched
                  </p>
                  <p className="text-sm">{new Date().toLocaleString()}</p>
                </div>
              </div>
              <div className="mt-6 p-4 bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 rounded-lg">
                <p className="text-sm text-green-800 dark:text-green-200">
                  ✅ API call successful! Token refresh is working properly.
                </p>
              </div>
            </div>
          ) : (
            <div className="text-center py-8 text-muted-foreground">
              <p>No user data available</p>
            </div>
          )}

          <div className="mt-8 space-y-2 text-muted-foreground text-center">
            <p>Your chat app is ready to go.</p>
            <p>User ID: {user?.userId}</p>
            <p>Email: {user?.email}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
