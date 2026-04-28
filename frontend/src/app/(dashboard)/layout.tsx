import { Sidebar } from "@/shared/components/sidebar";
import { Header } from "@/shared/components/header";
import { HydrateAuth } from "@/domains/auth/presentation/components/hydrate-auth";
import { getCurrentUser } from "@/domains/user/infrastructure/server/user-server-api";

export default async function DashboardLayout({ children }: { children: React.ReactNode }) {
  const user = await getCurrentUser();

  return (
    <div className="min-h-screen bg-muted/40">
      <HydrateAuth user={user} />
      <Sidebar />
      <div className="pl-64">
        <Header />
        <main className="p-6">{children}</main>
      </div>
    </div>
  );
}
