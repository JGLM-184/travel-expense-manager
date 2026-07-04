package com.github.jglm_184.travel_expense_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(name = "TravelUpdate", description = "Payload required to update an existing open corporate travel report")
public class TravelUpdateDTO {

    @NotBlank(message = "The travel purpose cannot be empty or null")
    @Schema(description = "Updated explanation or business reason for the travel request",
            example = "Tech Conference 2026 Updated / Client Annual Meeting", requiredMode = Schema.RequiredMode.REQUIRED)
    private String purpose;

    @Valid
    @NotNull(message = "The destination address is required")
    @Schema(description = "Updated detailed address structure of the travel destination",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private AddressCreateDTO destination;

    @NotNull(message = "The start date is required")
    @Schema(description = "The updated departure or starting date of the corporate trip",
            example = "2026-08-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "The end date is required")
    @Schema(description = "The updated return or completion date of the corporate trip",
            example = "2026-08-20", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;
}