package com.github.jglm_184.travel_expense_manager.integration;

import com.github.jglm_184.travel_expense_manager.dto.UserCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserUpdateDTO;
import com.github.jglm_184.travel_expense_manager.model.Address;
import com.github.jglm_184.travel_expense_manager.model.Company;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import com.github.jglm_184.travel_expense_manager.repository.AddressRepository;
import com.github.jglm_184.travel_expense_manager.repository.CompanyRepository;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import com.github.jglm_184.travel_expense_manager.util.AddressCreator;
import com.github.jglm_184.travel_expense_manager.util.CompanyCreator;
import com.github.jglm_184.travel_expense_manager.util.RestResponsePage;
import com.github.jglm_184.travel_expense_manager.util.UserCreator;
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

import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private JwtEncoder jwtEncoder;

    private <T> HttpEntity<T> createHttpEntityWithToken(T body, String role, Long companyId) {
        java.time.Instant now = java.time.Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject("1")
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

    // TESTS FOR ADMIN
    @Test
    @DisplayName("Returns a page of active users when users exist")
    void findAllActiveUsers_ReturnsPageOfActiveUsers_WhenUsersExist() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        User userSaved = UserCreator.createValidActiveUserToBeSaved();
        userSaved.setCompany(companySaved);
        userRepository.save(userSaved);

        String expectedName = userSaved.getName();

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN", companySaved.getId());

        ResponseEntity<RestResponsePage<UserDetailsDTO>> responseEntity = testRestTemplate
                .exchange("/users/active?name=" + expectedName,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<UserDetailsDTO>>() {
                        });

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();

        List<UserDetailsDTO> userList = responseEntity.getBody().toList();
        Assertions.assertThat(userList).isNotEmpty().hasSize(1);
        Assertions.assertThat(userList.get(0).getName()).isEqualTo(expectedName);
        Assertions.assertThat(userList.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("Returns a void page of active users when users does not exist")
    void findAllActiveUsers_ReturnsVoidPageOfActiveUsers_WhenUsersDoesNotExist() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN", 1L);

        ResponseEntity<RestResponsePage<UserDetailsDTO>> responseEntity = testRestTemplate
                .exchange("/users/active?name=NonExistentName",
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<UserDetailsDTO>>() {
                        });

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().toList()).isEmpty();
    }

    @Test
    @DisplayName("Returns a page of inactive users when users exist")
    void findAllInactiveUsers_ReturnsPageOfInactiveUsers_WhenUsersExist() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        User userSaved = UserCreator.createValidInactiveUserToBeSaved();
        userSaved.setCompany(companySaved);
        userRepository.save(userSaved);

        String expectedName = userSaved.getName();

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN", companySaved.getId());

        ResponseEntity<RestResponsePage<UserDetailsDTO>> responseEntity = testRestTemplate
                .exchange("/users/inactive",
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<UserDetailsDTO>>() {
                        });

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();

        List<UserDetailsDTO> userList = responseEntity.getBody().toList();
        Assertions.assertThat(userList).isNotEmpty().hasSize(1);
        Assertions.assertThat(userList.get(0).getName()).isEqualTo(expectedName);
        Assertions.assertThat(userList.get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("Returns a void page of inactive users when users does not exist")
    void findAllInactiveUsers_ReturnsVoidPageOfInactiveUsers_WhenUsersDoesNotExist() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN", 1L);

        ResponseEntity<RestResponsePage<UserDetailsDTO>> responseEntity = testRestTemplate
                .exchange("/users/inactive",
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<UserDetailsDTO>>() {
                        });

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().toList()).isEmpty();
    }

    @Test
    @DisplayName("Saves and returns user details when user data is valid")
    void createUser_SavesAndReturnsUserDetails_WhenUserDataIsValid() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        UserCreateDTO inputDto = UserCreator.createValidUserCreateDTO();
        inputDto.setCompanyId(companySaved.getId());
        inputDto.setRole(Role.ROLE_EMPLOYEE);

        HttpEntity<UserCreateDTO> requestEntity = createHttpEntityWithToken(inputDto, "ADMIN", companySaved.getId());

        ResponseEntity<UserDetailsDTO> responseEntity = testRestTemplate
                .exchange("/users",
                        HttpMethod.POST,
                        requestEntity,
                        UserDetailsDTO.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getId()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getEmail()).isEqualTo(inputDto.getEmail());
    }

    @Test
    @DisplayName("Returns 400 bad request when user email is already registered")
    void createUser_Returns400BadRequest_WhenUserEmailIsAlreadyRegistered() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        User existingUser = UserCreator.createValidActiveUserToBeSaved();
        existingUser.setCompany(companySaved);
        userRepository.save(existingUser);

        UserCreateDTO inputDto = UserCreator.createValidUserCreateDTO();
        inputDto.setCompanyId(companySaved.getId());
        inputDto.setEmail(existingUser.getEmail());

        HttpEntity<UserCreateDTO> requestEntity = createHttpEntityWithToken(inputDto, "ADMIN", companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users",
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Returns 400 bad request when user CPF is blank")
    void createUser_Returns400BadRequest_WhenUserCpfIsBlank() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        UserCreateDTO inputDto = UserCreator.createValidUserCreateDTO();
        inputDto.setCompanyId(companySaved.getId());
        inputDto.setCpf("");

        HttpEntity<UserCreateDTO> requestEntity = createHttpEntityWithToken(inputDto, "ADMIN", companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users",
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Updates and returns user details when user exists")
    void updateUser_UpdatesAndReturnsUserDetails_WhenUserExists() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        User existingUser = UserCreator.createValidActiveUserToBeSaved();
        existingUser.setCompany(companySaved);
        existingUser.setRole(Role.ROLE_EMPLOYEE);
        userRepository.save(existingUser);

        UserUpdateDTO updateDto = UserCreator.createValidUserUpdateDTO();
        updateDto.setCompanyId(companySaved.getId());
        updateDto.setRole(Role.ROLE_EMPLOYEE);
        String expectedName = updateDto.getName();

        HttpEntity<UserUpdateDTO> requestEntity = createHttpEntityWithToken(updateDto, "ADMIN", companySaved.getId());

        ResponseEntity<UserDetailsDTO> responseEntity = testRestTemplate
                .exchange("/users/" + existingUser.getId(),
                        HttpMethod.PUT,
                        requestEntity,
                        UserDetailsDTO.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("Returns 404 not found when user does not exists")
    void updateUser_Returns404NotFound_WhenUserDoesNotExists() {
        UserUpdateDTO updateDto = UserCreator.createValidUserUpdateDTO();
        HttpEntity<UserUpdateDTO> requestEntity = createHttpEntityWithToken(updateDto, "ADMIN", 1L);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/999",
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Returns 404 not found when user exists and is inactive")
    void updateUser_Returns404NotFound_WhenUserExistsAndIsInactive() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        User inactiveUser = UserCreator.createValidInactiveUserToBeSaved();
        inactiveUser.setCompany(companySaved);
        userRepository.save(inactiveUser);

        UserUpdateDTO updateDto = UserCreator.createValidUserUpdateDTO();
        updateDto.setCompanyId(companySaved.getId());

        HttpEntity<UserUpdateDTO> requestEntity = createHttpEntityWithToken(updateDto, "ADMIN", companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/" + inactiveUser.getId(),
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Deactivates user and returns http status code 204 no content when user exists and is active")
    void deactivateUser_DeactivatesUserAndReturnsNoContent_WhenUserExistsAndIsActive() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        User activeUser = UserCreator.createValidActiveUserToBeSaved();
        activeUser.setCompany(companySaved);
        userRepository.save(activeUser);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN", companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/deactivate/" + activeUser.getId(),
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Optional<User> updatedUser = userRepository.findById(activeUser.getId());
        Assertions.assertThat(updatedUser).isPresent();
        Assertions.assertThat(updatedUser.get().isActive()).isFalse();
    }

    @Test
    @DisplayName("Returns 404 not found when user does not exists")
    void deactivateUser_Returns404NotFound_WhenUserDoesNotExist() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN", 1L);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/deactivate/999",
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Activates user and returns http status code 204 no content when user exists and is inactive")
    void activateUser_ActivatesUserAndReturnsNoContent_WhenUserExistsAndIsInactive() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        User inactiveUser = UserCreator.createValidInactiveUserToBeSaved();
        inactiveUser.setCompany(companySaved);
        userRepository.save(inactiveUser);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN", companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/activate/" + inactiveUser.getId(),
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Optional<User> updatedUser = userRepository.findById(inactiveUser.getId());
        Assertions.assertThat(updatedUser).isPresent();
        Assertions.assertThat(updatedUser.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("Returns 404 not found when user does not exists")
    void activateUser_Returns404NotFound_WhenUserDoesNotExist() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "ADMIN", 1L);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/activate/999",
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // TESTS FOR MANAGER
    @Test
    @DisplayName("findAllActiveUsers returns only users from the same company when authenticated user is a manager")
    void findAllActiveUsers_ReturnsOnlyUsersFromSameCompany_WhenUserIsManager() {
        Address addressSaved1 = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company myCompany = CompanyCreator.createValidActiveCompanyToBeSaved();
        myCompany.setHeadquarters(addressSaved1);
        Company myCompanySaved = companyRepository.save(myCompany);

        Address alternativeAddress = AddressCreator.createValidAddressToBeSaved();
        alternativeAddress.setZipCode("02002000");
        Address addressSaved2 = addressRepository.save(alternativeAddress);

        Company otherCompany = CompanyCreator.createValidActiveCompanyToBeSaved();
        otherCompany.setCnpj("99999999000177");
        otherCompany.setHeadquarters(addressSaved2);
        Company otherCompanySaved = companyRepository.save(otherCompany);

        User myUser = UserCreator.createValidActiveUserToBeSaved();
        myUser.setCompany(myCompanySaved);
        myUser.setRole(Role.ROLE_EMPLOYEE);
        userRepository.save(myUser);

        User otherUser = UserCreator.createValidActiveUserToBeSaved();
        otherUser.setEmail("other.employee@test.com");
        otherUser.setCpf("11111111111");
        otherUser.setEmployeeId("EMP-9999");
        otherUser.setCompany(otherCompanySaved);
        otherUser.setRole(Role.ROLE_EMPLOYEE);
        userRepository.save(otherUser);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER", myCompanySaved.getId());

        ResponseEntity<RestResponsePage<UserDetailsDTO>> responseEntity = testRestTemplate
                .exchange("/users/active",
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<RestResponsePage<UserDetailsDTO>>() {
                        });

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();

        List<UserDetailsDTO> userList = responseEntity.getBody().toList();
        Assertions.assertThat(userList).hasSize(1);
        Assertions.assertThat(userList.get(0).getEmail()).isEqualTo(myUser.getEmail());
    }

    @Test
    @DisplayName("Saves user in same company when manager creates valid user")
    void createUser_SavesUserInSameCompany_WhenManagerCreatesValidUser() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        UserCreateDTO inputDto = UserCreator.createValidUserCreateDTO();
        inputDto.setCompanyId(companySaved.getId());
        inputDto.setRole(Role.ROLE_EMPLOYEE);

        HttpEntity<UserCreateDTO> requestEntity = createHttpEntityWithToken(inputDto, "MANAGER", companySaved.getId());

        ResponseEntity<UserDetailsDTO> responseEntity = testRestTemplate
                .exchange("/users",
                        HttpMethod.POST,
                        requestEntity,
                        UserDetailsDTO.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().getId()).isNotNull();
    }

    @Test
    @DisplayName("Returns 400 bad request when manager tries to create admin user")
    void createUser_Returns400BadRequest_WhenManagerTriesToCreateAdminUser() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        UserCreateDTO inputDto = UserCreator.createValidUserCreateDTO();
        inputDto.setCompanyId(companySaved.getId());
        inputDto.setRole(Role.ROLE_ADMIN);

        HttpEntity<UserCreateDTO> requestEntity = createHttpEntityWithToken(inputDto, "MANAGER", companySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users",
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("updateUser returns 404 Not Found when manager tries to update user from a different company")
    void updateUser_Returns404NotFound_WhenUserBelongsToDifferentCompanyAndUserIsManager() {
        Address addressSaved1 = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company myCompany = CompanyCreator.createValidActiveCompanyToBeSaved();
        myCompany.setHeadquarters(addressSaved1);
        Company myCompanySaved = companyRepository.save(myCompany);

        Address alternativeAddress = AddressCreator.createValidAddressToBeSaved();
        alternativeAddress.setZipCode("03003000");
        Address addressSaved2 = addressRepository.save(alternativeAddress);

        Company otherCompany = CompanyCreator.createValidActiveCompanyToBeSaved();
        otherCompany.setCnpj("99999999000199");
        otherCompany.setHeadquarters(addressSaved2);
        Company otherCompanySaved = companyRepository.save(otherCompany);

        User otherUser = UserCreator.createValidActiveUserToBeSaved();
        otherUser.setCompany(otherCompanySaved);
        otherUser.setRole(Role.ROLE_EMPLOYEE);
        userRepository.save(otherUser);

        UserUpdateDTO updateDto = UserCreator.createValidUserUpdateDTO();
        updateDto.setCompanyId(myCompanySaved.getId());
        updateDto.setRole(Role.ROLE_EMPLOYEE);

        HttpEntity<UserUpdateDTO> requestEntity = createHttpEntityWithToken(updateDto, "MANAGER", myCompanySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/" + otherUser.getId(),
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deactivateUser returns 404 Not Found when manager tries to deactivate user from a different company")
    void deactivateUser_Returns404NotFound_WhenUserBelongsToDifferentCompanyAndUserIsManager() {
        Address addressSaved1 = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company myCompany = CompanyCreator.createValidActiveCompanyToBeSaved();
        myCompany.setHeadquarters(addressSaved1);
        Company myCompanySaved = companyRepository.save(myCompany);

        Address alternativeAddress = AddressCreator.createValidAddressToBeSaved();
        alternativeAddress.setZipCode("04004000");
        Address addressSaved2 = addressRepository.save(alternativeAddress);

        Company otherCompany = CompanyCreator.createValidActiveCompanyToBeSaved();
        otherCompany.setCnpj("99999999000188");
        otherCompany.setHeadquarters(addressSaved2);
        Company otherCompanySaved = companyRepository.save(otherCompany);

        User otherUser = UserCreator.createValidActiveUserToBeSaved();
        otherUser.setCompany(otherCompanySaved);
        otherUser.setRole(Role.ROLE_EMPLOYEE);
        userRepository.save(otherUser);

        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "MANAGER", myCompanySaved.getId());

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/deactivate/" + otherUser.getId(),
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // TESTS FOR EMPLOYEE
    @Test
    @DisplayName("findAllActiveUsers returns 403 Forbidden when authenticated user is an employee")
    void findAllActiveUsers_Returns403Forbidden_WhenUserIsEmployee() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE", 1L);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/active",
                        HttpMethod.GET,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("findAllInactiveUsers returns 403 Forbidden when authenticated user is an employee")
    void findAllInactiveUsers_Returns403Forbidden_WhenUserIsEmployee() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE", 1L);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/inactive",
                        HttpMethod.GET,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("createUser returns 403 Forbidden when authenticated user is an employee")
    void createUser_Returns403Forbidden_WhenUserIsEmployee() {
        UserCreateDTO inputDto = UserCreator.createValidUserCreateDTO();
        HttpEntity<UserCreateDTO> requestEntity = createHttpEntityWithToken(inputDto, "EMPLOYEE", 1L);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users",
                        HttpMethod.POST,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("updateUser returns 403 Forbidden when authenticated user is an employee")
    void updateUser_Returns403Forbidden_WhenUserIsEmployee() {
        UserUpdateDTO updateDto = UserCreator.createValidUserUpdateDTO();
        HttpEntity<UserUpdateDTO> requestEntity = createHttpEntityWithToken(updateDto, "EMPLOYEE", 1L);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/1",
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("activateUser returns 403 Forbidden when authenticated user is an employee")
    void activateUser_Returns403Forbidden_WhenUserIsEmployee() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE", 1L);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/activate/1",
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("deactivateUser returns 403 Forbidden when authenticated user is an employee")
    void deactivateUser_Returns403Forbidden_WhenUserIsEmployee() {
        HttpEntity<Void> requestEntity = createHttpEntityWithToken(null, "EMPLOYEE", 1L);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/users/deactivate/1",
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

}