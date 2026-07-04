package com.github.jglm_184.travel_expense_manager.mapper;

import com.github.jglm_184.travel_expense_manager.dto.ExpenseCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ExpenseDetailsDTO;
import com.github.jglm_184.travel_expense_manager.model.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class ExpenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "travel", ignore = true)
    public abstract Expense toExpense(ExpenseCreateDTO dto);

    public abstract ExpenseDetailsDTO toDto(Expense expense);
}
