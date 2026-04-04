package com.nexus.banking.userservice.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

        @Email @NotBlank String email,

        @NotBlank String firstName,

        @NotBlank String lastName,

        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password

        /* TODO: Add these fields for proper production form
            Middle Name (Optional)
            Street Address
            Unit Number (Optional)
            City
            State (Drop down select/option)
            zip code
            phone number
            phone type (Mobile, Home, Other)
            Date of birth
          */
) {}