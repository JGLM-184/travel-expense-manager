package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.TravelStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Schema(name = "TravelDetails", description = "Detailed representation of a corporate travel report including " +
        "associated requester information and expenses")
public class TravelDetailsDTO {

    @Schema(description = "Unique identifier of the travel report", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Brief explanation or business reason for the travel request",
            example = "Tech Conference 2026", requiredMode = Schema.RequiredMode.REQUIRED)
    private String purpose;

    @Schema(description = "Detailed address structure of the travel destination",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private AddressDetailsDTO destination;

    @Schema(description = "The departure or starting date of the corporate trip",
            example = "2026-08-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @Schema(description = "The return or completion date of the corporate trip",
            example = "2026-08-20", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @Schema(description = "Current lifecycle status of the travel report (e.g., OPEN, SUBMITTED, APPROVED, REJECTED)",
            example = "OPEN", requiredMode = Schema.RequiredMode.REQUIRED)
    private TravelStatus status;

    @Schema(description = "Unique identifier of the employee who requested the travel",
            example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "Full name of the employee who requested the travel",
            example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

    @Schema(description = "List of individual expenses attached to this specific travel report",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ExpenseDetailsDTO> expenses;
}