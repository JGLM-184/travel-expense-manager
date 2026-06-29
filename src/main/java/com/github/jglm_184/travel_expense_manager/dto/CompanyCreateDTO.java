package com.github.jglm_184.travel_expense_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Data transfer object for creating a company")
public class CompanyCreateDTO {
    @NotBlank(message = "The CNPJ cannot be empty or null")
    @Schema(description = "The Brazilian National Registry of Legal Entities (CNPJ)", example = "06990590000123")
    private String cnpj;
    @Schema(description = "The registered legal corporate name (Razão Social)", example = "Google Brasil Internet Ltda.")
    private String companyName;
    @Schema(description = "The commercial or brand name (Nome Fantasia)", example = "Google")
    private String tradeName;
    @Valid
    @Schema(description = "The detailed address of the company's corporate headquarters")
    private AddressCreateDTO headquarters;

    @Schema(hidden = true)
    public boolean isFullyFilled() {
        return companyName != null && !companyName.isBlank() &&
                tradeName != null && !tradeName.isBlank();
    }
}
