import { getCurrentUser } from "@/domains/user/infrastructure/server/user-server-api";
import { ProfileCard } from "@/domains/user/presentation/components/profile-card";
import { ProfileForm } from "@/domains/user/presentation/components/profile-form";
import { PageHeader } from "@/shared/components/page-header";

export default async function ProfilePage() {
  const user = await getCurrentUser();

  return (
    <div className="space-y-6 max-w-2xl">
      <PageHeader title="Profile" description="Manage your account settings" />
      <ProfileCard user={user} />
      <ProfileForm user={user} />
    </div>
  );
}
