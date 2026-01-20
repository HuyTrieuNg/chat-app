import { Link } from "react-router-dom";
import { useRef } from "react";
import { z } from "zod";
import { MessageCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { CommonForm } from "@/components/CommonForm";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useLogin } from "@/hooks/useAuth";
import { GoogleLogo } from "@/components/ui/GoogleLogo";
import { GithubLogo } from "@/components/ui/GithubLogo";
import type { FormFieldOption, CommonFormRefs } from "@/types/formField";

const loginSchema = z.object({
  username: z.string().min(1, "Username is required"),
  password: z.string().min(1, "Password is required"),
  rememberMe: z.boolean().optional().default(false),
});

type LoginFormData = z.infer<typeof loginSchema>;

const loginFields: FormFieldOption[] = [
  {
    name: "username",
    label: "Username",
    type: "text",
    placeholder: "Enter your username",
  },
  {
    name: "password",
    label: "Password",
    type: "password",
    placeholder: "Enter your password",
  },
  {
    name: "rememberMe",
    label: "Remember me",
    type: "checkbox",
    defaultValue: false,
  },
];

export default function LoginPage() {
  const loginMutation = useLogin();
  const formRef = useRef<CommonFormRefs<typeof loginSchema>>(null);

  const handleSubmit = (data: LoginFormData) => {
    loginMutation.mutate(data);
  };

  const handleOAuthLogin = (provider: string) => {
    const apiUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    window.location.href = `${apiUrl}/oauth2/authorization/${provider}`;
    console.log(`OAuth login with ${provider}`);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-linear-to-br from-slate-50 to-slate-100 dark:from-slate-950 dark:to-slate-900 p-4">
      <Card className="w-full max-w-md shadow-xl">
        <CardHeader className="space-y-1 text-center">
          <div className="flex justify-center mb-4">
            <div className="p-3 bg-primary/10 rounded-full">
              <MessageCircle className="h-8 w-8 text-primary" />
            </div>
          </div>
          <CardTitle className="text-2xl font-bold">Welcome Back</CardTitle>
          <CardDescription>
            Sign in to your account to continue chatting
          </CardDescription>
        </CardHeader>
        <CardContent>
          <CommonForm ref={formRef} schema={loginSchema} fields={loginFields} />

          <Button
            type="button"
            className="w-full mt-4"
            onClick={() =>
              handleSubmit(formRef.current?.getValues() as LoginFormData)
            }
            disabled={loginMutation.isPending}
          >
            {loginMutation.isPending ? "Signing in..." : "Sign In"}
          </Button>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <span className="w-full border-t" />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-background px-2 text-muted-foreground">
                  Or continue with
                </span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 mt-6">
              <Button
                type="button"
                variant="outline"
                onClick={() => handleOAuthLogin("google")}
                disabled={loginMutation.isPending}
              >
                <GoogleLogo />
                Google
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => handleOAuthLogin("github")}
                disabled={loginMutation.isPending}
              >
                <GithubLogo />
                GitHub
              </Button>
            </div>
          </div>
        </CardContent>
        <CardFooter className="flex flex-col space-y-4">
          <div className="text-sm text-muted-foreground text-center">
            Don't have an account?{" "}
            <Link
              to="/register"
              className="text-primary hover:underline font-medium"
            >
              Sign up
            </Link>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
