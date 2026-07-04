package com.github.jglm_184.travel_expense_manager.integration;

import com.github.jglm_184.travel_expense_manager.dto.TravelCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelUpdateDTO;
import com.github.jglm_184.travel_expense_manager.model.*;
import com.github.jglm_184.travel_expense_manager.model.enums.ExpenseCategory;
import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import com.github.jglm_184.travel_expense_manager.model.enums.TravelStatus;
import com.github.jglm_184.travel_expense_manager.repository.AddressRepository;
import com.github.jglm_184.travel_expense_manager.repository.CompanyRepository;
import com.github.jglm_184.travel_expense_manager.repository.TravelRepository;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import com.github.jglm_184.travel_expense_manager.util.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TravelControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private JwtEncoder jwtEncoder;

    private <T> HttpEntity<T> createHttpEntityWithToken(T body, String role, Long userId, Long companyId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("scope", "ROLE_" + role)
                .claim("companyId", companyId)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return new HttpEntity<>(body, headers);
    }

    @Test
    @DisplayName("findAllTravels returns all travels when user is Admin")
    void findAllTravels_ReturnsPageOfTravels_WhenUserIsAdmin() {
        Address headquartersAddress = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(headquartersAddress);
        Company companySaved = companyRepository.save(company);

        User user = UserCreator.createValidActiveUserToBeSaved();
        user.setCompany(companySaved);
        User userSaved = userRepository.save(user);

        Address destinationAddress = addressRepository.save(AddressCreator.createAnotherValidAddressToBeSaved());

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(userSaved);
        travel.setCompany(companySaved);
        travel.setDestination(destinationAddress);

        travelRepository.save(travel);

        System.out.println("User salvo = " + userSaved.getId());
        System.out.println("Existe? " + userRepository.findById(userSaved.getId()).isPresent());

        HttpEntity<Void> requestEntity =
                createHttpEntityWithToken(null, "ADMIN", userSaved.getId(), companySaved.getId());

        ResponseEntity<RestResponsePage<TravelDetailsDTO>> responseEntity =
                testRestTemplate.exchange(
                        "/travels",
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<TravelDetailsDTO>>() {
                        });


        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().toList()).hasSize(1);
        Assertions.assertThat(responseEntity.getBody().getContent().getFirst().getPurpose())
                .isEqualTo(travel.getPurpose());
    }

    @Test
    @DisplayName("findAllTravels returns company travels when user is Manager")
    void findAllTravels_ReturnsPageOfCompanyTravels_WhenUserIsManager() {

        Address headquartersAddress = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(headquartersAddress);
        Company companySaved = companyRepository.save(company);

        User manager = UserCreator.createValidActiveUserToBeSaved();
        manager.setRole(Role.ROLE_MANAGER);
        manager.setCompany(companySaved);
        manager.setEmail("manager@company1.com");
        manager.setCpf("11111111111");
        User managerSaved = userRepository.save(manager);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setCompany(companySaved);
        employee.setEmail("employee@company1.com");
        employee.setCpf("22222222222");
        employee.setEmployeeId("E001");
        User employeeSaved = userRepository.save(employee);

        Address destinationAddress =
                addressRepository.save(AddressCreator.createAnotherValidAddressToBeSaved());

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved);
        travel.setCompany(companySaved);
        travel.setDestination(destinationAddress);

        travelRepository.save(travel);

        HttpEntity<Void> requestEntity =
                createHttpEntityWithToken(
                        null,
                        "MANAGER",
                        managerSaved.getId(),
                        companySaved.getId());

        ResponseEntity<RestResponsePage<TravelDetailsDTO>> responseEntity =
                testRestTemplate.exchange(
                        "/travels",
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<TravelDetailsDTO>>() {
                        });

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().toList()).hasSize(1);
        Assertions.assertThat(responseEntity.getBody().getContent().getFirst().getPurpose())
                .isEqualTo(travel.getPurpose());
    }

    @Test
    @DisplayName("findAllTravels returns only own travels when user is Employee")
    void findAllTravels_ReturnsPageOfOwnTravels_WhenUserIsEmployee() {

        Address headquartersAddress = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(headquartersAddress);
        Company companySaved = companyRepository.save(company);

        User employee1 = UserCreator.createValidActiveUserToBeSaved();
        employee1.setRole(Role.ROLE_EMPLOYEE);
        employee1.setCompany(companySaved);
        employee1.setEmail("emp1@test.com");
        employee1.setCpf("33333333333");
        employee1.setEmployeeId("EMP001");
        User employeeSaved1 = userRepository.save(employee1);

        User employee2 = UserCreator.createValidActiveUserToBeSaved();
        employee2.setRole(Role.ROLE_EMPLOYEE);
        employee2.setCompany(companySaved);
        employee2.setEmail("emp2@test.com");
        employee2.setCpf("44444444444");
        employee2.setEmployeeId("EMP002");
        User employeeSaved2 = userRepository.save(employee2);

        Address destination1 =
                addressRepository.save(AddressCreator.createAnotherValidAddressToBeSaved());

        Address destination2 =
                addressRepository.save(AddressCreator.createOneMoreValidAddressToBeSaved());

        Travel travel1 = TravelCreator.createValidTravelToBeSaved();
        travel1.setUser(employeeSaved1);
        travel1.setCompany(companySaved);
        travel1.setDestination(destination1);
        travelRepository.save(travel1);

        Travel travel2 = TravelCreator.createAnotherValidTravelToBeSaved();
        travel2.setUser(employeeSaved2);
        travel2.setCompany(companySaved);
        travel2.setDestination(destination2);
        travelRepository.save(travel2);

        HttpEntity<Void> requestEntity =
                createHttpEntityWithToken(
                        null,
                        "EMPLOYEE",
                        employeeSaved1.getId(),
                        companySaved.getId());

        ResponseEntity<RestResponsePage<TravelDetailsDTO>> responseEntity =
                testRestTemplate.exchange(
                        "/travels",
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<TravelDetailsDTO>>() {
                        });

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().toList()).hasSize(1);
        Assertions.assertThat(responseEntity.getBody().getContent().getFirst().getPurpose())
                .isEqualTo(travel1.getPurpose());
    }

    @Test
    @DisplayName("findAllTravels returns empty page when employee has no travels")
    void findAllTravels_ReturnsEmptyPage_WhenEmployeeHasNoTravels() {
        Address headquarters = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(headquarters);
        Company companySaved = companyRepository.save(company);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setRole(Role.ROLE_EMPLOYEE);
        employee.setCompany(companySaved);
        employee.setEmail("employee@test.com");
        employee.setCpf("11111111111");
        employee.setEmployeeId("EMP-111");

        User employeeSaved = userRepository.save(employee);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(
                null,
                "EMPLOYEE",
                employeeSaved.getId(),
                companySaved.getId()
        );

        ResponseEntity<RestResponsePage<TravelDetailsDTO>> responseEntity =
                testRestTemplate.exchange(
                        "/travels",
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<TravelDetailsDTO>>() {
                        }
                );

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getContent()).isEmpty();
    }

    @Test
    @DisplayName("createTravel saves travel when data is valid")
    void createTravel_SavesAndReturnsTravelDetails_WhenDataIsValidAndUserIsEmployee() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        User empSaved = userRepository.save(emp);

        TravelCreateDTO dto = TravelCreator.createValidTravelCreateDTO();

        HttpEntity<TravelCreateDTO> requestEntity = createHttpEntityWithToken(dto, "EMPLOYEE", empSaved.getId(), companySaved.getId());

        ResponseEntity<TravelDetailsDTO> responseEntity = testRestTemplate
                .exchange("/travels",
                        HttpMethod.POST,
                        requestEntity,
                        TravelDetailsDTO.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody().getId()).isNotNull();
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getPurpose())
                .isEqualTo(dto.getPurpose());
        Assertions.assertThat(responseEntity.getBody().getStatus())
                .isEqualTo(TravelStatus.OPEN);
    }

    @Test
    @DisplayName("createTravel returns 400 Bad Request when purpose is blank")
    void createTravel_Returns400BadRequest_WhenPurposeIsBlank() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        User empSaved = userRepository.save(emp);

        TravelCreateDTO dto = TravelCreator.createValidTravelCreateDTO();
        dto.setPurpose("");

        HttpEntity<TravelCreateDTO> requestEntity = createHttpEntityWithToken(dto, "EMPLOYEE", empSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels",
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    @DisplayName("createTravel returns 400 Bad Request when dates are inverted")
    void createTravel_Returns400BadRequest_WhenEndDateIsBeforeStartDate() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        User empSaved = userRepository.save(emp);

        TravelCreateDTO dto = TravelCreator.createValidTravelCreateDTO();
        dto.setStartDate(LocalDate.of(2026, 12, 20));
        dto.setEndDate(LocalDate.of(2026, 12, 10));

        HttpEntity<TravelCreateDTO> requestEntity = createHttpEntityWithToken(dto, "EMPLOYEE", empSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels",
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("updateTravel updates travel when user is owner and status is OPEN")
    void updateTravel_UpdatesAndReturnsTravelDetails_WhenUserIsOwnerAndStatusIsOpen() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        User empSaved = userRepository.save(emp);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(empSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.OPEN);
        Travel travelSaved = travelRepository.save(travel);

        TravelUpdateDTO dto = TravelCreator.createValidTravelUpdateDTO();

        HttpEntity<TravelUpdateDTO> requestEntity =
                createHttpEntityWithToken(dto, "EMPLOYEE", empSaved.getId(), companySaved.getId());

        ResponseEntity<TravelDetailsDTO> responseEntity = testRestTemplate.exchange(
                "/travels/" + travelSaved.getId(),
                HttpMethod.PUT,
                requestEntity,
                TravelDetailsDTO.class
        );

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getId()).isEqualTo(travelSaved.getId());
        Assertions.assertThat(responseEntity.getBody().getStatus()).isEqualTo(TravelStatus.OPEN);
    }

    @Test
    @DisplayName("updateTravel returns 400 Bad Request when status is not OPEN")
    void updateTravel_Returns400BadRequest_WhenStatusIsNotOpen() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        User empSaved = userRepository.save(emp);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(empSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.SUBMITTED);
        Travel travelSaved = travelRepository.save(travel);

        TravelUpdateDTO dto = TravelCreator.createValidTravelUpdateDTO();

        HttpEntity<TravelUpdateDTO> requestEntity =
                createHttpEntityWithToken(dto, "EMPLOYEE", empSaved.getId(), companySaved.getId());

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                "/travels/" + travelSaved.getId(),
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("updateTravel returns 404 Not Found when employee is not owner")
    void updateTravel_Returns404NotFound_WhenUserIsEmployeeButNotOwner() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User employee1 = UserCreator.createValidActiveUserToBeSaved();
        employee1.setRole(Role.ROLE_EMPLOYEE);
        employee1.setCompany(companySaved);
        employee1.setEmail("emp1@test.com");
        employee1.setCpf("33333333333");
        employee1.setEmployeeId("EMP001");
        User employeeSaved1 = userRepository.save(employee1);

        User employee2 = UserCreator.createValidActiveUserToBeSaved();
        employee2.setRole(Role.ROLE_EMPLOYEE);
        employee2.setCompany(companySaved);
        employee2.setEmail("emp2@test.com");
        employee2.setCpf("44444444444");
        employee2.setEmployeeId("EMP002");
        User employeeSaved2 = userRepository.save(employee2);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved1);
        travel.setCompany(companySaved);
        Travel travelSaved = travelRepository.save(travel);

        TravelUpdateDTO dto = TravelCreator.createValidTravelUpdateDTO();

        HttpEntity<TravelUpdateDTO> requestEntity =
                createHttpEntityWithToken(dto, "EMPLOYEE", employeeSaved2.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                "/travels/" + travelSaved.getId(),
                HttpMethod.PUT,
                requestEntity,
                Void.class
        );

        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("updateTravel returns 404 Not Found when travel does not exist")
    void updateTravel_Returns404NotFound_WhenTravelDoesNotExist() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        TravelUpdateDTO dto = TravelCreator.createValidTravelUpdateDTO();

        HttpEntity<TravelUpdateDTO> requestEntity = createHttpEntityWithToken(dto, "EMPLOYEE", 1L, companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/999",
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("submitTravel submits successfully when report has expenses")
    void submitTravel_SubmitsSuccessfully_WhenUserIsOwnerAndHasExpenses() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        User empSaved = userRepository.save(emp);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(empSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.OPEN);

        Expense expense = Expense.builder()
                .description("Dinner")
                .amount(BigDecimal.TEN)
                .category(ExpenseCategory.MEALS)
                .date(LocalDate.now())
                .travel(travel)
                .build();

        travel.setExpenses(List.of(expense));

        Travel travelSaved = travelRepository.save(travel);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE", empSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/" + travelSaved.getId() + "/submit",
                        HttpMethod.PATCH,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("submitTravel returns 400 Bad Request when report has no expenses")
    void submitTravel_Returns400BadRequest_WhenReportHasNoExpenses() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        User empSaved = userRepository.save(emp);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(empSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.OPEN);
        Travel travelSaved = travelRepository.save(travel);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE", empSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                "/travels/" + travelSaved.getId() + "/submit",
                HttpMethod.PATCH,
                requestEntity,
                Void.class
        );

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("submitTravel returns 404 Not Found when employee is not owner")
    void submitTravel_Returns404NotFound_WhenUserIsEmployeeButNotOwner() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp1 = UserCreator.createValidActiveUserToBeSaved();
        emp1.setRole(Role.ROLE_EMPLOYEE);
        emp1.setCompany(companySaved);
        emp1.setEmail("emp1@test.com");
        emp1.setCpf("33333333333");
        emp1.setEmployeeId("EMP001");
        User empSaved1 = userRepository.save(emp1);

        User emp2 = UserCreator.createValidActiveUserToBeSaved();
        emp2.setRole(Role.ROLE_EMPLOYEE);
        emp2.setCompany(companySaved);
        emp2.setEmail("emp2@test.com");
        emp2.setCpf("44444444444");
        emp2.setEmployeeId("EMP002");
        User empSaved2 = userRepository.save(emp2);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(empSaved1);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.OPEN);
        Travel travelSaved = travelRepository.save(travel);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE", empSaved2.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                "/travels/" + travelSaved.getId() + "/submit",
                HttpMethod.PATCH,
                requestEntity,
                Void.class
        );

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("submitTravel returns 404 Not Found when travel does not exist")
    void submitTravel_Returns404NotFound_WhenTravelDoesNotExist() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        HttpEntity<Void> requestEntity =
                createHttpEntityWithToken(null, "EMPLOYEE", 1L, companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                "/travels/999/submit",
                HttpMethod.PATCH,
                requestEntity,
                Void.class
        );

        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("approveTravel approves travel when manager belongs to same company")
    void approveTravel_ApprovesSuccessfully_WhenUserIsManagerOfSameCompany() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        emp.setEmail("emp@test.com");
        emp.setCpf("11111111111");
        emp.setEmployeeId("EMP001");
        User empSaved = userRepository.save(emp);

        User manager = UserCreator.createValidActiveUserToBeSaved();
        manager.setRole(Role.ROLE_MANAGER);
        manager.setCompany(companySaved);
        manager.setEmail("manager@test.com");
        manager.setCpf("22222222222");
        manager.setEmployeeId("MAN001");
        User managerSaved = userRepository.save(manager);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(empSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.SUBMITTED);
        Travel travelSaved = travelRepository.save(travel);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER", managerSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/" + travelSaved.getId() + "/approve",
                        HttpMethod.PATCH,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Travel updatedTravel = travelRepository.findById(travelSaved.getId())
                .orElseThrow();

        Assertions.assertThat(updatedTravel.getStatus())
                .isEqualTo(TravelStatus.APPROVED);
    }

    @Test
    @DisplayName("approveTravel returns 400 Bad Request when manager is owner of report")
    void approveTravel_Returns400BadRequest_WhenManagerIsOwnerOfReport() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User manager = UserCreator.createValidActiveUserToBeSaved();
        manager.setRole(Role.ROLE_MANAGER);
        manager.setCompany(companySaved);
        User managerSaved = userRepository.save(manager);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(managerSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.SUBMITTED);
        Travel travelSaved = travelRepository.save(travel);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER", managerSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/" + travelSaved.getId() + "/approve",
                        HttpMethod.PATCH,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Travel updatedTravel = travelRepository.findById(travelSaved.getId())
                .orElseThrow();

        Assertions.assertThat(updatedTravel.getStatus())
                .isEqualTo(TravelStatus.SUBMITTED);
    }

    @Test
    @DisplayName("approveTravel returns 403 Forbidden when user is an employee")
    void approveTravel_Returns403Forbidden_WhenUserIsEmployee() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        User empSaved = userRepository.save(emp);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(empSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.SUBMITTED);
        Travel travelSaved = travelRepository.save(travel);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE", empSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/" + travelSaved.getId() + "/approve",
                        HttpMethod.PATCH,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("approveTravel returns 404 Not Found when manager is from a different company")
    void approveTravel_Returns404NotFound_WhenManagerIsFromDifferentCompany() {
        Address address1 = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company1 = CompanyCreator.createValidActiveCompanyToBeSaved();
        company1.setHeadquarters(address1);
        Company companySaved1 = companyRepository.save(company1);

        Address address2 = addressRepository.save(AddressCreator.createAnotherValidAddressToBeSaved());
        Company company2 = CompanyCreator.createValidActiveCompanyToBeSaved();
        company2.setCnpj("19131256000130");
        company2.setHeadquarters(address2);
        Company companySaved2 = companyRepository.save(company2);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved1);
        emp.setEmail("emp@company1.com");
        emp.setCpf("33333333333");
        emp.setEmployeeId("EMP001");
        User empSaved = userRepository.save(emp);

        User manager = UserCreator.createValidActiveUserToBeSaved();
        manager.setRole(Role.ROLE_MANAGER);
        manager.setCompany(companySaved2);
        manager.setEmail("manager@company2.com");
        manager.setCpf("44444444444");
        manager.setEmployeeId("EMP002");
        User managerSaved = userRepository.save(manager);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(empSaved);
        travel.setCompany(companySaved1);
        travel.setStatus(TravelStatus.SUBMITTED);
        Travel travelSaved = travelRepository.save(travel);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER", managerSaved.getId(), companySaved2.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/" + travelSaved.getId() + "/approve",
                        HttpMethod.PATCH,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("approveTravel returns 404 Not Found when travel does not exist")
    void approveTravel_Returns404NotFound_WhenTravelDoesNotExist() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User manager = UserCreator.createValidActiveUserToBeSaved();
        manager.setRole(Role.ROLE_MANAGER);
        manager.setCompany(companySaved);
        User managerSaved = userRepository.save(manager);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER", managerSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/999/approve",
                        HttpMethod.PATCH,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("rejectTravel rejects travel when manager belongs to same company")
    void rejectTravel_RejectsSuccessfully_WhenUserIsManagerOfSameCompany() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        emp.setEmail("emp@test.com");
        emp.setCpf("33333333333");
        emp.setEmployeeId("EMP001");
        User empSaved = userRepository.save(emp);

        User manager = UserCreator.createValidActiveUserToBeSaved();
        manager.setRole(Role.ROLE_MANAGER);
        manager.setCompany(companySaved);
        emp.setCpf("44444444444");
        emp.setEmployeeId("MA001");
        manager.setEmail("manager@test.com");

        User managerSaved = userRepository.save(manager);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(empSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.SUBMITTED);
        Travel travelSaved = travelRepository.save(travel);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER", managerSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/" + travelSaved.getId() + "/reject",
                        HttpMethod.PATCH,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Travel updatedTravel = travelRepository.findById(travelSaved.getId()).orElseThrow();

        Assertions.assertThat(updatedTravel.getStatus())
                .isEqualTo(TravelStatus.REJECTED);
    }

    @Test
    @DisplayName("rejectTravel returns 400 Bad Request when manager is owner of report")
    void rejectTravel_Returns400BadRequest_WhenManagerIsOwnerOfReport() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User manager = UserCreator.createValidActiveUserToBeSaved();
        manager.setRole(Role.ROLE_MANAGER);
        manager.setCompany(companySaved);
        manager.setEmail("manager@test.com");
        manager.setCpf("11111111111");
        manager.setEmployeeId("MAN001");
        User managerSaved = userRepository.save(manager);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(managerSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.SUBMITTED);
        Travel travelSaved = travelRepository.save(travel);

        HttpEntity<Void> requestEntity =
                createHttpEntityWithToken(null, "MANAGER", managerSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/" + travelSaved.getId() + "/reject",
                        HttpMethod.PATCH,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("rejectTravel returns 403 Forbidden when user is an employee")
    void rejectTravel_Returns403Forbidden_WhenUserIsEmployee() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User emp = UserCreator.createValidActiveUserToBeSaved();
        emp.setCompany(companySaved);
        User empSaved = userRepository.save(emp);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(empSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.SUBMITTED);
        Travel travelSaved = travelRepository.save(travel);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE", empSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/" + travelSaved.getId() + "/reject",
                        HttpMethod.PATCH,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("rejectTravel returns 404 Not Found when travel does not exist")
    void rejectTravel_Returns404NotFound_WhenTravelDoesNotExist() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User manager = UserCreator.createValidActiveUserToBeSaved();
        manager.setRole(Role.ROLE_MANAGER);
        manager.setCompany(companySaved);
        User managerSaved = userRepository.save(manager);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER", managerSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/travels/999/reject",
                        HttpMethod.PATCH,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}