package com.github.jglm_184.travel_expense_manager.integration;

import com.github.jglm_184.travel_expense_manager.model.Address;
import com.github.jglm_184.travel_expense_manager.model.Company;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import com.github.jglm_184.travel_expense_manager.repository.AddressRepository;
import com.github.jglm_184.travel_expense_manager.repository.CompanyRepository;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import com.github.jglm_184.travel_expense_manager.util.AddressCreator;
import com.github.jglm_184.travel_expense_manager.util.CompanyCreator;
import com.github.jglm_184.travel_expense_manager.util.UserCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    @DisplayName("login returns 200 OK and JWT token when credentials are valid")
    void login_Returns200AndToken_WhenCredentialsAreValid() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        User user = UserCreator.createValidActiveUserToBeSaved();
        user.setCompany(companySaved);
        user.setRole(Role.ROLE_EMPLOYEE);

        String rawPassword = "password123";
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);

        Map<String, String> loginRequest = Map.of(
                "email", user.getEmail(),
                "password", rawPassword
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest);

        ResponseEntity<Map> responseEntity = testRestTemplate
                .exchange("/auth/login", HttpMethod.POST, requestEntity, Map.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();

        Assertions.assertThat(responseEntity.getBody().get("accessToken")).isNotNull();
        Assertions.assertThat(responseEntity.getBody().get("expiresIn")).isEqualTo(3600);
    }

    @Test
    @DisplayName("login returns Unauthorized or BadRequest when password is invalid")
    void login_ReturnsError_WhenPasswordIsInvalid() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        User user = UserCreator.createValidActiveUserToBeSaved();
        user.setCompany(companySaved);
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        Map<String, String> loginRequest = Map.of(
                "email", user.getEmail(),
                "password", "wrong_password"
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/auth/login", HttpMethod.POST, requestEntity, Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("login returns Error when user is inactive")
    void login_ReturnsError_WhenUserIsInactive() {
        Address addressSaved = addressRepository.save(AddressCreator.createValidAddressToBeSaved());
        Company company = CompanyCreator.createValidActiveCompanyToBeSaved();
        company.setHeadquarters(addressSaved);
        Company companySaved = companyRepository.save(company);

        User user = UserCreator.createValidInactiveUserToBeSaved();
        user.setCompany(companySaved);
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        Map<String, String> loginRequest = Map.of(
                "email", user.getEmail(),
                "password", "password123"
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/auth/login", HttpMethod.POST, requestEntity, Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("login returns Error when user does not exist in database")
    void login_ReturnsError_WhenUserDoesNotExist() {
        Map<String, String> loginRequest = Map.of(
                "email", "non.existent.user@test.com",
                "password", "anyPassword123"
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest);

        ResponseEntity<Void> responseEntity = testRestTemplate
                .exchange("/auth/login", HttpMethod.POST, requestEntity, Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST);
    }
}
