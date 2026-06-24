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
                .cnpj("06990590000123")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .headquarters(AddressCreator.createValidAddress())
                .active(true)
                .build();
    }

    public static Company createValidActiveCompanyToBeSaved() {
        return Company.builder()
                .cnpj("06990590000123")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .active(true)
                .build();
    }

    public static Company createValidInactiveCompany() {
        return Company.builder()
                .id(1L)
                .cnpj("06990590000123")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .headquarters(AddressCreator.createValidAddress())
                .active(false)
                .build();
    }

    public static Company createValidInactiveCompanyToBeSaved() {
        return Company.builder()
                .cnpj("06990590000123")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .active(false)
                .build();
    }

    public static CompanyDetailsDTO createActiveCompanyDetailsDTO() {
        return CompanyDetailsDTO.builder()
                .id(1L)
                .cnpj("06990590000123")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .headquarters(AddressCreator.createAddressDetailsDTO())
                .active(true)
                .build();
    }

    public static CompanyDetailsDTO createInactiveCompanyDetailsDTO() {
        return CompanyDetailsDTO.builder()
                .id(1L)
                .cnpj("06990590000123")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .headquarters(AddressCreator.createAddressDetailsDTO())
                .active(false)
                .build();
    }

    public static CompanyCreateDTO createValidCompanyCreateDTO() {
        return CompanyCreateDTO.builder()
                .cnpj("06990590000123")
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .headquarters(AddressCreator.createValidAddressCreateDTO())
                .build();
    }

    public static ReceitaWSResponse createValidReceitaWSResponse() {
        return com.github.jglm_184.travel_expense_manager.dto.ReceitaWSResponse.builder()
                .companyName("Test Enterprise LTDA")
                .tradeName("Test Company")
                .status("OK")
                .build();
    }

    public static CompanyCreateDTO createValidCompanyCreateDTOWithOnlyCnpj() {
        return CompanyCreateDTO.builder()
                .cnpj("06990590000123")
                .build();
    }

    public static CompanyUpdateDTO createValidCompanyUpdateDTO() {
        return CompanyUpdateDTO.builder()
                .companyName("Updated Enterprise Name LTDA")
                .tradeName("Updated Company Brand")
                .headquarters(AddressCreator.createValidAddressCreateDTO())
                .build();
    }
}
