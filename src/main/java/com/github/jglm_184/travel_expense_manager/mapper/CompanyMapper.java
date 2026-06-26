package com.github.jglm_184.travel_expense_manager.mapper;

import com.github.jglm_184.travel_expense_manager.dto.CompanyCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyUpdateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ReceitaWSResponse;
import com.github.jglm_184.travel_expense_manager.model.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class CompanyMapper {

    @Mapping(target = "headquarters", ignore = true)
    public abstract Company toCompany(CompanyCreateDTO dto);
    @Mapping(target = "headquarters", ignore = true)
    public abstract Company toCompany(ReceitaWSResponse response);
    public abstract CompanyDetailsDTO toDto(Company company);


}
