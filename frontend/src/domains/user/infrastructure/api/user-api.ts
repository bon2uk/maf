import { api } from "@/shared/lib/api-client";
import { User, UpdateUserData } from "../../domain/types";
import { UserResponse } from "../dto/user-dto";
import { userMapper } from "../../application/mappers/user-mapper";

export const userApi = {
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<UserResponse>("/users/me");
    return userMapper.toDomain(response);
  },

  updateCurrentUser: async (data: UpdateUserData): Promise<User> => {
    const request = userMapper.toUpdateRequest(data);
    const response = await api.put<UserResponse>("/users/me", request);
    return userMapper.toDomain(response);
  },
};
