import { UserEntity } from "../../domain/entities/user";
import { User, UserRole, UpdateUserData } from "../../domain/types";
import { UserResponse, UpdateUserRequest } from "../../infrastructure/dto/user-dto";

export const userMapper = {
  toDomain(dto: UserResponse): UserEntity {
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

  // Plain serializable shape suitable for crossing the RSC -> Client boundary.
  // Eagerly materializes interface fields that are computed by UserEntity getters.
  toPlain(entity: UserEntity): User {
    return {
      id: entity.id,
      email: entity.email,
      firstName: entity.firstName,
      lastName: entity.lastName,
      fullName: entity.fullName,
      avatarUrl: entity.avatarUrl,
      role: entity.role,
      createdAt: entity.createdAt,
      updatedAt: entity.updatedAt,
    };
  },

  toUpdateRequest(data: UpdateUserData): UpdateUserRequest {
    return {
      first_name: data.firstName,
      last_name: data.lastName,
      email: data.email,
    };
  },
};
