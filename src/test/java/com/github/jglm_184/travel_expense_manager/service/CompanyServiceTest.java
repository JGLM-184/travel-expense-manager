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
    private CompanyService companyServiceMock;

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
    @DisplayName("findAllActiveCompanies returns list of active companies inside page object when successful")
    void findAllActiveCompanies_ReturnsListOfActiveCompaniesInsidePageObject_WhenSuccessful() {
        BDDMockito.when(companyMapper.toDto(ArgumentMatchers.any(Company.class)))
                .thenReturn(CompanyCreator.createValidActiveCompanyDTODetails());

        String expectedCompanyName = CompanyCreator.createValidActiveCompany().getCompanyName();

        Page<CompanyDetailsDTO> companyDetailsDTOPage = companyServiceMock.findAllActiveCompanies(
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
    @DisplayName("findAllInactiveCompanies returns list of inactive companies inside page object when successful")
    void findAllInactiveCompanies_ReturnsListOfInactiveCompaniesInsidePageObject_WhenSuccessful() {
        BDDMockito.when(companyMapper.toDto(ArgumentMatchers.any(Company.class)))
                .thenReturn(CompanyCreator.createValidInactiveCompanyDTODetails());

        String expectedCompanyName = CompanyCreator.createValidInactiveCompany().getCompanyName();

        Page<CompanyDetailsDTO> companyDetailsDTOPage = companyServiceMock.findAllInactiveCompanies(
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
    @DisplayName("createCompany saves and returns company details when DTO is fully filled without calling ReceitaWS")
    void createCompany_SavesAndReturnsCompanyDetails_WhenDtoIsFullyFilled() {
        CompanyCreateDTO dto = CompanyCreator.createCompanyCreateDTO();
        Company companyToSave = CompanyCreator.createValidActiveCompany();
        CompanyDetailsDTO expectedResponse = CompanyCreator.createValidActiveCompanyDTODetails();

        BDDMockito.when(addressService.getOrCreateAddress(ArgumentMatchers.any()))
                .thenReturn(AddressCreator.createValidAddress());

        BDDMockito.when(companyMapper.toCompany(ArgumentMatchers.any(CompanyCreateDTO.class)))
                .thenReturn(companyToSave);

        BDDMockito.when(companyRepository.save(ArgumentMatchers.any(Company.class)))
                .thenReturn(companyToSave);

        BDDMockito.when(companyMapper.toDto(ArgumentMatchers.any(Company.class)))
                .thenReturn(expectedResponse);

        CompanyDetailsDTO actualResponse = companyServiceMock.createCompany(dto);

        Assertions.assertThat(actualResponse).isNotNull();
        Assertions.assertThat(actualResponse.getCnpj()).isEqualTo(dto.getCnpj());
        Assertions.assertThat(actualResponse.getCompanyName()).isEqualTo(dto.getCompanyName());
        Assertions.assertThat(actualResponse.isActive()).isTrue();

        BDDMockito.then(addressService).should().getOrCreateAddress(ArgumentMatchers.any());
        BDDMockito.then(companyRepository).should().save(ArgumentMatchers.any(Company.class));
        BDDMockito.verifyNoInteractions(receitaWSClient);
    }

    @Test
    @DisplayName("createCompany fetches data from ReceitaWS, saves and returns company details when DTO contains only CNPJ")
    void createCompany_FetchesFromReceitaWSAndReturnsCompanyDetails_WhenDtoContainsOnlyCnpj() {
        CompanyCreateDTO dto = CompanyCreator.createCompanyCreateDTOWithOnlyCnpj();
        Company companyToSave = CompanyCreator.createValidActiveCompany();
        CompanyDetailsDTO expectedResponse = CompanyCreator.createValidActiveCompanyDTODetails();

        BDDMockito.when(formatterUtil.cleanNumbers(ArgumentMatchers.anyString()))
                .thenReturn("11222333000100");

        BDDMockito.when(receitaWSClient.findCnpj(ArgumentMatchers.anyString()))
                .thenReturn(CompanyCreator.createReceitaWSResponse());

        BDDMockito.when(addressService.getOrCreateAddress(ArgumentMatchers.any()))
                .thenReturn(AddressCreator.createValidAddress());

        BDDMockito.when(companyMapper.toCompany(ArgumentMatchers.any(ReceitaWSResponse.class)))
                .thenReturn(companyToSave);

        BDDMockito.when(companyRepository.save(ArgumentMatchers.any(Company.class)))
                .thenReturn(companyToSave);

        BDDMockito.when(companyMapper.toDto(ArgumentMatchers.any(Company.class)))
                .thenReturn(expectedResponse);

        CompanyDetailsDTO actualResponse = companyServiceMock.createCompany(dto);

        Assertions.assertThat(actualResponse).isNotNull();
        Assertions.assertThat(actualResponse.getCnpj()).isEqualTo(expectedResponse.getCnpj());
        Assertions.assertThat(actualResponse.getCompanyName()).isEqualTo(expectedResponse.getCompanyName());
        Assertions.assertThat(actualResponse.isActive()).isTrue();

        BDDMockito.then(receitaWSClient).should().findCnpj(ArgumentMatchers.anyString());
        BDDMockito.then(addressService).should().getOrCreateAddress(ArgumentMatchers.any());
        BDDMockito.then(companyRepository).should().save(ArgumentMatchers.any(Company.class));
    }

    @Test
    @DisplayName("createCompany throws BusinessException when cnpj already exists when successful")
    void createCompany_ThrowsBusinessException_WhenCnpjAlreadyExists_WhenSuccessful() {
        CompanyCreateDTO dto = CompanyCreator.createCompanyCreateDTO();

        BDDMockito.when(formatterUtil.cleanNumbers(ArgumentMatchers.anyString()))
                .thenReturn("11222333000100");

        BDDMockito.when(companyRepository.findByCnpj("11222333000100"))
                .thenReturn(Optional.of(CompanyCreator.createValidActiveCompany()));

        Throwable thrown = Assertions.catchThrowable(() -> companyServiceMock.createCompany(dto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Company with this CNPJ already exists");

        BDDMockito.verify(companyRepository, BDDMockito.never()).save(ArgumentMatchers.any(Company.class));
    }

    @Test
    @DisplayName("updateCompany updates and returns company details when successful")
    void updateCompany_UpdatesAndReturnsCompanyDetails_WhenSuccessful() {
        CompanyUpdateDTO updateDto = CompanyCreator.createCompanyUpdateDTO();
        Company existingCompany = CompanyCreator.createValidActiveCompany();
        CompanyDetailsDTO expectedResponse = CompanyCreator.createValidActiveCompanyDTODetails();

        BDDMockito.when(companyRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(existingCompany));

        BDDMockito.when(addressService.getOrCreateAddress(ArgumentMatchers.any()))
                .thenReturn(AddressCreator.createValidAddress());

        BDDMockito.when(companyRepository.save(ArgumentMatchers.any(Company.class)))
                .thenReturn(existingCompany);

        BDDMockito.when(companyMapper.toDto(ArgumentMatchers.any(Company.class)))
                .thenReturn(expectedResponse);

        CompanyDetailsDTO actualResponse = companyServiceMock.updateCompany(1L, updateDto);

        Assertions.assertThat(actualResponse).isNotNull();
        BDDMockito.then(companyRepository).should().save(ArgumentMatchers.any(Company.class));
    }

    @Test
    @DisplayName("updateCompany throws ResourceNotFoundException when company is not found or inactive when successful")
    void updateCompany_ThrowsResourceNotFoundException_WhenCompanyNotFoundOrInactive_WhenSuccessful() {
        CompanyUpdateDTO updateDto = CompanyCreator.createCompanyUpdateDTO();

        BDDMockito.when(companyRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        Throwable thrown = Assertions.catchThrowable(() -> companyServiceMock.updateCompany(1L, updateDto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found or is inactive");

        BDDMockito.verify(companyRepository, BDDMockito.never()).save(ArgumentMatchers.any(Company.class));
        BDDMockito.verifyNoInteractions(addressService);
    }

    @Test
    @DisplayName("deactivateCompany deactivates company when successful")
    void deactivateCompany_DeactivatesCompany_WhenSuccessful() {
        Company existingCompany = CompanyCreator.createValidActiveCompany();

        BDDMockito.when(companyRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(existingCompany));

        companyServiceMock.deactivateCompany(1L);

        Assertions.assertThat(existingCompany.isActive()).isFalse();
        BDDMockito.then(companyRepository).should().save(existingCompany);
    }

    @Test
    @DisplayName("deactivateCompany throws ResourceNotFoundException when company not found or already inactive when successful")
    void deactivateCompany_ThrowsResourceNotFoundException_WhenCompanyNotFoundOrAlreadyInactive_WhenSuccessful() {
        BDDMockito.when(companyRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        Throwable thrown = Assertions.catchThrowable(() -> companyServiceMock.deactivateCompany(1L));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found or is inactive");

        BDDMockito.verify(companyRepository, BDDMockito.never()).save(ArgumentMatchers.any(Company.class));
    }

    @Test
    @DisplayName("activateCompany activates company when successful")
    void activateCompany_ActivatesCompany_WhenSuccessful() {
        Company existingInactiveCompany = CompanyCreator.createValidInactiveCompany();

        BDDMockito.when(companyRepository.findByIdAndActiveFalse(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(existingInactiveCompany));

        companyServiceMock.activateCompany(1L);

        Assertions.assertThat(existingInactiveCompany.isActive()).isTrue();
        BDDMockito.then(companyRepository).should().save(existingInactiveCompany);
    }

    @Test
    @DisplayName("activateCompany throws ResourceNotFoundException when company not found or already active when successful")
    void activateCompany_ThrowsResourceNotFoundException_WhenCompanyNotFoundOrAlreadyActive_WhenSuccessful() {
        BDDMockito.when(companyRepository.findByIdAndActiveFalse(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        Throwable thrown = Assertions.catchThrowable(() -> companyServiceMock.activateCompany(1L));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found or is active");

        BDDMockito.verify(companyRepository, BDDMockito.never()).save(ArgumentMatchers.any(Company.class));
    }
}