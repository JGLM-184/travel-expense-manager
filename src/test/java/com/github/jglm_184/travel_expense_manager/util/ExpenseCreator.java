package com.github.jglm_184.travel_expense_manager.util;

import com.github.jglm_184.travel_expense_manager.dto.ExpenseCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ExpenseDetailsDTO;
import com.github.jglm_184.travel_expense_manager.model.Expense;
import com.github.jglm_184.travel_expense_manager.model.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseCreator {

    public static Expense createValidExpense() {
        return Expense.builder()
                .id(1L)
                .description("Lunch during business trip")
                .amount(new BigDecimal("89.90"))
                .date(LocalDate.of(2026, 8, 16))
                .category(ExpenseCategory.MEALS)
                .receiptCode("RCPT-12345")
                .build();
    }

    public static Expense createValidExpenseToBeSaved() {
        return Expense.builder()
                .description("Lunch during business trip")
                .amount(new BigDecimal("89.90"))
                .date(LocalDate.of(2026, 8, 16))
                .category(ExpenseCategory.MEALS)
                .receiptCode("RCPT-12345")
                .build();
    }

    public static ExpenseCreateDTO createValidExpenseCreateDTO() {
        return ExpenseCreateDTO.builder()
                .description("Lunch during business trip")
                .amount(new BigDecimal("89.90"))
                .date(LocalDate.of(2026, 8, 16))
                .category(ExpenseCategory.MEALS)
                .receiptCode("RCPT-12345")
                .build();
    }

    public static ExpenseCreateDTO createExpenseCreateDTOWithInvalidDate() {
        return ExpenseCreateDTO.builder()
                .description("Lunch during business trip")
                .amount(new BigDecimal("89.90"))
                .date(LocalDate.of(2026, 8, 25))
                .category(ExpenseCategory.MEALS)
                .receiptCode("RCPT-12345")
                .build();
    }

    public static ExpenseDetailsDTO createValidExpenseDetailsDTO() {
        return ExpenseDetailsDTO.builder()
                .id(1L)
                .description("Lunch during business trip")
                .amount(new BigDecimal("89.90"))
                .date(LocalDate.of(2026, 8, 16))
                .category(ExpenseCategory.MEALS)
                .receiptCode("RCPT-12345")
                .build();
    }
}