package com.github.jglm_184.travel_expense_manager.util;

import com.github.jglm_184.travel_expense_manager.dto.AddressCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.AddressDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.ViaCepResponse;
import com.github.jglm_184.travel_expense_manager.model.Address;

public class AddressDTOCreator {

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

    public static AddressCreateDTO createAddressCreateDTO() {
        return AddressCreateDTO.builder()
                .zipCode("11111111")
                .street("Praça da Sé")
                .neighborhood("Sé")
                .city("São Paulo")
                .state("São Paulo")
                .build();
    }

    public static ViaCepResponse createViaCepResponse() {
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

    public static AddressCreateDTO createAddressCreateDTONotFullyFilled() {
        return AddressCreateDTO.builder()
                .zipCode("01001000")
                .build();
    }

    public static AddressCreateDTO createInvalidAddressCreateDTO() {
        return AddressCreateDTO.builder()
                .zipCode("111111111")
                .build();
    }

}
