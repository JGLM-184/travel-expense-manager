package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Data transfer object containing detailed profile information of a user")
public class UserDetailsDTO {

    @Schema(description = "The unique identifier of the user in the database", example = "1")
    private Long id;

    @Schema(description = "The full name of the user", example = "João Moraes")
    private String name;

    @Schema(description = "The unique corporate registration or identification number for the employee", example = "EMP-1234")
    private String employeeId;

    @Schema(description = "The Brazilian Individual Taxpayer Registry number (CPF)", example = "12345678901")
    private String cpf;

    @Schema(description = "The corporate department or sector the user belongs to", example = "Engineering")
    private String department;

    @Schema(description = "The primary corporate email address of the user", example = "joao.moraes@company.com")
    private String email;

    @Schema(description = "The access level control role assigned to the user within the system", example = "ROLE_EMPLOYEE")
    private Role role;

    @Schema(description = "Indicates whether the user account is currently active and allowed to log into the system", example = "true")
    private boolean active;
}