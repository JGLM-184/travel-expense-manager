package com.github.jglm_184.travel_expense_manager.dto;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyUpdateDTO {
    private Long id;
    private String companyName;
    private String tradeName;
    @Valid
    private AddressCreateDTO headquarters;
}
