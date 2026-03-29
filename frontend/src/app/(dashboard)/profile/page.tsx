"use client";

import { useCurrentUser } from "@/domains/user/presentation/hooks/use-current-user";
import { ProfileCard } from "@/domains/user/presentation/components/profile-card";
import { ProfileForm } from "@/domains/user/presentation/components/profile-form";
import { PageHeader } from "@/shared/components/page-header";
import { Loading } from "@/shared/components/loading";
import { ErrorState } from "@/shared/components/error-state";

export default function ProfilePage() {
  const { data: user, isLoading, isError, refetch } = useCurrentUser();

  if (isLoading) {
    return <Loading />;
  }

  if (isError || !user) {
    return (
      <ErrorState
        title="Failed to load profile"
        message="We couldn't load your profile. Please try again."
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <PageHeader
        title="Profile"
        description="Manage your account settings"
      />
      <ProfileCard user={user} />
      <ProfileForm user={user} />
    </div>
  );
}
