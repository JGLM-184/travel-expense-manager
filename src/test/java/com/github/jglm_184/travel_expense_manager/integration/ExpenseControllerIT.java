package com.github.jglm_184.travel_expense_manager.integration;

import com.github.jglm_184.travel_expense_manager.dto.ExpenseCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ExpenseDetailsDTO;
import com.github.jglm_184.travel_expense_manager.model.*;
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
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ExpenseControllerIT {

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

    @Test //REVISAR
    @DisplayName("addExpenseToTravel creates expense successfully when employee is owner and travel is OPEN")
    void addExpenseToTravel_CreatesExpenseSuccessfully_WhenUserIsOwnerAndTravelIsOpen() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setCompany(companySaved);
        employee.setEmail("employee@test.com");
        employee.setCpf("11111111111");
        employee.setEmployeeId("EMP001");
        User employeeSaved = userRepository.save(employee);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.OPEN);
        Travel travelSaved = travelRepository.save(travel);

        ExpenseCreateDTO dto = ExpenseCreator.createValidExpenseCreateDTO();
        dto.setDate(travelSaved.getStartDate().plusDays(1));

        HttpEntity<ExpenseCreateDTO> requestEntity =
                createHttpEntityWithToken(dto, "EMPLOYEE", employeeSaved.getId(), companySaved.getId());

        ResponseEntity<ExpenseDetailsDTO> responseEntity =
                testRestTemplate.exchange(
                        "/expenses/travels/" + travelSaved.getId(),
                        HttpMethod.POST,
                        requestEntity,
                        ExpenseDetailsDTO.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getDescription()).isEqualTo(dto.getDescription());
    }

    @Test
    @DisplayName("addExpenseToTravel returns 400 Bad Request when description is blank")
    void addExpenseToTravel_Returns400BadRequest_WhenDescriptionIsBlank() {

        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setCompany(companySaved);
        employee.setEmail("employee@test.com");
        employee.setCpf("11111111111");
        employee.setEmployeeId("EMP001");
        User employeeSaved = userRepository.save(employee);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.OPEN);
        Travel travelSaved = travelRepository.save(travel);

        ExpenseCreateDTO dto = ExpenseCreator.createValidExpenseCreateDTO();
        dto.setDescription("");

        HttpEntity<ExpenseCreateDTO> requestEntity =
                createHttpEntityWithToken(dto, "EMPLOYEE", employeeSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(
                        "/expenses/travels/" + travelSaved.getId(),
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("addExpenseToTravel returns 400 Bad Request when expense date is outside travel period")
    void addExpenseToTravel_Returns400BadRequest_WhenExpenseDateIsOutsideTravelPeriod() {

        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setCompany(companySaved);
        employee.setEmail("employee@test.com");
        employee.setCpf("11111111111");
        employee.setEmployeeId("EMP001");
        User employeeSaved = userRepository.save(employee);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.OPEN);
        Travel travelSaved = travelRepository.save(travel);

        ExpenseCreateDTO dto = ExpenseCreator.createExpenseCreateDTOWithInvalidDate();

        HttpEntity<ExpenseCreateDTO> requestEntity =
                createHttpEntityWithToken(dto, "EMPLOYEE", employeeSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(
                        "/expenses/travels/" + travelSaved.getId(),
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("addExpenseToTravel returns 400 Bad Request when travel status is not OPEN")
    void addExpenseToTravel_Returns400BadRequest_WhenTravelStatusIsNotOpen() {

        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setCompany(companySaved);
        employee.setEmail("employee@test.com");
        employee.setCpf("11111111111");
        employee.setEmployeeId("EMP001");
        User employeeSaved = userRepository.save(employee);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.SUBMITTED);
        Travel travelSaved = travelRepository.save(travel);

        ExpenseCreateDTO dto = ExpenseCreator.createValidExpenseCreateDTO();

        HttpEntity<ExpenseCreateDTO> requestEntity =
                createHttpEntityWithToken(dto, "EMPLOYEE", employeeSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(
                        "/expenses/travels/" + travelSaved.getId(),
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("addExpenseToTravel returns 404 Not Found when employee is not owner")
    void addExpenseToTravel_Returns404NotFound_WhenUserIsEmployeeButNotOwner() {

        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User employee1 = UserCreator.createValidActiveUserToBeSaved();
        employee1.setRole(Role.ROLE_EMPLOYEE);
        employee1.setCompany(companySaved);
        employee1.setEmail("emp1@test.com");
        employee1.setCpf("11111111111");
        employee1.setEmployeeId("EMP001");
        User employeeSaved1 = userRepository.save(employee1);

        User employee2 = UserCreator.createValidActiveUserToBeSaved();
        employee2.setRole(Role.ROLE_EMPLOYEE);
        employee2.setCompany(companySaved);
        employee2.setEmail("emp2@test.com");
        employee2.setCpf("22222222222");
        employee2.setEmployeeId("EMP002");
        User employeeSaved2 = userRepository.save(employee2);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved1);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.OPEN);
        Travel travelSaved = travelRepository.save(travel);

        ExpenseCreateDTO dto = ExpenseCreator.createValidExpenseCreateDTO();
        dto.setDate(travelSaved.getStartDate().plusDays(1)); // data válida

        HttpEntity<ExpenseCreateDTO> requestEntity =
                createHttpEntityWithToken(dto, "EMPLOYEE", employeeSaved2.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(
                        "/expenses/travels/" + travelSaved.getId(),
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("addExpenseToTravel returns 404 Not Found when manager is from different company")
    void addExpenseToTravel_Returns404NotFound_WhenManagerIsFromDifferentCompany() {

        Address address1 = AddressCreator.createValidAddressToBeSaved();
        address1.setZipCode("01001000");
        Address addressSaved1 = addressRepository.save(address1);

        Company company1 = CompanyCreator.createValidActiveCompanyToBeSaved();
        company1.setHeadquarters(addressSaved1);
        Company companySaved1 = companyRepository.save(company1);

        Address address2 = AddressCreator.createValidAddressToBeSaved();
        address2.setZipCode("02002000");
        Address addressSaved2 = addressRepository.save(address2);

        Company company2 = CompanyCreator.createValidActiveCompanyToBeSaved();
        company2.setHeadquarters(addressSaved2);
        company2.setCnpj("12345678000199");
        Company companySaved2 = companyRepository.save(company2);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setCompany(companySaved1);
        employee.setEmail("employee@test.com");
        employee.setCpf("11111111111");
        employee.setEmployeeId("EMP001");
        User employeeSaved = userRepository.save(employee);

        User manager = UserCreator.createValidActiveUserToBeSaved();
        manager.setRole(Role.ROLE_MANAGER);
        manager.setCompany(companySaved2);
        manager.setEmail("manager@test.com");
        manager.setCpf("22222222222");
        manager.setEmployeeId("MNG001");
        User managerSaved = userRepository.save(manager);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved);
        travel.setCompany(companySaved1);
        travel.setStatus(TravelStatus.OPEN);
        Travel travelSaved = travelRepository.save(travel);

        ExpenseCreateDTO dto = ExpenseCreator.createValidExpenseCreateDTO();

        HttpEntity<ExpenseCreateDTO> requestEntity =
                createHttpEntityWithToken(dto, "MANAGER", managerSaved.getId(), companySaved2.getId());

        ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(
                        "/expenses/travels/" + travelSaved.getId(),
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("addExpenseToTravel returns 404 Not Found when travel does not exist")
    void addExpenseToTravel_Returns404NotFound_WhenTravelDoesNotExist() {

        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        ExpenseCreateDTO dto = ExpenseCreator.createValidExpenseCreateDTO();

        HttpEntity<ExpenseCreateDTO> requestEntity =
                createHttpEntityWithToken(dto, "EMPLOYEE", 1L, companySaved.getId());

        ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(
                        "/expenses/travels/999",
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deleteExpense deletes expense successfully when user is owner")
    void deleteExpense_DeletesSuccessfully_WhenUserIsOwner() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setCompany(companySaved);
        employee.setEmail("employee@test.com");
        employee.setCpf("11111111111");
        employee.setEmployeeId("EMP001");
        User employeeSaved = userRepository.save(employee);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.OPEN);

        Expense expense = ExpenseCreator.createValidExpenseToBeSaved();
        expense.setTravel(travel);

        travel.setExpenses(new ArrayList<>(List.of(expense)));

        Travel travelSaved = travelRepository.save(travel);
        Long expenseId = travelSaved.getExpenses().getFirst().getId();

        HttpEntity<Void> requestEntity =
                createHttpEntityWithToken(null, "EMPLOYEE", employeeSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                "/expenses/" + expenseId,
                HttpMethod.DELETE,
                requestEntity,
                Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("deleteExpense returns 400 Bad Request when travel report is not OPEN")
    void deleteExpense_Returns400BadRequest_WhenTravelStatusIsNotOpen() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setCompany(companySaved);
        employee.setEmail("employee@test.com");
        employee.setCpf("11111111111");
        employee.setEmployeeId("EMP001");
        User employeeSaved = userRepository.save(employee);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.SUBMITTED);

        Expense expense = ExpenseCreator.createValidExpenseToBeSaved();
        expense.setTravel(travel);

        travel.setExpenses(new ArrayList<>(List.of(expense)));

        Travel travelSaved = travelRepository.save(travel);
        Long expenseId = travelSaved.getExpenses().getFirst().getId();

        HttpEntity<Void> requestEntity =
                createHttpEntityWithToken(null, "EMPLOYEE", employeeSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                "/expenses/" + expenseId,
                HttpMethod.DELETE,
                requestEntity,
                Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("deleteExpense returns 404 Not Found when employee is not owner")
    void deleteExpense_Returns404NotFound_WhenEmployeeIsNotOwner() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User employee1 = UserCreator.createValidActiveUserToBeSaved();
        employee1.setCompany(companySaved);
        employee1.setEmail("emp1@test.com");
        employee1.setCpf("11111111111");
        employee1.setEmployeeId("EMP001");
        employee1.setRole(Role.ROLE_EMPLOYEE);
        User employeeSaved1 = userRepository.save(employee1);

        User employee2 = UserCreator.createValidActiveUserToBeSaved();
        employee2.setCompany(companySaved);
        employee2.setEmail("emp2@test.com");
        employee2.setCpf("22222222222");
        employee2.setEmployeeId("EMP002");
        employee2.setRole(Role.ROLE_EMPLOYEE);
        User employeeSaved2 = userRepository.save(employee2);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved1);
        travel.setCompany(companySaved);
        travel.setStatus(TravelStatus.OPEN);

        Expense expense = ExpenseCreator.createValidExpenseToBeSaved();
        expense.setTravel(travel);

        travel.setExpenses(new ArrayList<>(List.of(expense)));

        Travel travelSaved = travelRepository.save(travel);
        Long expenseId = travelSaved.getExpenses().getFirst().getId();

        System.out.println("Employee 1: " + employeeSaved1.getId());
        System.out.println("Employee 2: " + employeeSaved2.getId());
        System.out.println("Travel owner: " + travelSaved.getUser().getId());
        System.out.println("Authenticated: " + employeeSaved2.getId());


        HttpEntity<Void> requestEntity =
                createHttpEntityWithToken(null, "EMPLOYEE", employeeSaved2.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                "/expenses/" + expenseId,
                HttpMethod.DELETE,
                requestEntity,
                Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deleteExpense returns 404 Not Found when manager is from different company")
    void deleteExpense_Returns404NotFound_WhenManagerIsFromDifferentCompany() {
        Address address1 = AddressCreator.createValidAddressToBeSaved();
        address1.setZipCode("01001001");
        Address addressSaved1 = addressRepository.save(address1);

        Company company1 = CompanyCreator.createValidActiveCompanyToBeSaved();
        company1.setHeadquarters(addressSaved1);
        Company companySaved1 = companyRepository.save(company1);

        Address address2 = AddressCreator.createValidAddressToBeSaved();
        address2.setZipCode("02002002");
        Address addressSaved2 = addressRepository.save(address2);

        Company company2 = CompanyCreator.createValidActiveCompanyToBeSaved();
        company2.setCnpj("12345678000199");
        company2.setHeadquarters(addressSaved2);
        Company companySaved2 = companyRepository.save(company2);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setCompany(companySaved1);
        employee.setEmail("emp@test.com");
        employee.setCpf("11111111111");
        employee.setEmployeeId("EMP001");
        User employeeSaved = userRepository.save(employee);

        User manager = UserCreator.createValidActiveUserToBeSaved();
        manager.setRole(Role.ROLE_MANAGER);
        manager.setCompany(companySaved2);
        manager.setEmail("manager@test.com");
        manager.setCpf("22222222222");
        manager.setEmployeeId("MAN001");
        User managerSaved = userRepository.save(manager);

        Travel travel = TravelCreator.createValidTravelToBeSaved();
        travel.setUser(employeeSaved);
        travel.setCompany(companySaved1);
        travel.setStatus(TravelStatus.OPEN);

        Expense expense = ExpenseCreator.createValidExpenseToBeSaved();
        expense.setTravel(travel);

        travel.setExpenses(new ArrayList<>(List.of(expense)));

        Travel travelSaved = travelRepository.save(travel);
        Long expenseId = travelSaved.getExpenses().getFirst().getId();

        HttpEntity<Void> requestEntity =
                createHttpEntityWithToken(null, "MANAGER", managerSaved.getId(), companySaved2.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                "/expenses/" + expenseId,
                HttpMethod.DELETE,
                requestEntity,
                Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deleteExpense returns 404 Not Found when expense does not exist")
    void deleteExpense_Returns404NotFound_WhenExpenseDoesNotExist() {
        Address address = addressRepository.save(AddressCreator.createValidAddressToBeSaved());

        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(address);
        Company companySaved = companyRepository.save(company);

        User employee = UserCreator.createValidActiveUserToBeSaved();
        employee.setCompany(companySaved);
        employee.setEmail("employee@test.com");
        employee.setCpf("11111111111");
        employee.setEmployeeId("EMP001");
        User employeeSaved = userRepository.save(employee);

        HttpEntity<Void> requestEntity =
                createHttpEntityWithToken(null, "EMPLOYEE", employeeSaved.getId(), companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                "/expenses/999",
                HttpMethod.DELETE,
                requestEntity,
                Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
