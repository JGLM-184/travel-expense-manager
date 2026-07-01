package com.github.jglm_184.travel_expense_manager.service;

import com.github.jglm_184.travel_expense_manager.dto.UserCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserUpdateDTO;
import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.mapper.UserMapper;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import com.github.jglm_184.travel_expense_manager.repository.CompanyRepository;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import com.github.jglm_184.travel_expense_manager.util.FormatterUtil;
import com.github.jglm_184.travel_expense_manager.util.UserCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private FormatterUtil formatterUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        Jwt jwt = BDDMockito.mock(Jwt.class);
        BDDMockito.when(jwt.getClaimAsString("scope")).thenReturn("ROLE_ADMIN");
        BDDMockito.when(jwt.getSubject()).thenReturn("1");

        Authentication authentication = BDDMockito.mock(Authentication.class);
        BDDMockito.when(authentication.getPrincipal()).thenReturn(jwt);

        SecurityContext securityContext = BDDMockito.mock(SecurityContext.class);
        BDDMockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext(String role, String subject) {
        Jwt jwt = BDDMockito.mock(Jwt.class);
        BDDMockito.when(jwt.getClaimAsString("scope")).thenReturn(role);
        BDDMockito.when(jwt.getSubject()).thenReturn(subject);
        BDDMockito.when(jwt.getClaim("companyId")).thenReturn(1L);

        Authentication authentication = BDDMockito.mock(Authentication.class);
        BDDMockito.when(authentication.getPrincipal()).thenReturn(jwt);

        SecurityContext securityContext = BDDMockito.mock(SecurityContext.class);
        BDDMockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Saves and returns user details when DTO is fully filled")
    void createUser_SavesAndReturnsUserDetails_WhenUserIsAdminAndCreatingAnotherAdmin() {
        UserCreateDTO userToBeSaved = UserCreator.createValidUserCreateDTO();
        User userSaved = UserCreator.createValidActiveUser();
        UserDetailsDTO expectedResponse = UserCreator.createActiveUserDetailsDTO();

        BDDMockito.when(formatterUtil.cleanNumbers(ArgumentMatchers.anyString()))
                .thenReturn("12345678901");

        BDDMockito.when(userRepository.findByEmail(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        BDDMockito.when(userRepository.findByCpf(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        BDDMockito.when(userMapper.toUser(ArgumentMatchers.any(UserCreateDTO.class)))
                .thenReturn(userSaved);

        BDDMockito.when(bCryptPasswordEncoder.encode(ArgumentMatchers.anyString()))
                .thenReturn("encoded_password");

        BDDMockito.when(userRepository.save(ArgumentMatchers.any(User.class)))
                .thenReturn(userSaved);

        BDDMockito.when(userMapper.toDto(ArgumentMatchers.any(User.class)))
                .thenReturn(expectedResponse);

        UserDetailsDTO actualResponse = userService.createUser(userToBeSaved);

        Assertions.assertThat(actualResponse).isNotNull();
        Assertions.assertThat(actualResponse.getEmail()).isEqualTo(userToBeSaved.getEmail());
        Assertions.assertThat(actualResponse.getName()).isEqualTo(userToBeSaved.getName());
        Assertions.assertThat(actualResponse.isActive()).isTrue();

        BDDMockito.then(userRepository).should().save(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("Throws BusinessException when user email already exists")
    void createUser_ThrowsBusinessException_WhenEmailAlreadyExists() {
        UserCreateDTO dto = UserCreator.createValidUserCreateDTO();

        BDDMockito.when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(UserCreator.createValidActiveUser()));

        Throwable thrown = Assertions.catchThrowable(() -> userService.createUser(dto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User with this email already exists");

        BDDMockito.verify(userRepository, BDDMockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("Throws BusinessException when user CPF already exists")
    void createUser_ThrowsBusinessException_WhenCpfAlreadyExists() {
        UserCreateDTO dto = UserCreator.createValidUserCreateDTO();

        BDDMockito.when(formatterUtil.cleanNumbers(ArgumentMatchers.anyString()))
                .thenReturn("12345678901");

        BDDMockito.when(userRepository.findByEmail(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        BDDMockito.when(userRepository.findByCpf("12345678901"))
                .thenReturn(Optional.of(UserCreator.createValidActiveUser()));

        Throwable thrown = Assertions.catchThrowable(() -> userService.createUser(dto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User with this CPF already exists");

        BDDMockito.verify(userRepository, BDDMockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("Throws BusinessException when manager tries to create admin user")
    void createUser_ThrowsBusinessException_WhenManagerTriesToCreateAdmin() {
        mockSecurityContext("ROLE_MANAGER", "2");
        UserCreateDTO dto = UserCreator.createValidUserCreateDTO();
        dto.setRole(Role.ROLE_ADMIN);

        BDDMockito.when(formatterUtil.cleanNumbers(ArgumentMatchers.anyString()))
                .thenReturn("12345678901");

        BDDMockito.when(userRepository.findByEmail(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        BDDMockito.when(userRepository.findByCpf(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        BDDMockito.when(userMapper.toUser(ArgumentMatchers.any(UserCreateDTO.class)))
                .thenReturn(UserCreator.createValidActiveUser());

        BDDMockito.when(userRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(UserCreator.createValidActiveUser()));

        Throwable thrown = Assertions.catchThrowable(() -> userService.createUser(dto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Managers cannot create Admin users");

        BDDMockito.verify(userRepository, BDDMockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("Updates and returns user details when user exists")
    void updateUser_UpdatesAndReturnsUserDetails_WhenUserExists() {
        UserUpdateDTO updateDto = UserCreator.createValidUserUpdateDTO();
        User existingUser = UserCreator.createValidActiveUser();
        UserDetailsDTO expectedResponse = UserCreator.createActiveUserDetailsDTO();

        BDDMockito.when(userRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(existingUser));

        BDDMockito.when(userRepository.save(ArgumentMatchers.any(User.class)))
                .thenReturn(existingUser);

        BDDMockito.when(userMapper.toDto(ArgumentMatchers.any(User.class)))
                .thenReturn(expectedResponse);

        UserDetailsDTO actualResponse = userService.updateUser(1L, updateDto);

        Assertions.assertThat(actualResponse).isNotNull();
        BDDMockito.then(userRepository).should().save(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("Throws BusinessException when manager tries to promote user to Admin")
    void updateUser_ThrowsBusinessException_WhenManagerTriesToPromoteUserToAdmin() {
        mockSecurityContext("ROLE_MANAGER", "2");
        UserUpdateDTO updateDto = UserCreator.createValidUserUpdateDTO();
        updateDto.setRole(Role.ROLE_ADMIN);

        User existingUser = UserCreator.createValidActiveUser();
        existingUser.getCompany().setId(1L);

        BDDMockito.when(userRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(existingUser));

        BDDMockito.when(companyRepository.findByIdAndActiveTrue(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(existingUser.getCompany()));

        Throwable thrown = Assertions.catchThrowable(() -> userService.updateUser(1L, updateDto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Managers cannot promote users to Admin");

        BDDMockito.verify(userRepository, BDDMockito.never()).save(ArgumentMatchers.any(User.class));
    }
}