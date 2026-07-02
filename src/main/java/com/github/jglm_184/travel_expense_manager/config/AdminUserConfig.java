package com.github.jglm_184.travel_expense_manager.config;

import com.github.jglm_184.travel_expense_manager.model.Address;
import com.github.jglm_184.travel_expense_manager.model.Company;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import com.github.jglm_184.travel_expense_manager.repository.AddressRepository;
import com.github.jglm_184.travel_expense_manager.repository.CompanyRepository;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class AdminUserConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        Optional<User> userAdmin = userRepository.findByEmail("admin@travelexpense.com");

        userAdmin.ifPresentOrElse(
                user -> {
                    System.out.println("Admin já existe");
                },
                () -> {
                    Address address = Address.builder()
                            .zipCode("00000000")
                            .street("Admin")
                            .neighborhood("Admin")
                            .city("Admin")
                            .state("Admin")
                            .build();
                    addressRepository.save(address);

                    Company company = Company.builder()
                            .cnpj("00000000000000")
                            .companyName("Admin")
                            .tradeName("Admin")
                            .active(true)
                            .headquarters(address)
                            .build();
                    companyRepository.save(company);

                    User user = User.builder()
                            .name("Admin")
                            .cpf("00000000000")
                            .employeeId("Admin")
                            .department("Admin")
                            .email("admin@travelexpense.com")
                            .password(bCryptPasswordEncoder.encode("123456"))
                            .company(company)
                            .role(Role.ROLE_ADMIN)
                            .active(true)
                            .build();
                    userRepository.save(user);

                }
        );
    }
}
