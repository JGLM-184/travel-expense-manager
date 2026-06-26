package com.github.jglm_184.travel_expense_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Data transfer object containing detailed company information")
public class CompanyDetailsDTO {
    @Schema(description = "The unique identifier of the company in the database", example = "1")
    private Long id;
    @Schema(description = "The Brazilian National Registry of Legal Entities (CNPJ)", example = "06990590000123")
    private String cnpj;
    @Schema(description = "The registered legal corporate name (Razão Social)", example = "Google Brasil Internet Ltda.")
    private String companyName;
    @Schema(description = "The commercial or brand name (Nome Fantasia)", example = "Google")
    private String tradeName;
    @Schema(description = "Indicates whether the company is currently active in the system", example = "true")
    private boolean active;
    @Schema(description = "The detailed address of the company's corporate headquarters")
    private AddressDetailsDTO headquarters;
}
