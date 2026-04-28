import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { LoginForm } from "@/domains/auth/presentation/components/login-form";

const SESSION_COOKIE = "maf_session";

interface LoginPageProps {
  searchParams: { next?: string };
}

export default function LoginPage({ searchParams }: LoginPageProps) {
  if (cookies().get(SESSION_COOKIE)?.value) {
    const next = searchParams.next && searchParams.next.startsWith("/") ? searchParams.next : "/";
    redirect(next);
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/40 px-4">
      <LoginForm />
    </div>
  );
}
