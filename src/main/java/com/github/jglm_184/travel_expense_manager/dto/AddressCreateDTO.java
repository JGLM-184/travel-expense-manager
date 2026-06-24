package com.github.jglm_184.travel_expense_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Data transfer object for creating an address")
public class AddressCreateDTO {
    @NotEmpty(message = "The zipCode of Address cannot be empty or null")
    @Schema(description = "The Brazilian postal code (CEP), digits only or formatted", example = "01311200")
    private String zipCode;
    @Schema(description = "Street name or public thoroughfare", example = "Avenida Paulista")
    private String street;
    @Schema(description = "Neighborhood or district name", example = "Bela Vista")
    private String neighborhood;
    @Schema(description = "City name", example = "São Paulo")
    private String city;
    @Schema(description = "State acronym (UF)", example = "São Paulo")
    private String state;

    @Schema(hidden = true)
    public boolean isFullyFilled() {
        return street != null && !street.isBlank() &&
                neighborhood != null && !neighborhood.isBlank() &&
                city != null && !city.isBlank() &&
                state != null && !state.isBlank();
    }
}
