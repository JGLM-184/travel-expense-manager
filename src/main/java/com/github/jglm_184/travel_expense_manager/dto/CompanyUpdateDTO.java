package com.github.jglm_184.travel_expense_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Data transfer object used to update company information")
public class CompanyUpdateDTO {
    @Schema(description = "The unique identifier of the company to be updated", example = "1")
    private Long id;
    @Schema(description = "The registered legal corporate name (Razão Social)", example = "Google Brasil Internet Ltda.")
    private String companyName;
    @Schema(description = "The registered legal corporate name (Razão Social)", example = "Google Brasil Internet Ltda.")
    private String tradeName;
    @Valid
    @Schema(description = "The updated headquarters address data. If omitted, the address remains unchanged.")
    private AddressCreateDTO headquarters;
}
