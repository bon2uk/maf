package com.maf.user.mapper;

import com.maf.user.dto.UserResponse;
import com.maf.user.entity.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt()
        );
    }
}
