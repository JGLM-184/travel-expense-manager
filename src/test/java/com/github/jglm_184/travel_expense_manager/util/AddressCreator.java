package com.github.jglm_184.travel_expense_manager.util;

import com.github.jglm_184.travel_expense_manager.dto.AddressCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.AddressDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.ViaCepResponse;
import com.github.jglm_184.travel_expense_manager.model.Address;

public class AddressCreator {

    public static Address createValidAddress() {
        return Address.builder()
                .id(1L)
                .zipCode("01001000")
                .street("Praça da Sé")
                .neighborhood("Sé")
                .city("São Paulo")
                .state("São Paulo")
                .build();
    }

    public static Address createValidAddressToBeSaved() {
        return Address.builder()
                .zipCode("01001000")
                .street("Praça da Sé")
                .neighborhood("Sé")
                .city("São Paulo")
                .state("São Paulo")
                .build();
    }

    public static AddressCreateDTO createValidAddressCreateDTO() {
        return AddressCreateDTO.builder()
                .zipCode("01001000")
                .street("Praça da Sé")
                .neighborhood("Sé")
                .city("São Paulo")
                .state("São Paulo")
                .build();
    }

    public static AddressCreateDTO createInvalidAddressCreateDTOFullyFilled() {
        return AddressCreateDTO.builder()
                .zipCode("11111111")
                .street("Praça da Sé")
                .neighborhood("Sé")
                .city("São Paulo")
                .state("São Paulo")
                .build();
    }

    public static ViaCepResponse createValidViaCepResponse() {
        return ViaCepResponse.builder()
                .zipCode("01001000")
                .street("Praça da Sé")
                .neighborhood("Sé")
                .city("São Paulo")
                .state("São Paulo")
                .error(false)
                .build();
    }

    public static ViaCepResponse createViaCepResponseWithError() {
        return ViaCepResponse.builder()
                .error(true)
                .build();
    }

    public static AddressCreateDTO createIncompleteAddressCreateDTO() {
        return AddressCreateDTO.builder()
                .zipCode("01001000")
                .build();
    }

    public static AddressCreateDTO createInvalidAddressCreateDTO() {
        return AddressCreateDTO.builder()
                .zipCode("111111111")
                .build();
    }

    public static AddressDetailsDTO createAddressDetailsDTO() {
        return AddressDetailsDTO.builder()
                .id(1L)
                .zipCode("01001000")
                .street("Praça da Sé")
                .neighborhood("Sé")
                .city("São Paulo")
                .state("São Paulo")
                .build();
    }

    public static Address createAnotherValidAddressToBeSaved() {
        return Address.builder()
                .zipCode("20040002")
                .street("Rua da Assembleia")
                .neighborhood("Centro")
                .city("Rio de Janeiro")
                .state("Rio de Janeiro")
                .build();
    }

    public static Address createOneMoreValidAddressToBeSaved() {
        return Address.builder()
                .street("Rua das Flores")
                .neighborhood("Centro")
                .city("Campinas")
                .state("SP")
                .zipCode("13010000")
                .build();
    }

}
