package com.github.jglm_184.travel_expense_manager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {
    @NotBlank(message = "The email is required.")
    private String email;
    @NotBlank(message = "The password is required.")
    private String password;
}
