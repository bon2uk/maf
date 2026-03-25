package com.maf.user.service;

import com.maf.user.dto.UpdateUserRequest;
import com.maf.user.entity.User;
import com.maf.user.model.UserStatus;
import com.maf.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUserProfile(UUID id, String email, String firstName, String lastName, UserStatus status) {
        User user = User.create(id, email, firstName, lastName, status);
        return userRepository.save(user);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(UUID id, UpdateUserRequest updateUserRequest) {
        User user = getUserById(id);
        user.updateProfile(updateUserRequest.firstName(), updateUserRequest.lastName());

        return userRepository.save(user);
    }
}