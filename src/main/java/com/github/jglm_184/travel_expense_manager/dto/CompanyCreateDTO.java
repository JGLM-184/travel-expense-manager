package com.github.jglm_184.travel_expense_manager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyCreateDTO {
    @NotEmpty(message = "The CNPJ cannot be empty or null")
    private String cnpj;

    private String companyName;
    private String tradeName;

    @Valid
    private AddressCreateDTO headquarters;

    public boolean isFullyFilled() {
        return companyName != null && !companyName.isBlank() &&
                tradeName != null && !tradeName.isBlank();
    }
}
