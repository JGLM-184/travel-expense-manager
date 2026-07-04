package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.ExpenseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(name = "ExpenseCreate", description = "Payload required to attach a new expense to an open travel report")
public class ExpenseCreateDTO {

    @NotBlank(message = "The expense description cannot be empty or null")
    @Schema(description = "Brief detail or name of the specific expense incurred",
            example = "Uber from Airport to Hotel", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull(message = "The expense amount is required")
    @DecimalMin(value = "0.01", message = "The expense amount must be greater than zero")
    @Schema(description = "Total cost value of the expense item",
            example = "75.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull(message = "The expense date is required")
    @Schema(description = "The exact date when the expense was paid",
            example = "2026-08-16", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    @NotNull(message = "The expense category is required")
    @Schema(description = "The type classification of the expense (e.g., MEALS, LODGING, TRANSPORT, OTHERS)",
            example = "TRANSPORT", requiredMode = Schema.RequiredMode.REQUIRED)
    private ExpenseCategory category;

    @Schema(description = "Optional dynamic voucher or receipt code identifier for proof of purchase validation",
            example = "REC-99214-XYZ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String receiptCode;
}