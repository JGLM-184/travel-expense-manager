package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.ExpenseCategory;
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
public class ExpenseDetailsDTO {

    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private ExpenseCategory category;
    private String receiptCode;
}