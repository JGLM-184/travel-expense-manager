package com.github.jglm_184.travel_expense_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Data transfer object containing detailed address information")
public class AddressDetailsDTO {
    @Schema(description = "The unique identifier of the address in the database", example = "1")
    private Long id;
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
}
