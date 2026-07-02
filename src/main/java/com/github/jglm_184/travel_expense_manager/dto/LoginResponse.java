package com.github.jglm_184.travel_expense_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Data transfer object containing successful authentication details and the secure token")
public class LoginResponse {

    @Schema(description = "The secure JSON Web Token (JWT) to be used as a Bearer token for authorized requests",
            example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "The time to live (TTL) of the generated access token in seconds", example = "3600")
    private Long expiresIn;
}