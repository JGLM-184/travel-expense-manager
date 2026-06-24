package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.CompanyCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyUpdateDTO;
import com.github.jglm_184.travel_expense_manager.service.CompanyService;
import com.github.jglm_184.travel_expense_manager.util.CompanyCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for CompanyController")
class CompanyControllerTest {

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private CompanyController companyController;

    @BeforeEach
    void setUp() {
        PageImpl<CompanyDetailsDTO> activeCompanyPage = new PageImpl<>(
                List.of(CompanyCreator.createActiveCompanyDetailsDTO()));

        PageImpl<CompanyDetailsDTO> inactiveCompanyPage = new PageImpl<>(
                List.of(CompanyCreator.createInactiveCompanyDetailsDTO()));

        BDDMockito.when(companyService.findAllActiveCompanies(ArgumentMatchers.any()))
                .thenReturn(activeCompanyPage);

        BDDMockito.when(companyService.findAllInactiveCompanies(ArgumentMatchers.any()))
                .thenReturn(inactiveCompanyPage);

        BDDMockito.when(companyService.createCompany(
                        ArgumentMatchers.any(CompanyCreateDTO.class)))
                .thenReturn(CompanyCreator.createActiveCompanyDetailsDTO());

        BDDMockito.when(companyService.updateCompany(
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.any(CompanyUpdateDTO.class)))
                .thenReturn(CompanyCreator.createActiveCompanyDetailsDTO());

        BDDMockito.doNothing()
                .when(companyService)
                .activateCompany(ArgumentMatchers.anyLong());

        BDDMockito.doNothing()
                .when(companyService)
                .deactivateCompany(ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("Returns a page of active companies when companies exist")
    void findAllActiveCompanies_ReturnsPageOfActiveCompanies_WhenCompaniesExist() {
        String expectedCompanyName = CompanyCreator.createActiveCompanyDetailsDTO().getCompanyName();

        ResponseEntity<Page<CompanyDetailsDTO>> responseEntity =
                companyController.findAllActiveCompanies(null);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().toList())
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(responseEntity.getBody()
                        .toList()
                        .get(0)
                        .getCompanyName())
                .isEqualTo(expectedCompanyName);

        Assertions.assertThat(responseEntity.getBody()
                        .toList()
                        .get(0)
                        .isActive())
                .isTrue();
    }

    @Test
    @DisplayName("Returns a page of inactive companies when companies exist")
    void findAllInactiveCompanies_ReturnsPageOfInactiveCompanies_WhenCompaniesExist() {
        String expectedCompanyName =
                CompanyCreator.createInactiveCompanyDetailsDTO().getCompanyName();

        ResponseEntity<Page<CompanyDetailsDTO>> responseEntity =
                companyController.findAllInactiveCompanies(null);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().toList())
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(responseEntity.getBody()
                        .toList()
                        .get(0)
                        .getCompanyName())
                .isEqualTo(expectedCompanyName);

        Assertions.assertThat(responseEntity.getBody()
                        .toList()
                        .get(0)
                        .isActive())
                .isFalse();
    }

    @Test
    @DisplayName("Saves and returns company details when company data is valid")
    void createCompany_SavesAndReturnsCompanyDetails_WhenCompanyDataIsValid() {
        CompanyCreateDTO inputDto =
                CompanyCreator.createValidCompanyCreateDTO();

        CompanyDetailsDTO expectedDetails =
                CompanyCreator.createActiveCompanyDetailsDTO();

        ResponseEntity<CompanyDetailsDTO> responseEntity =
                companyController.createCompany(inputDto);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        Assertions.assertThat(responseEntity.getBody())
                .isNotNull()
                .isEqualTo(expectedDetails);
    }

    @Test
    @DisplayName("Updates and returns company details when company data is valid")
    void updateCompany_UpdatesAndReturnsCompanyDetails_WhenCompanyDataIsValid() {
        CompanyUpdateDTO updateDto =
                CompanyCreator.createValidCompanyUpdateDTO();

        CompanyDetailsDTO expectedDetails =
                CompanyCreator.createActiveCompanyDetailsDTO();

        ResponseEntity<CompanyDetailsDTO> responseEntity =
                companyController.updateCompany(1L, updateDto);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        Assertions.assertThat(responseEntity.getBody())
                .isNotNull()
                .isEqualTo(expectedDetails);
    }

    @Test
    @DisplayName("Deactivates company when company ID is valid")
    void deactivateCompany_DeactivatesCompany_WhenCompanyExists() {
        ResponseEntity<Void> responseEntity =
                companyController.deactivateCompany(1L);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Activates company when company ID is valid")
    void activateCompany_ActivatesCompany_WhenCompanyExists() {
        ResponseEntity<Void> responseEntity =
                companyController.activateCompany(1L);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }
}