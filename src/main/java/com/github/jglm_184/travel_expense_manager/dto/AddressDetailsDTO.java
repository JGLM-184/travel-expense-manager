package com.github.jglm_184.travel_expense_manager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDetailsDTO {
    private Long id;
    private String zipCode;
    private String street;
    private String neighborhood;
    private String city;
    private String state;
}
