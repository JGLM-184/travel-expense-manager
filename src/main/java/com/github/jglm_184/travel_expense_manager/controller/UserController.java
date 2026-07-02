package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.UserCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserUpdateDTO;
import com.github.jglm_184.travel_expense_manager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "Endpoints for managing users, corporate roles, and account statuses")
public class UserController {

    private final UserService userService;

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "List all active users paginated",
            description = "Retrieves a paginated list of active users. If authenticated as an ADMIN, lists all active " +
                    "users across the system. If authenticated as a MANAGER, filters and lists only users belonging to " +
                    "the same company. Supports optional filtering by name and department.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of active users"),
                    @ApiResponse(responseCode = "400", description = "Invalid pagination, sorting, or filtering parameters " +
                            "provided"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "403", description = "Forbidden. Authenticated user does not have the " +
                            "ADMIN or MANAGER role")
            }
    )
    public ResponseEntity<Page<UserDetailsDTO>> findAllActiveUsers(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String department,
            @ParameterObject Pageable pageable) {
        return new ResponseEntity<>(userService.findAllActiveUsers(name, department, pageable), HttpStatus.OK);
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "List all inactive users paginated",
            description = "Retrieves a paginated list of inactive users. If authenticated as an ADMIN, lists all " +
                    "inactive users across the system. If authenticated as a MANAGER, filters and lists only inactive " +
                    "users belonging to the same company.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of inactive users"),
                    @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameters provided"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "403", description = "Forbidden. Authenticated user does not have the " +
                            "ADMIN or MANAGER role")
            }
    )
    public ResponseEntity<Page<UserDetailsDTO>> findAllInactiveUsers(@ParameterObject Pageable pageable) {
        return new ResponseEntity<>(userService.findAllInactiveUsers(pageable), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Create a new user",
            description = "Registers a new user into the system. If authenticated as an ADMIN, the user can be assigned " +
                    "to any company. If authenticated as a MANAGER, the new user will be automatically tied to the " +
                    "manager's company regardless of the payload.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User successfully created"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload, validation failed, or " +
                            "unique attributes (email, CPF, employeeId) already exist"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "403", description = "Forbidden. Authenticated user does not have the " +
                            "ADMIN or MANAGER role")
            }
    )
    public ResponseEntity<UserDetailsDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        return new ResponseEntity<>(userService.createUser(userCreateDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Update an existing user",
            description = "Updates registration profile data of a user by their unique ID. If authenticated as a MANAGER, " +
                    "the target user must belong to the same company, otherwise access will be denied.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User details successfully updated"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or data validation failed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "403", description = "Forbidden. Authenticated user does not have the " +
                            "ADMIN or MANAGER role"),
                    @ApiResponse(responseCode = "404", description = "User not found (or cross-company access attempted " +
                            "by a MANAGER)")
            }
    )
    public ResponseEntity<UserDetailsDTO> updateUser(@PathVariable Long id,
                                                     @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        return new ResponseEntity<>(userService.updateUser(id, userUpdateDTO), HttpStatus.OK);
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Activate a user account by ID",
            description = "Changes the target user's active status back to true, allowing them to access the system again. " +
                    "If authenticated as a MANAGER, the target user must belong to the same company.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User successfully activated"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "403", description = "Forbidden. Authenticated user does not have the " +
                            "ADMIN or MANAGER role"),
                    @ApiResponse(responseCode = "404", description = "User not found, already active (or cross-company " +
                            "access attempted by a MANAGER)")
            }
    )
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Deactivate a user account by ID",
            description = "Changes the target user's active status to false. Inactive users cannot log into the " +
                    "application. If authenticated as a MANAGER, the target user must belong to the same company.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User successfully deactivated"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "403", description = "Forbidden. Authenticated user does not have the " +
                            "ADMIN or MANAGER role"),
                    @ApiResponse(responseCode = "404", description = "User not found, already inactive (or cross-company " +
                            "access attempted by a MANAGER)")
            }
    )
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}