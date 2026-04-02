package com.nexus.banking.userservice.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User registerUser(RegisterUserRequest request) {

        User user = User.builder()
                        .email(request.email())
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .locked(false)
                        .build();

        return userRepository.save(user);
    }
}