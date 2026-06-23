package com.github.jglm_184.travel_expense_manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReceitaWSResponse {
    @JsonProperty("nome")
    private String companyName;
    @JsonProperty("fantasia")
    private String tradeName;
    private String status;
}
