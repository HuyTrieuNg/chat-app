import { useAuthStore } from "@/store/authStore";
import { authApi } from "@/services/authService";
import toast from "react-hot-toast";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { LogOut, MessageCircle } from "lucide-react";

export default function HomePage() {
  const { user, clearAuth } = useAuthStore();
  const navigate = useNavigate();

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

        <div className="bg-white dark:bg-slate-800 rounded-lg shadow-md p-8 text-center">
          <h2 className="text-xl font-semibold mb-4">
            🎉 Authentication Setup Complete!
          </h2>
          <div className="space-y-2 text-muted-foreground">
            <p>Your chat app is ready to go.</p>
            <p>User ID: {user?.userId}</p>
            <p>Email: {user?.email}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
