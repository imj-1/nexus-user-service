package com.nexus.banking.userservice.api;

import com.nexus.banking.userservice.user.RegisterUserRequest;
import com.nexus.banking.userservice.user.User;
import com.nexus.banking.userservice.user.UserRegistrationOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRegistrationOrchestrator orchestrator;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {

        User user = orchestrator.registerUser(request);

        return new UserResponse(user.getId(), user.getEmail());
    }
}