package com.github.jglm_184.travel_expense_manager.util;

import com.github.jglm_184.travel_expense_manager.dto.UserCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserUpdateDTO;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.model.enums.Role;

public class UserCreator {

    public static User createValidActiveUser() {
        return User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@test.com")
                .password("password123")
                .cpf("12345678901")
                .employeeId("EMP-123")
                .department("ENGINEERING")
                .role(Role.ROLE_ADMIN)
                .active(true)
                .company(CompanyCreator.createValidActiveCompany())
                .build();
    }

    public static UserDetailsDTO createActiveUserDetailsDTO() {
        return UserDetailsDTO.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@test.com")
                .cpf("12345678901")
                .employeeId("EMP-123")
                .department("ENGINEERING")
                .role(Role.ROLE_ADMIN)
                .active(true)
                .build();
    }

    public static UserCreateDTO createValidUserCreateDTO() {
        return UserCreateDTO.builder()
                .name("John Doe")
                .email("john.doe@test.com")
                .password("password123")
                .cpf("123.456.789-01")
                .employeeId("EMP-123")
                .department("Engineering")
                .role(Role.ROLE_ADMIN)
                .companyId(1L)
                .build();
    }

    public static UserUpdateDTO createValidUserUpdateDTO() {
        return UserUpdateDTO.builder()
                .name("John Doe Updated")
                .employeeId("EMP-123-NEW")
                .department("Management")
                .role(Role.ROLE_ADMIN)
                .companyId(1L)
                .build();
    }
}
