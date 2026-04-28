import "server-only";

import { serverFetch } from "@/shared/api/server";
import { User } from "../../domain/types";
import { UserResponse } from "../dto/user-dto";
import { userMapper } from "../../application/mappers/user-mapper";

export async function getCurrentUser(): Promise<User> {
  const dto = await serverFetch<UserResponse>("/users/me");
  return userMapper.toPlain(userMapper.toDomain(dto));
}
