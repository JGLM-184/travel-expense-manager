package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreateDTO {

    @NotBlank(message = "The name cannot be empty or null")
    private String name;

    private String employeeId;

    @NotBlank(message = "The CPF cannot be empty or null")
    private String cpf;

    @NotBlank(message = "The department cannot be empty or null")
    private String department;

    @NotBlank(message = "The email cannot be empty or null")
    private String email;

    @NotBlank(message = "The password cannot be empty or null")
    private String password;

    private Long companyId;

    @NotNull(message = "The role cannot be empty or null")
    private Role role;
}