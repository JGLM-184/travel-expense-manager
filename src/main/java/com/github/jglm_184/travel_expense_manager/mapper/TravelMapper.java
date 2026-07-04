package com.github.jglm_184.travel_expense_manager.mapper;

import com.github.jglm_184.travel_expense_manager.dto.TravelCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelDetailsDTO;
import com.github.jglm_184.travel_expense_manager.model.Travel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class TravelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "destination", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    public abstract Travel toTravel(TravelCreateDTO dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    public abstract TravelDetailsDTO toDto(Travel travel);

}
