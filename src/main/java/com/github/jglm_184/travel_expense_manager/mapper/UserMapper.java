package com.github.jglm_184.travel_expense_manager.mapper;

import com.github.jglm_184.travel_expense_manager.dto.UserCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserDetailsDTO;
import com.github.jglm_184.travel_expense_manager.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    public abstract User toUser(UserCreateDTO dto);

    public abstract UserDetailsDTO toDto(User user);
}