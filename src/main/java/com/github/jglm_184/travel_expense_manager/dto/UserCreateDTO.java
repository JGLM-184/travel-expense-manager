package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Data transfer object for creating a new user")
public class UserCreateDTO {

    @NotBlank(message = "The name cannot be empty or null")
    @Schema(description = "The full name of the user", example = "João Moraes")
    private String name;

    @Schema(description = "The unique corporate registration or identification number for the employee", example = "EMP-1234")
    private String employeeId;

    @NotBlank(message = "The CPF cannot be empty or null")
    @Schema(description = "The Brazilian Individual Taxpayer Registry number (CPF)", example = "12345678901")
    private String cpf;

    @NotBlank(message = "The department cannot be empty or null")
    @Schema(description = "The corporate department or sector the user belongs to", example = "Engineering")
    private String department;

    @NotBlank(message = "The email cannot be empty or null")
    @Schema(description = "The primary corporate email address of the user", example = "joao.moraes@company.com")
    private String email;

    @NotBlank(message = "The password cannot be empty or null")
    @Schema(description = "The raw password for the user's account (will be encrypted before persistence)",
            example = "securePassword123")
    private String password;

    @Schema(description = "The unique identifier of the company the user belongs to. Optional for ADMIN role, " +
            "as it may be assigned later, and automatically managed for MANAGER role.", example = "1")
    private Long companyId;

    @NotNull(message = "The role cannot be empty or null")
    @Schema(description = "The access level control role assigned to the user within the system", example = "ROLE_EMPLOYEE")
    private Role role;
}