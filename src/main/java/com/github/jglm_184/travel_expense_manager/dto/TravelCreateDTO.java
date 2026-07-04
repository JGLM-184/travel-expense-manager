package com.github.jglm_184.travel_expense_manager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TravelCreateDTO {

    @NotBlank(message = "The travel purpose cannot be empty or null")
    private String purpose;

    @Valid
    @NotNull(message = "The destination address is required")
    private AddressCreateDTO destination;

    @NotNull(message = "The start date is required")
    @FutureOrPresent(message = "The start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "The end date is required")
    private LocalDate endDate;
}