package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.LoginRequest;
import com.github.jglm_184.travel_expense_manager.dto.LoginResponse;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and token generation")
public class AuthController {

    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and generate JWT token",
            description = "Validates user credentials (email and password). If the user exists, is active, and the " +
                    "password matches, it generates a secure JWT token containing user scopes and company tenant ID.",
            security = {},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully authenticated. Returns the JWT token " +
                            "and expiration time"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload, invalid email/password, " +
                            "or user account is currently inactive")
            }
    )
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()
                || !userOptional.get().isActive()
                || !userOptional.get().isLoginCorrect(loginRequest, bCryptPasswordEncoder)) {
            throw new BadCredentialsException("User or password is invalid");
        }

        User user = userOptional.get();
        Instant now = Instant.now();
        Long expiresIn = 3600L;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", user.getRole().name())
                .claim("companyId", user.getCompany() != null ? user.getCompany().getId() : null)
                .build();

        String jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new ResponseEntity<>(new LoginResponse(jwtValue, expiresIn), HttpStatus.OK);
    }
}