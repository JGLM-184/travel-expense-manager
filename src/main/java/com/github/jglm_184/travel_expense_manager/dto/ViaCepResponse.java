package com.github.jglm_184.travel_expense_manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ViaCepResponse {
    @JsonProperty("cep")
    private String zipCode;
    @JsonProperty("logradouro")
    private String street;
    @JsonProperty("bairro")
    private String neighborhood;
    @JsonProperty("localidade")
    private String city;
    @JsonProperty("estado")
    private String state;
    @JsonProperty("erro")
    private boolean error;
}