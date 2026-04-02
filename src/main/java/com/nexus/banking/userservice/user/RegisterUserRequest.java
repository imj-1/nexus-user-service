package com.nexus.banking.userservice.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(

        @Email @NotBlank String email,

        @NotBlank String firstName,

        @NotBlank String lastName
) {}