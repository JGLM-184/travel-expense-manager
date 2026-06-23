package com.github.jglm_184.travel_expense_manager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyDetailsDTO {
    private Long id;
    private String cnpj;
    private String companyName;
    private String tradeName;
    private boolean active;
    private AddressDetailsDTO headquarters;
}
