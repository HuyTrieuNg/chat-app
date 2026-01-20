import { Link } from "react-router-dom";
import { useRef } from "react";
import { z } from "zod";
import { MessageCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { CommonForm } from "@/components/CommonForm";
import type { FormFieldOption, CommonFormRefs } from "@/types/formField";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useRegister } from "@/hooks/useAuth";
import { GoogleLogo } from "@/components/ui/GoogleLogo";
import { GithubLogo } from "@/components/ui/GithubLogo";

const registerSchema = z
  .object({
    username: z
      .string()
      .min(3, "Username must be at least 3 characters")
      .max(20, "Username must be at most 20 characters")
      .regex(
        /^[a-zA-Z0-9_]+$/,
        "Username can only contain letters, numbers, and underscores",
      ),
    email: z.email("Invalid email address"),
    password: z
      .string()
      .min(6, "Password must be at least 6 characters")
      .max(50, "Password must be at most 50 characters"),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });

type RegisterFormData = z.infer<typeof registerSchema>;

const registerFields: FormFieldOption[] = [
  {
    name: "username",
    label: "Username",
    type: "text",
    placeholder: "Choose a username",
  },
  {
    name: "email",
    label: "Email",
    type: "text",
    placeholder: "Enter your email",
  },
  {
    name: "password",
    label: "Password",
    type: "password",
    placeholder: "Create a password",
  },
  {
    name: "confirmPassword",
    label: "Confirm Password",
    type: "password",
    placeholder: "Confirm your password",
  },
];

export default function RegisterPage() {
  const registerMutation = useRegister();
  const formRef = useRef<CommonFormRefs<typeof registerSchema>>(null);

  const handleSubmit = (data: RegisterFormData) => {
    registerMutation.mutate({
      username: data.username,
      email: data.email,
      password: data.password,
    });
  };

  const handleOAuthRegister = (provider: string) => {
    window.location.href = `/oauth2/authorization/${provider}`;
    console.log(`OAuth register with ${provider}`);
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
          <CardTitle className="text-2xl font-bold">Create Account</CardTitle>
          <CardDescription>
            Sign up to start chatting with your friends
          </CardDescription>
        </CardHeader>
        <CardContent>
          <CommonForm
            ref={formRef}
            schema={registerSchema}
            fields={registerFields}
          />

          <Button
            type="button"
            className="w-full mt-4"
            onClick={() =>
              handleSubmit(formRef.current?.getValues() as RegisterFormData)
            }
            disabled={registerMutation.isPending}
          >
            {registerMutation.isPending
              ? "Creating account..."
              : "Create Account"}
          </Button>

          {/* OAuth Section */}
          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <span className="w-full border-t" />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-background px-2 text-muted-foreground">
                  Or sign up with
                </span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 mt-6">
              <Button
                type="button"
                variant="outline"
                onClick={() => handleOAuthRegister("google")}
                disabled={registerMutation.isPending}
              >
                <GoogleLogo />
                Google
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => handleOAuthRegister("github")}
                disabled={registerMutation.isPending}
              >
                <GithubLogo />
                GitHub
              </Button>
            </div>
          </div>
        </CardContent>
        <CardFooter className="flex flex-col space-y-4">
          <div className="text-sm text-muted-foreground text-center">
            Already have an account?{" "}
            <Link
              to="/login"
              className="text-primary hover:underline font-medium"
            >
              Sign in
            </Link>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
