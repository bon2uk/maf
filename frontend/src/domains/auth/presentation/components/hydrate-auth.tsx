"use client";

import { useEffect } from "react";
import { User } from "@/domains/user/domain/types";
import { useAuthStore } from "../../infrastructure/store/auth-store";

interface HydrateAuthProps {
  user: User | null;
}

export function HydrateAuth({ user }: HydrateAuthProps) {
  const setUser = useAuthStore((state) => state.setUser);

  useEffect(() => {
    setUser(user);
  }, [user, setUser]);

  return null;
}
