"use client";

import { AuthGuard } from "@/domains/auth/presentation/components/auth-guard";
import { Sidebar } from "@/shared/components/sidebar";
import { Header } from "@/shared/components/header";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthGuard>
      <div className="min-h-screen bg-muted/40">
        <Sidebar />
        <div className="pl-64">
          <Header />
          <main className="p-6">{children}</main>
        </div>
      </div>
    </AuthGuard>
  );
}
