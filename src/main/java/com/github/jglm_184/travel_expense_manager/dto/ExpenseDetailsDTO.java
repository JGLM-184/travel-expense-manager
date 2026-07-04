package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.ExpenseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "ExpenseDetails", description = "Detailed representation of an individual corporate expense record")
public class ExpenseDetailsDTO {

    @Schema(description = "Unique identifier of the expense record", example = "250",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Brief detail or name of the specific expense incurred",
            example = "Uber from Airport to Hotel", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "Total cost value of the expense item",
            example = "75.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "The exact date when the expense was paid",
            example = "2026-08-16", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    @Schema(description = "The type classification of the expense (e.g., MEALS, LODGING, TRANSPORT, OTHERS)",
            example = "TRANSPORT", requiredMode = Schema.RequiredMode.REQUIRED)
    private ExpenseCategory category;

    @Schema(description = "Voucher or receipt code identifier used for compliance and proof of purchase verification",
            example = "REC-99214-XYZ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String receiptCode;
}