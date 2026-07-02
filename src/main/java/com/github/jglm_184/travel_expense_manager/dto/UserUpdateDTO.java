package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@Schema(description = "Data transfer object used to update an existing user's profile information")
public class UserUpdateDTO {

    @NotBlank(message = "The name cannot be empty or null")
    @Schema(description = "The updated full name of the user", example = "João Moraes")
    private String name;

    @Schema(description = "The updated unique corporate registration or identification number for the employee",
            example = "EMP-1234")
    private String employeeId;

    @NotBlank(message = "The department cannot be empty or null")
    @Schema(description = "The updated corporate department or sector the user belongs to", example = "Engineering")
    private String department;

    @Schema(description = "The unique identifier of the company the user is being moved to. Only processable by " +
            "ADMIN roles; for MANAGER roles, company binding remains isolated.", example = "1")
    private Long companyId;

    @NotNull(message = "The role cannot be empty or null")
    @Schema(description = "The updated access level control role assigned to the user within the system",
            example = "ROLE_EMPLOYEE")
    private Role role;
}