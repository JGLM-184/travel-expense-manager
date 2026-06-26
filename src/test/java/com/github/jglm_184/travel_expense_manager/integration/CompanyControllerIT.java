package com.github.jglm_184.travel_expense_manager.integration;

import com.github.jglm_184.travel_expense_manager.dto.AddressCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyUpdateDTO;
import com.github.jglm_184.travel_expense_manager.model.Address;
import com.github.jglm_184.travel_expense_manager.model.Company;
import com.github.jglm_184.travel_expense_manager.repository.AddressRepository;
import com.github.jglm_184.travel_expense_manager.repository.CompanyRepository;
import com.github.jglm_184.travel_expense_manager.util.AddressCreator;
import com.github.jglm_184.travel_expense_manager.util.CompanyCreator;
import com.github.jglm_184.travel_expense_manager.util.RestResponsePage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CompanyControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Test
    @DisplayName("Returns a page of active companies when companies exist")
    void findAllActiveCompanies_ReturnsPageOfActiveCompanies_WhenCompaniesExist() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companySaved = CompanyCreator.createValidActiveCompanyToBeSaved();
        companySaved.setHeadquarters(addressSaved);
        companyRepository.save(companySaved);

        String expectedCompanyName = companySaved.getCompanyName();
        String expectedCnpj = companySaved.getCnpj();

        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/active", HttpMethod.GET, null,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        List<CompanyDetailsDTO> companyDetailsDTOList = response.getBody().toList();

        Assertions.assertThat(companyDetailsDTOList)
                .isNotNull()
                .isNotEmpty();
        Assertions.assertThat(companyDetailsDTOList.get(0).getCompanyName()).isEqualTo(expectedCompanyName);
        Assertions.assertThat(companyDetailsDTOList.get(0).getCnpj()).isEqualTo(expectedCnpj);
        Assertions.assertThat(companyDetailsDTOList.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("Returns a void page of active companies when companies does not exist")
    void findAllActiveCompanies_ReturnsVoidPageOfActiveCompanies_WhenCompaniesDoesNotExist() {
        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/active", HttpMethod.GET, null,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        List<CompanyDetailsDTO> companyDetailsDTOList = response.getBody().toList();

        Assertions.assertThat(companyDetailsDTOList)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Returns a page of inactive companies when companies exist")
    void findAllInactiveCompanies_ReturnsPageOfInactiveCompanies_WhenCompaniesExist() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companySaved = CompanyCreator.createValidInactiveCompanyToBeSaved();
        companySaved.setHeadquarters(addressSaved);
        companyRepository.save(companySaved);

        String expectedCompanyName = companySaved.getCompanyName();
        String expectedCnpj = companySaved.getCnpj();

        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/inactive", HttpMethod.GET, null,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        List<CompanyDetailsDTO> companyDetailsDTOList = response.getBody().toList();

        Assertions.assertThat(companyDetailsDTOList)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
        Assertions.assertThat(companyDetailsDTOList.get(0).getCompanyName()).isEqualTo(expectedCompanyName);
        Assertions.assertThat(companyDetailsDTOList.get(0).getCnpj()).isEqualTo(expectedCnpj);
        Assertions.assertThat(companyDetailsDTOList.get(0).isActive()).isFalse();

    }

    @Test
    @DisplayName("Returns a void page of inactive companies when companies does not exist")
    void findAllActiveCompanies_ReturnsVoidPageOfInactiveCompanies_WhenCompaniesDoesNotExist() {
        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/inactive", HttpMethod.GET, null,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        List<CompanyDetailsDTO> companyDetailsDTOList = response.getBody().toList();

        Assertions.assertThat(companyDetailsDTOList)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Saves and returns company details when company data is valid")
    void createCompany_SavesAndReturnsCompanyDetails_WhenCompanyDataIsValid() {
        AddressCreateDTO addressToBeSaved = AddressCreator.createValidAddressCreateDTO();

        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);
        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.postForEntity("/companies",
                companyToBeSaved, CompanyDetailsDTO.class);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getId()).isNotNull();

        Long generatedId = response.getBody().getId();
        Optional<Company> companyInDatabase = companyRepository.findById(generatedId);

        Assertions.assertThat(companyInDatabase).isPresent();
        Assertions.assertThat(companyInDatabase.get().getCompanyName())
                .isEqualTo(companyToBeSaved.getCompanyName());
        Assertions.assertThat(companyInDatabase.get().getCnpj())
                .isEqualTo(companyToBeSaved.getCnpj());

    }

    @Test
    @DisplayName("Returns 400 bad request when company cnpj is empty")
    void createCompany_Returns400BadRequest_WhenCompanyCnpjIsEmpty() {
        AddressCreateDTO addressToBeSaved = AddressCreator.createValidAddressCreateDTO();

        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);
        companyToBeSaved.setCnpj("");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.postForEntity("/companies",
                companyToBeSaved, CompanyDetailsDTO.class);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);


        Assertions.assertThat(response.getBody().getId()).isNull();
        Assertions.assertThat(response.getBody().getCnpj()).isNull();
        Assertions.assertThat(response.getBody().getCompanyName()).isNull();
        Assertions.assertThat(response.getBody().getTradeName()).isNull();
        Assertions.assertThat(response.getBody().isActive()).isFalse();
        Assertions.assertThat(response.getBody().getHeadquarters()).isNull();

        Optional<Company> companyInDatabase = companyRepository.findById(1L);

        Assertions.assertThat(companyInDatabase).isNotPresent();
    }

    @Test
    @DisplayName("Returns 400 bad request when company cnpj is null")
    void createCompany_Returns400BadRequest_WhenCompanyCnpjIsNull() {
        AddressCreateDTO addressToBeSaved = AddressCreator.createValidAddressCreateDTO();

        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);
        companyToBeSaved.setCnpj(null);

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.postForEntity("/companies",
                companyToBeSaved, CompanyDetailsDTO.class);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);


        Assertions.assertThat(response.getBody().getId()).isNull();
        Assertions.assertThat(response.getBody().getCnpj()).isNull();
        Assertions.assertThat(response.getBody().getCompanyName()).isNull();
        Assertions.assertThat(response.getBody().getTradeName()).isNull();
        Assertions.assertThat(response.getBody().isActive()).isFalse();
        Assertions.assertThat(response.getBody().getHeadquarters()).isNull();

        Optional<Company> companyInDatabase = companyRepository.findById(1L);

        Assertions.assertThat(companyInDatabase).isNotPresent();
    }

    @Test
    @DisplayName("Returns 400 bad request when company cnpj is invalid")
    void createCompany_Returns400BadRequest_WhenCompanyCnpjIsInvalid() {
        AddressCreateDTO addressToBeSaved = AddressCreator.createValidAddressCreateDTO();

        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);
        companyToBeSaved.setCnpj("00.000.000/0001-00");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.postForEntity("/companies",
                companyToBeSaved, CompanyDetailsDTO.class);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);


        Assertions.assertThat(response.getBody().getId()).isNull();
        Assertions.assertThat(response.getBody().getCnpj()).isNull();
        Assertions.assertThat(response.getBody().getCompanyName()).isNull();
        Assertions.assertThat(response.getBody().getTradeName()).isNull();
        Assertions.assertThat(response.getBody().isActive()).isFalse();
        Assertions.assertThat(response.getBody().getHeadquarters()).isNull();

        Optional<Company> companyInDatabase = companyRepository.findById(1L);

        Assertions.assertThat(companyInDatabase).isNotPresent();
    }

    @Test
    @DisplayName("Returns 400 bad request when company cnpj is already registered")
    void createCompany_Returns400BadRequest_WhenCompanyCnpjIsAlreadyRegistered() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companySaved = CompanyCreator.createValidActiveCompanyToBeSaved();
        companySaved.setHeadquarters(addressSaved);
        companyRepository.save(companySaved);

        AddressCreateDTO addressToBeSaved = AddressCreator.createValidAddressCreateDTO();

        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.postForEntity("/companies",
                companyToBeSaved, CompanyDetailsDTO.class);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);


        Assertions.assertThat(response.getBody().getId()).isNull();
        Assertions.assertThat(response.getBody().getCnpj()).isNull();
        Assertions.assertThat(response.getBody().getCompanyName()).isNull();
        Assertions.assertThat(response.getBody().getTradeName()).isNull();
        Assertions.assertThat(response.getBody().isActive()).isFalse();
        Assertions.assertThat(response.getBody().getHeadquarters()).isNull();

        Optional<Company> companyInDatabase = companyRepository.findById(2L);

        Assertions.assertThat(companyInDatabase).isNotPresent();
    }

    @Test
    @DisplayName("Returns 400 bad request when company head quarters is empty")
    void createCompany_Returns400BadRequest_WhenCompanyHeadQuartersIsEmpty() {
        AddressCreateDTO addressToBeSaved = AddressCreator.createValidAddressCreateDTO();
        addressToBeSaved.setZipCode("");

        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.postForEntity("/companies",
                companyToBeSaved, CompanyDetailsDTO.class);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);


        Assertions.assertThat(response.getBody().getId()).isNull();
        Assertions.assertThat(response.getBody().getCnpj()).isNull();
        Assertions.assertThat(response.getBody().getCompanyName()).isNull();
        Assertions.assertThat(response.getBody().getTradeName()).isNull();
        Assertions.assertThat(response.getBody().isActive()).isFalse();
        Assertions.assertThat(response.getBody().getHeadquarters()).isNull();

        Optional<Company> companyInDatabase = companyRepository.findById(1L);

        Assertions.assertThat(companyInDatabase).isNotPresent();
    }

    @Test
    @DisplayName("Returns 400 bad request when company head quarters is invalid")
    void createCompany_Returns400BadRequest_WhenCompanyHeadQuartersIsInvalid() {
        AddressCreateDTO addressToBeSaved = AddressCreator.createInvalidAddressCreateDTOFullyFilled();

        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.postForEntity("/companies",
                companyToBeSaved, CompanyDetailsDTO.class);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);


        Assertions.assertThat(response.getBody().getId()).isNull();
        Assertions.assertThat(response.getBody().getCnpj()).isNull();
        Assertions.assertThat(response.getBody().getCompanyName()).isNull();
        Assertions.assertThat(response.getBody().getTradeName()).isNull();
        Assertions.assertThat(response.getBody().isActive()).isFalse();
        Assertions.assertThat(response.getBody().getHeadquarters()).isNull();

        Optional<Company> companyInDatabase = companyRepository.findById(1L);

        Assertions.assertThat(companyInDatabase).isNotPresent();
    }

    @Test
    @DisplayName("Updates and returns company details when company exists")
    void updateCompany_UpdatesAndReturnsCompanyDetails_WhenCompanyExists() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companyToBeSaved = CompanyCreator.createValidActiveCompanyToBeSaved();
        companyToBeSaved.setHeadquarters(addressSaved);
        companyRepository.save(companyToBeSaved);

        CompanyUpdateDTO companyForUpdate = CompanyCreator.createValidCompanyUpdateDTO();

        Long expectedId = companyToBeSaved.getId();
        String expectedCnpj = companyToBeSaved.getCnpj();

        String expectedCompanyName = companyForUpdate.getCompanyName();
        String expectedTradeName = companyForUpdate.getTradeName();

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/1",
                HttpMethod.PUT,
                new HttpEntity<>(companyForUpdate),
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getId()).isEqualTo(expectedId);
        Assertions.assertThat(response.getBody().getCnpj()).isEqualTo(expectedCnpj);
        Assertions.assertThat(response.getBody().getCompanyName()).isEqualTo(expectedCompanyName);
        Assertions.assertThat(response.getBody().getTradeName()).isEqualTo(expectedTradeName);
    }

    @Test
    @DisplayName("Returns 404 not found when company exists and is inactive")
    void updateCompany_Returns404NotFound_WhenCompanyExistsAndIsInactive() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companyToBeSaved = CompanyCreator.createValidInactiveCompanyToBeSaved();
        companyToBeSaved.setHeadquarters(addressSaved);
        companyRepository.save(companyToBeSaved);

        CompanyUpdateDTO companyForUpdate = CompanyCreator.createValidCompanyUpdateDTO();

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/1",
                HttpMethod.PUT,
                new HttpEntity<>(companyForUpdate),
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Returns 404 not found when company does not exists")
    void updateCompany_Returns404NotFound_WhenCompanyDoesNotExists() {
        CompanyUpdateDTO companyForUpdate = CompanyCreator.createValidCompanyUpdateDTO();

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/1",
                HttpMethod.PUT,
                new HttpEntity<>(companyForUpdate),
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Returns 400 bad request when company data is invalid")
    void updateCompany_UReturns400BadRequest_WhenCompanyDataIsInvalid() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companyToBeSaved = CompanyCreator.createValidActiveCompanyToBeSaved();
        companyToBeSaved.setHeadquarters(addressSaved);
        companyRepository.save(companyToBeSaved);

        CompanyUpdateDTO companyForUpdate = CompanyCreator.createValidCompanyUpdateDTO();
        companyForUpdate.getHeadquarters().setZipCode("");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/1",
                HttpMethod.PUT,
                new HttpEntity<>(companyForUpdate),
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    @DisplayName("Deactivates company and returns http status code 204 no content when company exists and is active")
    void deactivateCompany_DeactivatesCompanyAndReturnsNoContent_WhenCompanyExists() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companyToBeSaved = CompanyCreator.createValidActiveCompanyToBeSaved();
        companyToBeSaved.setHeadquarters(addressSaved);
        companyRepository.save(companyToBeSaved);

        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/deactivate/1",
                HttpMethod.PUT,
                null,
                Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Returns 404 not found when company does not exists")
    void deactivateCompany_Returns404NotFound_WhenCompanyExists() {
        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/deactivate/1",
                HttpMethod.PUT,
                null,
                Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Returns 404 not found when company exists and is already inactive")
    void deactivateCompany_Returns404NotFound_WhenCompanyExistsAndIsActive() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companyToBeSaved = CompanyCreator.createValidInactiveCompanyToBeSaved();
        companyToBeSaved.setHeadquarters(addressSaved);
        companyRepository.save(companyToBeSaved);

        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/deactivate/1",
                HttpMethod.PUT,
                null,
                Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Activates company and returns http status code 204 no content when company exists and is inactive")
    void activateCompany_DeactivatesCompanyAndReturnsNoContent_WhenCompanyExists() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companyToBeSaved = CompanyCreator.createValidInactiveCompanyToBeSaved();
        companyToBeSaved.setHeadquarters(addressSaved);
        companyRepository.save(companyToBeSaved);

        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/activate/1",
                HttpMethod.PUT,
                null,
                Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Returns 404 not found when company does not exists")
    void activateCompany_Returns404NotFound_WhenCompanyExists() {
        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/activate/1",
                HttpMethod.PUT,
                null,
                Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Returns 404 not found when company exists and is already active")
    void activateCompany_Returns404NotFound_WhenCompanyExistsAndIsActive() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companyToBeSaved = CompanyCreator.createValidActiveCompanyToBeSaved();
        companyToBeSaved.setHeadquarters(addressSaved);
        companyRepository.save(companyToBeSaved);

        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/activate/1",
                HttpMethod.PUT,
                null,
                Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
