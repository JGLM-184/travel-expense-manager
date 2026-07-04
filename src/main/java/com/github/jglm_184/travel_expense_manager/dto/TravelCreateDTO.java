package com.github.jglm_184.travel_expense_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(name = "TravelCreate", description = "Payload required to create a new corporate travel report")
public class TravelCreateDTO {

    @NotBlank(message = "The travel purpose cannot be empty or null")
    @Schema(description = "Brief explanation or business reason for the travel request",
            example = "Tech Conference 2026 / Client Annual Meeting", requiredMode = Schema.RequiredMode.REQUIRED)
    private String purpose;

    @Valid
    @NotNull(message = "The destination address is required")
    @Schema(description = "Detailed address structure of the travel destination",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private AddressCreateDTO destination;

    @NotNull(message = "The start date is required")
    @FutureOrPresent(message = "The start date must be today or in the future")
    @Schema(description = "The departure or starting date of the corporate trip (must be today or a future date)",
            example = "2026-08-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "The end date is required")
    @Schema(description = "The return or completion date of the corporate trip",
            example = "2026-08-20", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;
}