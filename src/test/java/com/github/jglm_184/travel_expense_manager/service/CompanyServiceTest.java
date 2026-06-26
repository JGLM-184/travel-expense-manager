package com.github.jglm_184.travel_expense_manager.service;

import com.github.jglm_184.travel_expense_manager.client.ReceitaWSClient;
import com.github.jglm_184.travel_expense_manager.dto.CompanyCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyUpdateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ReceitaWSResponse;
import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.exception.ResourceNotFoundException;
import com.github.jglm_184.travel_expense_manager.mapper.CompanyMapper;
import com.github.jglm_184.travel_expense_manager.model.Company;
import com.github.jglm_184.travel_expense_manager.repository.CompanyRepository;
import com.github.jglm_184.travel_expense_manager.util.AddressCreator;
import com.github.jglm_184.travel_expense_manager.util.CompanyCreator;
import com.github.jglm_184.travel_expense_manager.util.FormatterUtil;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for CompanyService")
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyMapper companyMapper;
    @Mock
    private ReceitaWSClient receitaWSClient;
    @Mock
    private AddressService addressService;
    @Mock
    private FormatterUtil formatterUtil;

    @InjectMocks
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        PageImpl<Company> activeCompanyPage = new PageImpl<>(
                List.of(CompanyCreator.createValidActiveCompany()));
        BDDMockito.when(companyRepository.findByActiveTrue(ArgumentMatchers.any(Pageable.class)))
                .thenReturn(activeCompanyPage);

        PageImpl<Company> inactiveCompanyPage = new PageImpl<>(
                List.of(CompanyCreator.createValidInactiveCompany()));
        BDDMockito.when(companyRepository.findByActiveFalse(ArgumentMatchers.any(Pageable.class)))
                .thenReturn(inactiveCompanyPage);
    }

    @Test
    @DisplayName("Returns a page of active companies when companies exist")
    void findAllActiveCompanies_ReturnsPageOfActiveCompanies_WhenCompaniesExist() {
        BDDMockito.when(companyMapper.toDto(ArgumentMatchers.any(Company.class)))
                .thenReturn(CompanyCreator.createActiveCompanyDetailsDTO());

        String expectedCompanyName = CompanyCreator.createValidActiveCompany().getCompanyName();

        Page<CompanyDetailsDTO> companyDetailsDTOPage = companyService.findAllActiveCompanies(
                PageRequest.of(1, 1));

        Assertions.assertThat(companyDetailsDTOPage).isNotNull();

        Assertions.assertThat(companyDetailsDTOPage.toList())
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(companyDetailsDTOPage.toList().get(0).getCompanyName())
                .isEqualTo(expectedCompanyName);
        Assertions.assertThat(companyDetailsDTOPage.toList().get(0).isActive())
                .isEqualTo(CompanyCreator.createValidActiveCompany().isActive());
    }

    @Test
    @DisplayName("Returns a page of inactive companies when companies exist")
    void findAllInactiveCompanies_ReturnsPageOfInactiveCompanies_WhenCompaniesExist() {
        BDDMockito.when(companyMapper.toDto(ArgumentMatchers.any(Company.class)))
                .thenReturn(CompanyCreator.createInactiveCompanyDetailsDTO());

        String expectedCompanyName = CompanyCreator.createValidInactiveCompany().getCompanyName();

        Page<CompanyDetailsDTO> companyDetailsDTOPage = companyService.findAllInactiveCompanies(
                PageRequest.of(1, 1));

        Assertions.assertThat(companyDetailsDTOPage).isNotNull();

        Assertions.assertThat(companyDetailsDTOPage.toList())
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(companyDetailsDTOPage.toList().get(0).getCompanyName())
                .isEqualTo(expectedCompanyName);
        Assertions.assertThat(companyDetailsDTOPage.toList().get(0).isActive())
                .isEqualTo(CompanyCreator.createValidInactiveCompany().isActive());
    }

    @Test
    @DisplayName("Saves and returns company details when DTO is fully filled")
    void createCompany_SavesAndReturnsCompanyDetails_WhenDTOIsFullyFilled() {
        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();
        Company companySaved = CompanyCreator.createValidActiveCompany();
        CompanyDetailsDTO expectedResponse = CompanyCreator.createActiveCompanyDetailsDTO();

        BDDMockito.when(formatterUtil.cleanNumbers(ArgumentMatchers.anyString()))
                .thenReturn("06990590000123");

        BDDMockito.when(addressService.getOrCreateAddress(ArgumentMatchers.any()))
                .thenReturn(AddressCreator.createValidAddress());

        BDDMockito.when(companyMapper.toCompany(ArgumentMatchers.any(CompanyCreateDTO.class)))
                .thenReturn(companySaved);

        BDDMockito.when(companyRepository.save(ArgumentMatchers.any(Company.class)))
                .thenReturn(companySaved);

        BDDMockito.when(companyMapper.toDto(ArgumentMatchers.any(Company.class)))
                .thenReturn(expectedResponse);

        BDDMockito.when(receitaWSClient.findCnpj(ArgumentMatchers.anyString()))
                .thenReturn(CompanyCreator.createValidReceitaWSResponse());

        CompanyDetailsDTO actualResponse = companyService.createCompany(companyToBeSaved);

        Assertions.assertThat(actualResponse).isNotNull();
        Assertions.assertThat(actualResponse.getCnpj()).isEqualTo(companyToBeSaved.getCnpj());
        Assertions.assertThat(actualResponse.getCompanyName()).isEqualTo(companyToBeSaved.getCompanyName());
        Assertions.assertThat(actualResponse.isActive()).isTrue();

        BDDMockito.then(addressService).should().getOrCreateAddress(ArgumentMatchers.any());
        BDDMockito.then(companyRepository).should().save(ArgumentMatchers.any(Company.class));
    }

    @Test
    @DisplayName("Fetches company data from ReceitaWS and returns company details when only CNPJ is provided")
    void createCompany_FetchesDataFromReceitaWSAndReturnsCompanyDetails_WhenOnlyCnpjIsProvided() {
        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTOWithOnlyCnpj();
        Company companySaved = CompanyCreator.createValidActiveCompany();
        CompanyDetailsDTO expectedResponse = CompanyCreator.createActiveCompanyDetailsDTO();

        BDDMockito.when(formatterUtil.cleanNumbers(ArgumentMatchers.anyString()))
                .thenReturn("06990590000123");

        BDDMockito.when(receitaWSClient.findCnpj(ArgumentMatchers.anyString()))
                .thenReturn(CompanyCreator.createValidReceitaWSResponse());

        BDDMockito.when(addressService.getOrCreateAddress(ArgumentMatchers.any()))
                .thenReturn(AddressCreator.createValidAddress());

        BDDMockito.when(companyMapper.toCompany(ArgumentMatchers.any(ReceitaWSResponse.class)))
                .thenReturn(companySaved);

        BDDMockito.when(companyRepository.save(ArgumentMatchers.any(Company.class)))
                .thenReturn(companySaved);

        BDDMockito.when(companyMapper.toDto(ArgumentMatchers.any(Company.class)))
                .thenReturn(expectedResponse);

        CompanyDetailsDTO actualResponse = companyService.createCompany(companyToBeSaved);

        Assertions.assertThat(actualResponse).isNotNull();
        Assertions.assertThat(actualResponse.getCnpj()).isEqualTo(expectedResponse.getCnpj());
        Assertions.assertThat(actualResponse.getCompanyName()).isEqualTo(expectedResponse.getCompanyName());
        Assertions.assertThat(actualResponse.isActive()).isTrue();

        BDDMockito.then(receitaWSClient).should().findCnpj(ArgumentMatchers.anyString());
        BDDMockito.then(addressService).should().getOrCreateAddress(ArgumentMatchers.any());
        BDDMockito.then(companyRepository).should().save(ArgumentMatchers.any(Company.class));
    }

    @Test
    @DisplayName("Throws BusinessException when company CNPJ already exists")
    void createCompany_ThrowsBusinessException_WhenCnpjAlreadyExists() {
        CompanyCreateDTO dto = CompanyCreator.createValidCompanyCreateDTO();

        BDDMockito.when(formatterUtil.cleanNumbers(ArgumentMatchers.anyString()))
                .thenReturn("06990590000123");

        BDDMockito.when(companyRepository.findByCnpj("06990590000123"))
                .thenReturn(Optional.of(CompanyCreator.createValidActiveCompany()));

        Throwable thrown = Assertions.catchThrowable(() -> companyService.createCompany(dto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Company with this CNPJ already exists");

        BDDMockito.verify(companyRepository, BDDMockito.never()).save(ArgumentMatchers.any(Company.class));
    }

    @Test
    @DisplayName("Updates and returns company details when company exists")
    void updateCompany_UpdatesAndReturnsCompanyDetails_WhenCompanyExists() {
        CompanyUpdateDTO updateDto = CompanyCreator.createValidCompanyUpdateDTO();
        Company existingCompany = CompanyCreator.createValidActiveCompany();
        CompanyDetailsDTO expectedResponse = CompanyCreator.createActiveCompanyDetailsDTO();

        BDDMockito.when(companyRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(existingCompany));

        BDDMockito.when(addressService.getOrCreateAddress(ArgumentMatchers.any()))
                .thenReturn(AddressCreator.createValidAddress());

        BDDMockito.when(companyRepository.save(ArgumentMatchers.any(Company.class)))
                .thenReturn(existingCompany);

        BDDMockito.when(companyMapper.toDto(ArgumentMatchers.any(Company.class)))
                .thenReturn(expectedResponse);

        CompanyDetailsDTO actualResponse = companyService.updateCompany(1L, updateDto);

        Assertions.assertThat(actualResponse).isNotNull();
        BDDMockito.then(companyRepository).should().save(ArgumentMatchers.any(Company.class));
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when company is not found or inactive")
    void updateCompany_ThrowsResourceNotFoundException_WhenCompanyIsNotFoundOrInactive() {
        CompanyUpdateDTO updateDto = CompanyCreator.createValidCompanyUpdateDTO();

        BDDMockito.when(companyRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        Throwable thrown = Assertions.catchThrowable(() -> companyService.updateCompany(1L, updateDto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found or is inactive");

        BDDMockito.verify(companyRepository, BDDMockito.never()).save(ArgumentMatchers.any(Company.class));
        BDDMockito.verifyNoInteractions(addressService);
    }

    @Test
    @DisplayName("Deactivates company when company is active")
    void deactivateCompany_DeactivatesCompany_WhenCompanyIsActive() {
        Company existingCompany = CompanyCreator.createValidActiveCompany();

        BDDMockito.when(companyRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(existingCompany));

        companyService.deactivateCompany(1L);

        Assertions.assertThat(existingCompany.isActive()).isFalse();
        BDDMockito.then(companyRepository).should().save(existingCompany);
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when company is not found or already inactive")
    void deactivateCompany_ThrowsResourceNotFoundException_WhenCompanyIsNotFoundOrAlreadyInactive() {
        BDDMockito.when(companyRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        Throwable thrown = Assertions.catchThrowable(() -> companyService.deactivateCompany(1L));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found or is inactive");

        BDDMockito.verify(companyRepository, BDDMockito.never()).save(ArgumentMatchers.any(Company.class));
    }

    @Test
    @DisplayName("Activates company when company is inactive")
    void activateCompany_ActivatesCompany_WhenCompanyIsInactive() {
        Company existingInactiveCompany = CompanyCreator.createValidInactiveCompany();

        BDDMockito.when(companyRepository.findByIdAndActiveFalse(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(existingInactiveCompany));

        companyService.activateCompany(1L);

        Assertions.assertThat(existingInactiveCompany.isActive()).isTrue();
        BDDMockito.then(companyRepository).should().save(existingInactiveCompany);
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when company is not found or already active")
    void activateCompany_ThrowsResourceNotFoundException_WhenCompanyIsNotFoundOrAlreadyActive() {
        BDDMockito.when(companyRepository.findByIdAndActiveFalse(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        Throwable thrown = Assertions.catchThrowable(() -> companyService.activateCompany(1L));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found or is active");

        BDDMockito.verify(companyRepository, BDDMockito.never()).save(ArgumentMatchers.any(Company.class));
    }
}