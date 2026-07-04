package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.ExpenseCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ExpenseDetailsDTO;
import com.github.jglm_184.travel_expense_manager.service.ExpenseService;
import com.github.jglm_184.travel_expense_manager.util.ExpenseCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for ExpenseController")
class ExpenseControllerTest {

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private ExpenseController expenseController;

    @BeforeEach
    void setUp() {
        BDDMockito.when(expenseService.addExpenseToTravel(
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.any(ExpenseCreateDTO.class)))
                .thenReturn(ExpenseCreator.createValidExpenseDetailsDTO());

        BDDMockito.doNothing()
                .when(expenseService)
                .deleteExpense(ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("Adds expense and returns expense details when expense data is valid")
    void addExpenseToTravel_AddsExpenseAndReturnsExpenseDetails_WhenExpenseDataIsValid() {
        ExpenseCreateDTO inputDto = ExpenseCreator.createValidExpenseCreateDTO();
        ExpenseDetailsDTO expectedDetails = ExpenseCreator.createValidExpenseDetailsDTO();

        ResponseEntity<ExpenseDetailsDTO> responseEntity =
                expenseController.addExpenseToTravel(1L, inputDto);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull().isEqualTo(expectedDetails);
    }

    @Test
    @DisplayName("Deletes expense when expense ID is valid")
    void deleteExpense_DeletesExpense_WhenExpenseIdIsValid() {
        ResponseEntity<Void> responseEntity = expenseController.deleteExpense(1L);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}