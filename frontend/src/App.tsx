import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import LoginPage from "@/pages/LoginPage";
import RegisterPage from "@/pages/RegisterPage";
import HomePage from "@/pages/HomePage";
import ProtectedRoute from "@/components/ProtectedRoute";
import PublicRoute from "@/components/PublicRoute";
import { toastConfig } from "@/config/toast.config";
import OAuth2Callback from "@/pages/OAuth2Callback";
import { AuthInitializer } from "@/components/AuthInitializer";

function App() {
  return (
    <BrowserRouter>
      <AuthInitializer>
        <Toaster {...toastConfig} />
        <Routes>
          <Route element={<PublicRoute />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/oauth2/callback" element={<OAuth2Callback />} />
          </Route>

          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<HomePage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthInitializer>
    </BrowserRouter>
  );
}

export default App;
