import { UserEntity } from "../../domain/entities/user";
import { User, UserRole, UpdateUserData } from "../../domain/types";
import { UserResponse, UpdateUserRequest } from "../../infrastructure/dto/user-dto";

export const userMapper = {
  toDomain(dto: UserResponse): User {
    return new UserEntity(
      dto.id,
      dto.email,
      dto.first_name,
      dto.last_name,
      dto.role as UserRole,
      new Date(dto.created_at),
      new Date(dto.updated_at),
      dto.avatar_url
    );
  },

  toUpdateRequest(data: UpdateUserData): UpdateUserRequest {
    return {
      first_name: data.firstName,
      last_name: data.lastName,
      email: data.email,
    };
  },
};
