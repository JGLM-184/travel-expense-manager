package com.github.jglm_184.travel_expense_manager.util;

import com.github.jglm_184.travel_expense_manager.dto.CompanyCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyUpdateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ReceitaWSResponse;
import com.github.jglm_184.travel_expense_manager.model.Company;

public class CompanyCreator {

    public static Company createValidActiveCompany() {
        return Company.builder()
                .id(1L)
                .cnpj("11222333000100")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .headquarters(AddressCreator.createValidAddress())
                .active(true)
                .build();
    }

    public static Company createValidInactiveCompany() {
        return Company.builder()
                .id(1L)
                .cnpj("11222333000100")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .headquarters(AddressCreator.createValidAddress())
                .active(false)
                .build();
    }

    public static CompanyDetailsDTO createValidActiveCompanyDTODetails() {
        return CompanyDetailsDTO.builder()
                .id(1L)
                .cnpj("11222333000100")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .headquarters(AddressCreator.createAddressDetailsDTO())
                .active(true)
                .build();
    }

    public static CompanyDetailsDTO createValidInactiveCompanyDTODetails() {
        return CompanyDetailsDTO.builder()
                .id(1L)
                .cnpj("11222333000100")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .headquarters(AddressCreator.createAddressDetailsDTO())
                .active(false)
                .build();
    }

    public static CompanyCreateDTO createCompanyCreateDTO() {
        return CompanyCreateDTO.builder()
                .cnpj("11222333000100")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .headquarters(AddressCreator.createAddressCreateDTO())
                .build();
    }

    public static ReceitaWSResponse createReceitaWSResponse() {
        return com.github.jglm_184.travel_expense_manager.dto.ReceitaWSResponse.builder()
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .status("OK")
                .build();
    }

    public static CompanyCreateDTO createCompanyCreateDTOWithOnlyCnpj() {
        return CompanyCreateDTO.builder()
                .cnpj("11222333000100")
                .build();
    }

    public static CompanyUpdateDTO createCompanyUpdateDTO() {
        return CompanyUpdateDTO.builder()
                .companyName("Updated Enterprise Name LTDA")
                .tradeName("Updated Company Brand")
                .headquarters(AddressCreator.createAddressCreateDTO())
                .build();
    }

    public static CompanyUpdateDTO createCompanyUpdateDTOWithBlankFields() {
        return CompanyUpdateDTO.builder()
                .companyName("")
                .tradeName(null)
                .headquarters(AddressCreator.createAddressCreateDTO())
                .build();
    }


}
