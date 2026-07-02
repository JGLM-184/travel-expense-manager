package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.UserCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserUpdateDTO;
import com.github.jglm_184.travel_expense_manager.service.UserService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for UserController")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        PageImpl<UserDetailsDTO> activeUserPage = new PageImpl<>(
                List.of(UserCreator.createActiveUserDetailsDTO()));

        PageImpl<UserDetailsDTO> inactiveUserPage = new PageImpl<>(
                List.of(UserCreator.createInactiveUserDetailsDTO()));

        BDDMockito.when(userService.findAllActiveUsers(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.any()))
                .thenReturn(activeUserPage);

        BDDMockito.when(userService.findAllInactiveUsers(ArgumentMatchers.any()))
                .thenReturn(inactiveUserPage);

        BDDMockito.when(userService.createUser(
                        ArgumentMatchers.any(UserCreateDTO.class)))
                .thenReturn(UserCreator.createActiveUserDetailsDTO());

        BDDMockito.when(userService.updateUser(
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.any(UserUpdateDTO.class)))
                .thenReturn(UserCreator.createActiveUserDetailsDTO());

        BDDMockito.doNothing()
                .when(userService)
                .activateUser(ArgumentMatchers.anyLong());

        BDDMockito.doNothing()
                .when(userService)
                .deactivateUser(ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("Returns a page of active users when users exist")
    void findAllActiveUsers_ReturnsPageOfActiveUsers_WhenUsersExist() {
        String expectedUserName = UserCreator.createActiveUserDetailsDTO().getName();

        ResponseEntity<Page<UserDetailsDTO>> responseEntity =
                userController.findAllActiveUsers("", "", null);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().toList())
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(responseEntity.getBody()
                        .toList()
                        .get(0)
                        .getName())
                .isEqualTo(expectedUserName);

        Assertions.assertThat(responseEntity.getBody()
                        .toList()
                        .get(0)
                        .isActive())
                .isTrue();
    }

    @Test
    @DisplayName("Returns a page of inactive users when users exist")
    void findAllInactiveUsers_ReturnsPageOfInactiveUsers_WhenUsersExist() {
        String expectedUserName = UserCreator.createInactiveUserDetailsDTO().getName();

        ResponseEntity<Page<UserDetailsDTO>> responseEntity =
                userController.findAllInactiveUsers(null);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().toList())
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(responseEntity.getBody()
                        .toList()
                        .get(0)
                        .getName())
                .isEqualTo(expectedUserName);

        Assertions.assertThat(responseEntity.getBody()
                        .toList()
                        .get(0)
                        .isActive())
                .isFalse();
    }

    @Test
    @DisplayName("Saves and returns user details when user data is valid")
    void createUser_SavesAndReturnsUserDetails_WhenUserDataIsValid() {
        UserCreateDTO inputDto = UserCreator.createValidUserCreateDTO();

        UserDetailsDTO expectedDetails = UserCreator.createActiveUserDetailsDTO();

        ResponseEntity<UserDetailsDTO> responseEntity =
                userController.createUser(inputDto);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        Assertions.assertThat(responseEntity.getBody())
                .isNotNull()
                .isEqualTo(expectedDetails);
    }

    @Test
    @DisplayName("Updates and returns user details when user data is valid")
    void updateUser_UpdatesAndReturnsUserDetails_WhenUserDataIsValid() {
        UserUpdateDTO updateDto = UserCreator.createValidUserUpdateDTO();

        UserDetailsDTO expectedDetails = UserCreator.createActiveUserDetailsDTO();

        ResponseEntity<UserDetailsDTO> responseEntity =
                userController.updateUser(1L, updateDto);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        Assertions.assertThat(responseEntity.getBody())
                .isNotNull()
                .isEqualTo(expectedDetails);
    }

    @Test
    @DisplayName("Deactivates user when user ID is valid")
    void deactivateUser_DeactivatesUser_WhenUserExists() {
        ResponseEntity<Void> responseEntity =
                userController.deactivateUser(1L);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Activates user when user ID is valid")
    void activateUser_ActivatesUser_WhenUserExists() {
        ResponseEntity<Void> responseEntity =
                userController.activateUser(1L);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

}