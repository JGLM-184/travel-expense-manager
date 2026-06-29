package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailsDTO {
    private Long id;
    private String name;
    private String employeeId;
    private String cpf;
    private String department;
    private String email;
    private Role role;
    private boolean active;
}