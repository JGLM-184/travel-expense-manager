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
import org.springframework.security.oauth2.jwt.JwtEncoder;
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
    @Autowired
    private JwtEncoder jwtEncoder;

    private <T> HttpEntity<T> createHttpEntityWithToken(T body, String role) {
        java.time.Instant now = java.time.Instant.now();
        org.springframework.security.oauth2.jwt.JwtClaimsSet claims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject("1")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("scope", "ROLE_" + role)
                .claim("companyId", 1L)
                .build();

        String token = jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(claims)).getTokenValue();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth(token);

        return new HttpEntity<>(body, headers);
    }


    // TESTS FOR ADMIN
    @Test
    @DisplayName("Returns a page of active companies when companies exist")
    void findAllActiveCompanies_ReturnsPageOfActiveCompanies_WhenCompaniesExist() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companySaved = CompanyCreator.createValidActiveCompanyToBeSaved();
        companySaved.setHeadquarters(addressSaved);
        companyRepository.save(companySaved);

        String expectedCompanyName = companySaved.getCompanyName();
        String expectedCnpj = companySaved.getCnpj();

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN");

        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/active", HttpMethod.GET, requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        List<CompanyDetailsDTO> companyDetailsDTOList = response.getBody().toList();

        boolean companyFound = false;

        for (CompanyDetailsDTO company : companyDetailsDTOList) {
            if (company.getCompanyName().equals(expectedCompanyName)
                    && company.getCnpj().equals(expectedCnpj)
                    && company.isActive()) {
                companyFound = true;
                break;
            }
        }

        Assertions.assertThat(companyFound).isTrue();
    }

    @Test
    @DisplayName("Returns a void page of active companies when companies does not exist")
    void findAllActiveCompanies_ReturnsVoidPageOfActiveCompanies_WhenCompaniesDoesNotExist() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN");

        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/active", HttpMethod.GET, requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        List<CompanyDetailsDTO> companyDetailsDTOList = response.getBody().toList();

        boolean companyFound = false;

        for (CompanyDetailsDTO company : companyDetailsDTOList) {
            if ("Test Enterprise LTDA".equals(company.getCompanyName())) {
                companyFound = true;
                break;
            }
        }

        Assertions.assertThat(companyFound).isFalse();
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

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN");

        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/inactive", HttpMethod.GET, requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        List<CompanyDetailsDTO> companyDetailsDTOList = response.getBody().toList();

        Assertions.assertThat(companyDetailsDTOList)
                .isNotNull()
                .isNotEmpty();

        boolean companyFound = false;
        for (CompanyDetailsDTO company : companyDetailsDTOList) {
            if (company.getCompanyName().equals(expectedCompanyName)
                    && company.getCnpj().equals(expectedCnpj)
                    && !company.isActive()) {
                companyFound = true;
                break;
            }
        }

        Assertions.assertThat(companyFound).isTrue();
    }

    @Test
    @DisplayName("Returns a void page of inactive companies when companies does not exist")
    void findAllActiveCompanies_ReturnsVoidPageOfInactiveCompanies_WhenCompaniesDoesNotExist() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN");

        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/inactive", HttpMethod.GET, requestEntity,
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

        HttpEntity<CompanyCreateDTO> requestEntity = createHttpEntityWithToken(companyToBeSaved, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies",
                HttpMethod.POST, requestEntity, CompanyDetailsDTO.class);

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

        HttpEntity<CompanyCreateDTO> requestEntity = createHttpEntityWithToken(companyToBeSaved, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies",
                HttpMethod.POST, requestEntity, CompanyDetailsDTO.class);

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
    @DisplayName("Returns 400 bad request when company cnpj is null")
    void createCompany_Returns400BadRequest_WhenCompanyCnpjIsNull() {
        AddressCreateDTO addressToBeSaved = AddressCreator.createValidAddressCreateDTO();
        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);
        companyToBeSaved.setCnpj(null);

        HttpEntity<CompanyCreateDTO> requestEntity = createHttpEntityWithToken(companyToBeSaved, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies",
                HttpMethod.POST, requestEntity, CompanyDetailsDTO.class);

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
    @DisplayName("Returns 400 bad request when company cnpj is invalid")
    void createCompany_Returns400BadRequest_WhenCompanyCnpjIsInvalid() {
        AddressCreateDTO addressToBeSaved = AddressCreator.createValidAddressCreateDTO();
        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);
        companyToBeSaved.setCnpj("00.000.000/0001-00");

        HttpEntity<CompanyCreateDTO> requestEntity = createHttpEntityWithToken(companyToBeSaved, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies",
                HttpMethod.POST, requestEntity, CompanyDetailsDTO.class);

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
    @DisplayName("Returns 400 bad request when company cnpj is already registered")
    void createCompany_Returns400BadRequest_WhenCompanyCnpjIsAlreadyRegistered() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company companySaved = CompanyCreator.createValidActiveCompanyToBeSaved();
        companySaved.setHeadquarters(addressSaved);
        companyRepository.save(companySaved);

        AddressCreateDTO addressToBeSaved = AddressCreator.createValidAddressCreateDTO();
        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);

        HttpEntity<CompanyCreateDTO> requestEntity = createHttpEntityWithToken(companyToBeSaved, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies",
                HttpMethod.POST, requestEntity, CompanyDetailsDTO.class);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Assertions.assertThat(response.getBody().getId()).isNull();
        Assertions.assertThat(response.getBody().getCnpj()).isNull();
        Assertions.assertThat(response.getBody().getCompanyName()).isNull();
        Assertions.assertThat(response.getBody().getTradeName()).isNull();
        Assertions.assertThat(response.getBody().isActive()).isFalse();
        Assertions.assertThat(response.getBody().getHeadquarters()).isNull();

        Optional<Company> companyInDatabase = companyRepository.findById(3L);

        Assertions.assertThat(companyInDatabase).isNotPresent();
    }

    @Test
    @DisplayName("Returns 400 bad request when company head quarters is empty")
    void createCompany_Returns400BadRequest_WhenCompanyHeadQuartersIsEmpty() {
        AddressCreateDTO addressToBeSaved = AddressCreator.createValidAddressCreateDTO();
        addressToBeSaved.setZipCode("");

        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();
        companyToBeSaved.setHeadquarters(addressToBeSaved);

        HttpEntity<CompanyCreateDTO> requestEntity = createHttpEntityWithToken(companyToBeSaved, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies",
                HttpMethod.POST, requestEntity, CompanyDetailsDTO.class);

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
    @DisplayName("Returns 400 bad request when company head quarters is invalid")
    void createCompany_Returns400BadRequest_WhenCompanyHeadQuartersIsInvalid() {
        AddressCreateDTO addressToBeSaved = AddressCreator.createInvalidAddressCreateDTOFullyFilled();
        CompanyCreateDTO companyToBeSaved = CompanyCreator.createValidCompanyCreateDTO();

        companyToBeSaved.setHeadquarters(addressToBeSaved);

        HttpEntity<CompanyCreateDTO> requestEntity = createHttpEntityWithToken(companyToBeSaved, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies",
                HttpMethod.POST, requestEntity, CompanyDetailsDTO.class);

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

        HttpEntity<CompanyUpdateDTO> requestEntity = createHttpEntityWithToken(companyForUpdate, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/" + expectedId,
                HttpMethod.PUT,
                requestEntity,
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

        HttpEntity<CompanyUpdateDTO> requestEntity = createHttpEntityWithToken(companyForUpdate, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/" + companyToBeSaved.getId(),
                HttpMethod.PUT,
                requestEntity,
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Returns 404 not found when company does not exists")
    void updateCompany_Returns404NotFound_WhenCompanyDoesNotExists() {
        CompanyUpdateDTO companyForUpdate = CompanyCreator.createValidCompanyUpdateDTO();

        HttpEntity<CompanyUpdateDTO> requestEntity = createHttpEntityWithToken(companyForUpdate, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/999",
                HttpMethod.PUT,
                requestEntity,
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

        HttpEntity<CompanyUpdateDTO> requestEntity = createHttpEntityWithToken(companyForUpdate, "ADMIN");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/" + companyToBeSaved.getId(),
                HttpMethod.PUT,
                requestEntity,
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

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN");

        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/deactivate/" + companyToBeSaved.getId(),
                HttpMethod.PUT,
                requestEntity,
                Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Returns 404 not found when company does not exists")
    void deactivateCompany_Returns404NotFound_WhenCompanyExists() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN");

        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/deactivate/999",
                HttpMethod.PUT,
                requestEntity,
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

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN");

        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/deactivate/" + companyToBeSaved.getId(),
                HttpMethod.PUT,
                requestEntity,
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

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN");

        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/activate/" + companyToBeSaved.getId(),
                HttpMethod.PUT,
                requestEntity,
                Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Returns 404 not found when company does not exists")
    void activateCompany_Returns404NotFound_WhenCompanyExists() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN");
        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/activate/999",
                HttpMethod.PUT,
                requestEntity,
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

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN");

        ResponseEntity<Void> response = testRestTemplate.exchange("/companies/activate/" + companyToBeSaved.getId(),
                HttpMethod.PUT,
                requestEntity,
                Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // TESTS FOR MANAGER
    @Test
    @DisplayName("Returns 403 forbidden when user is manager")
    void findAllActiveCompanies_Returns403Forbidden_WhenUserIsManager() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER");

        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/active", HttpMethod.GET, requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Returns 403 forbidden when user is manager")
    void findAllActiveCompanies_Returns403Forbidden_WhenCompaniesDoesNotExistAndUserIsManager() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER");

        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/active", HttpMethod.GET, requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Returns 403 forbidden when user is manager")
    void findCompanyById_Returns403Forbidden_WhenCompanyExistsAndIsActiveAndUserIsManager() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/1",
                HttpMethod.GET,
                requestEntity,
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Returns 403 forbidden when user is manager")
    void findCompanyById_Returns403Forbidden_WhenCompanyExistsAndIsInactiveAndUserIsManager() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/1",
                HttpMethod.GET,
                requestEntity,
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Returns 403 forbidden when user is manager")
    void findCompanyById_Returns403Forbidden_WhenCompanyDoesNotExistAndUserIsManager() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/999",
                HttpMethod.GET,
                requestEntity,
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Returns 403 forbidden when user is manager")
    void updateCompany_Returns403Forbidden_WhenCompanyExistsAndUserIsManager() {
        CompanyUpdateDTO companyForUpdate = CompanyCreator.createValidCompanyUpdateDTO();

        HttpEntity<CompanyUpdateDTO> requestEntity = createHttpEntityWithToken(companyForUpdate, "MANAGER");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/1",
                HttpMethod.PUT,
                requestEntity,
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // TESTS FOR EMPLOYEE
    @Test
    @DisplayName("Returns 403 forbidden when user is employee")
    void findAllActiveCompanies_Returns403Forbidden_WhenUserIsEmployee() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE");

        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/active", HttpMethod.GET, requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Returns 403 forbidden when user is employee")
    void findAllActiveCompanies_Returns403Forbidden_WhenCompaniesDoesNotExistAndUserIsEmployee() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE");

        ResponseEntity<RestResponsePage<CompanyDetailsDTO>> response = testRestTemplate
                .exchange("/companies/active", HttpMethod.GET, requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<CompanyDetailsDTO>>() {
                        });

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Returns 403 forbidden when user is employee")
    void findCompanyById_Returns403Forbidden_WhenCompanyExistsAndIsActiveAndUserIsEmployee() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/1",
                HttpMethod.GET,
                requestEntity,
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Returns 403 forbidden when user is employee")
    void findCompanyById_Returns403Forbidden_WhenCompanyExistsAndIsInactiveAndUserIsEmployee() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/1",
                HttpMethod.GET,
                requestEntity,
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Returns 403 forbidden when user is employee")
    void findCompanyById_Returns403Forbidden_WhenCompanyDoesNotExistAndUserIsEmployee() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/999",
                HttpMethod.GET,
                requestEntity,
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Returns 403 forbidden when user is employee")
    void updateCompany_Returns403Forbidden_WhenCompanyExistsAndUserIsEmployee() {
        CompanyUpdateDTO companyForUpdate = CompanyCreator.createValidCompanyUpdateDTO();

        HttpEntity<CompanyUpdateDTO> requestEntity = createHttpEntityWithToken(companyForUpdate, "EMPLOYEE");

        ResponseEntity<CompanyDetailsDTO> response = testRestTemplate.exchange("/companies/1",
                HttpMethod.PUT,
                requestEntity,
                CompanyDetailsDTO.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}