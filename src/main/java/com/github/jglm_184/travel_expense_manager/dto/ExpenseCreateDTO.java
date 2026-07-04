package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCreateDTO {

    @NotBlank(message = "The expense description cannot be empty or null")
    private String description;

    @NotNull(message = "The expense amount is required")
    @DecimalMin(value = "0.01", message = "The expense amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "The expense date is required")
    private LocalDate date;

    @NotNull(message = "The expense category is required")
    private ExpenseCategory category;

    private String receiptCode;
}