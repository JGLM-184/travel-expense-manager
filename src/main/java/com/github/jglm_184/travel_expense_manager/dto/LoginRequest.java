package com.github.jglm_184.travel_expense_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Data transfer object representing a user authentication request")
public class LoginRequest {

    @NotBlank(message = "The email is required.")
    @Schema(description = "The corporate email address used for login authentication", example = "joao.moraes@company.com")
    private String email;

    @NotBlank(message = "The password is required.")
    @Schema(description = "The raw password string associated with the user account", example = "securePassword123")
    private String password;
}