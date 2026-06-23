package com.github.jglm_184.travel_expense_manager.mapper;

import com.github.jglm_184.travel_expense_manager.dto.AddressCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ViaCepResponse;
import com.github.jglm_184.travel_expense_manager.model.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class AddressMapper {
    public abstract Address toAddress(ViaCepResponse response);

    public abstract Address toAddress(AddressCreateDTO addressCreateDTO);
}
