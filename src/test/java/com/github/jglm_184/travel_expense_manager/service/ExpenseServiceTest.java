package com.github.jglm_184.travel_expense_manager.service;

import com.github.jglm_184.travel_expense_manager.dto.ExpenseCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ExpenseDetailsDTO;
import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.exception.ResourceNotFoundException;
import com.github.jglm_184.travel_expense_manager.mapper.ExpenseMapper;
import com.github.jglm_184.travel_expense_manager.model.Expense;
import com.github.jglm_184.travel_expense_manager.model.Travel;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import com.github.jglm_184.travel_expense_manager.repository.ExpenseRepository;
import com.github.jglm_184.travel_expense_manager.repository.TravelRepository;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import com.github.jglm_184.travel_expense_manager.util.ExpenseCreator;
import com.github.jglm_184.travel_expense_manager.util.TravelCreator;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for ExpenseService")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private TravelRepository travelRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseService expenseService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = UserCreator.createValidActiveUser();
        currentUser.setRole(Role.ROLE_EMPLOYEE);

        Authentication authentication = BDDMockito.mock(Authentication.class);
        BDDMockito.when(authentication.getName()).thenReturn(currentUser.getId().toString());

        SecurityContext securityContext = BDDMockito.mock(SecurityContext.class);
        BDDMockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        BDDMockito.when(userRepository.findById(currentUser.getId()))
                .thenReturn(Optional.of(currentUser));
    }

    private void changeCurrentUserRole(Role role, Long id) {
        currentUser.setRole(role);
        currentUser.setId(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        BDDMockito.when(authentication.getName()).thenReturn(id.toString());

        BDDMockito.when(userRepository.findById(id))
                .thenReturn(Optional.of(currentUser));
    }

    @Test
    @DisplayName("Adds expense successfully when travel is OPEN and expense date is valid")
    void addExpenseToTravel_AddsExpense_WhenTravelIsOpenAndDateIsValid() {

        ExpenseCreateDTO createDTO = ExpenseCreator.createValidExpenseCreateDTO();

        Travel travel = TravelCreator.createValidTravelInOpenStatus();
        travel.getUser().setId(currentUser.getId());
        travel.setStartDate(createDTO.getDate().minusDays(1));
        travel.setEndDate(createDTO.getDate().plusDays(1));

        Expense expense = ExpenseCreator.createValidExpense();
        ExpenseDetailsDTO expectedResponse = ExpenseCreator.createValidExpenseDetailsDTO();

        BDDMockito.when(travelRepository.findById(1L))
                .thenReturn(Optional.of(travel));

        BDDMockito.when(expenseMapper.toExpense(createDTO))
                .thenReturn(expense);

        BDDMockito.when(expenseRepository.save(ArgumentMatchers.any(Expense.class)))
                .thenReturn(expense);

        BDDMockito.when(expenseMapper.toDto(expense))
                .thenReturn(expectedResponse);

        ExpenseDetailsDTO response = expenseService.addExpenseToTravel(1L, createDTO);

        Assertions.assertThat(response).isNotNull();

        BDDMockito.then(expenseRepository)
                .should()
                .save(ArgumentMatchers.any(Expense.class));
    }

    @Test
    @DisplayName("Throws BusinessException when adding expense to travel that is not OPEN")
    void addExpenseToTravel_ThrowsBusinessException_WhenTravelIsNotOpen() {

        ExpenseCreateDTO dto = ExpenseCreator.createValidExpenseCreateDTO();

        Travel travel = TravelCreator.createValidTravelInSubmittedStatus();
        travel.getUser().setId(currentUser.getId());

        BDDMockito.when(travelRepository.findById(1L))
                .thenReturn(Optional.of(travel));

        Throwable thrown = Assertions.catchThrowable(() ->
                expenseService.addExpenseToTravel(1L, dto));

        Assertions.assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot add expenses");

        BDDMockito.then(expenseRepository)
                .should(BDDMockito.never())
                .save(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Throws BusinessException when expense date is outside travel period")
    void addExpenseToTravel_ThrowsBusinessException_WhenExpenseDateIsInvalid() {

        ExpenseCreateDTO dto = ExpenseCreator.createExpenseCreateDTOWithInvalidDate();

        Travel travel = TravelCreator.createValidTravelInOpenStatus();
        travel.getUser().setId(currentUser.getId());

        BDDMockito.when(travelRepository.findById(1L))
                .thenReturn(Optional.of(travel));

        Throwable thrown = Assertions.catchThrowable(() ->
                expenseService.addExpenseToTravel(1L, dto));

        Assertions.assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expense date");

        BDDMockito.then(expenseRepository)
                .should(BDDMockito.never())
                .save(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Deletes expense successfully when travel status is OPEN")
    void deleteExpense_DeletesExpense_WhenTravelStatusIsOpen() {

        Expense expense = ExpenseCreator.createValidExpense();

        Travel travel = TravelCreator.createValidTravelInOpenStatus();
        travel.getUser().setId(currentUser.getId());

        expense.setTravel(travel);

        BDDMockito.when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));

        expenseService.deleteExpense(1L);

        BDDMockito.then(expenseRepository)
                .should()
                .delete(expense);
    }

    @Test
    @DisplayName("Throws BusinessException when deleting expense from non OPEN travel")
    void deleteExpense_ThrowsBusinessException_WhenTravelStatusIsNotOpen() {

        Expense expense = ExpenseCreator.createValidExpense();

        Travel travel = TravelCreator.createValidTravelInSubmittedStatus();
        travel.getUser().setId(currentUser.getId());

        expense.setTravel(travel);

        BDDMockito.when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));

        Throwable thrown = Assertions.catchThrowable(() ->
                expenseService.deleteExpense(1L));

        Assertions.assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete expenses");

        BDDMockito.then(expenseRepository)
                .should(BDDMockito.never())
                .delete(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when expense does not exist")
    void deleteExpense_ThrowsResourceNotFoundException_WhenExpenseDoesNotExist() {

        BDDMockito.when(expenseRepository.findById(1L))
                .thenReturn(Optional.empty());

        Throwable thrown = Assertions.catchThrowable(() ->
                expenseService.deleteExpense(1L));

        Assertions.assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense not found");
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when employee accesses another employee expense")
    void deleteExpense_ThrowsResourceNotFoundException_WhenEmployeeAccessesAnotherExpense() {

        Expense expense = ExpenseCreator.createValidExpense();

        Travel travel = TravelCreator.createValidTravelInOpenStatus();
        travel.getUser().setId(999L);

        expense.setTravel(travel);

        BDDMockito.when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));

        Throwable thrown = Assertions.catchThrowable(() ->
                expenseService.deleteExpense(1L));

        Assertions.assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense not found");
    }

    @Test
    @DisplayName("Deletes expense successfully when manager belongs to same company")
    void deleteExpense_DeletesExpense_WhenManagerBelongsToSameCompany() {

        changeCurrentUserRole(Role.ROLE_MANAGER, 2L);

        Expense expense = ExpenseCreator.createValidExpense();

        Travel travel = TravelCreator.createValidTravelInOpenStatus();
        travel.getCompany().setId(currentUser.getCompany().getId());

        expense.setTravel(travel);

        BDDMockito.when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));

        expenseService.deleteExpense(1L);

        BDDMockito.then(expenseRepository)
                .should()
                .delete(expense);
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when manager accesses expense from another company")
    void deleteExpense_ThrowsResourceNotFoundException_WhenManagerFromAnotherCompany() {

        changeCurrentUserRole(Role.ROLE_MANAGER, 2L);

        Expense expense = ExpenseCreator.createValidExpense();

        Travel travel = TravelCreator.createValidTravelInOpenStatus();
        travel.getCompany().setId(999L);

        expense.setTravel(travel);

        BDDMockito.when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));

        Throwable thrown = Assertions.catchThrowable(() ->
                expenseService.deleteExpense(1L));

        Assertions.assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense not found");
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when authenticated user is not found")
    void addExpenseToTravel_ThrowsResourceNotFoundException_WhenAuthenticatedUserDoesNotExist() {
        ExpenseCreateDTO dto = ExpenseCreator.createValidExpenseCreateDTO();

        Travel travel = TravelCreator.createValidTravelInOpenStatus();

        BDDMockito.when(travelRepository.findById(1L))
                .thenReturn(Optional.of(travel));

        BDDMockito.when(userRepository.findById(currentUser.getId()))
                .thenReturn(Optional.empty());

        Throwable thrown = Assertions.catchThrowable(() ->
                expenseService.addExpenseToTravel(1L, dto));

        Assertions.assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Authenticated user metadata not found");
    }
}