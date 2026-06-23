package com.github.jglm_184.travel_expense_manager.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressCreateDTO {
    @NotEmpty(message = "The zipCode of Address cannot be empty or null")
    private String zipCode;
    private String street;
    private String neighborhood;
    private String city;
    private String state;

    public boolean isFullyFilled() {
        return street != null && !street.isBlank() &&
                neighborhood != null && !neighborhood.isBlank() &&
                city != null && !city.isBlank() &&
                state != null && !state.isBlank();
    }
}
