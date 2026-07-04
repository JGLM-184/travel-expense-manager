package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.ExpenseCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ExpenseDetailsDTO;
import com.github.jglm_184.travel_expense_manager.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/expenses")
@Tag(name = "Expense", description = "Endpoints for managing expenses attached to corporate travel reports")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/travels/{travelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Add a new expense to a travel report",
            description = "Creates and links a new corporate expense (such as meals, lodging, or transport) to a specific " +
                    "travel report by its ID. New expenses can only be added if the travel status is currently OPEN. " +
                    "Employees can only add expenses to their own travel reports. Requires ADMIN, MANAGER, or EMPLOYEE role.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Expense successfully added to the travel report"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload, the travel report is not in OPEN status, or the expense date is outside the travel period"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "404", description = "Travel report not found")
            }
    )
    public ResponseEntity<ExpenseDetailsDTO> addExpenseToTravel(
            @PathVariable Long travelId,
            @Valid @RequestBody ExpenseCreateDTO expenseCreateDTO) {
        ExpenseDetailsDTO createdExpense = expenseService.addExpenseToTravel(travelId, expenseCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Delete an expense from an open travel report",
            description = "Removes a specific expense record by its unique ID. Deletion is strictly blocked if the parent " +
                    "travel report has already been submitted, approved, or rejected (must be in OPEN status). Employees " +
                    "can only delete expenses from their own reports. Requires ADMIN, MANAGER, or EMPLOYEE role.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Expense successfully deleted"),
                    @ApiResponse(responseCode = "400", description = "The parent travel report is not in OPEN status"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "404", description = "Expense record not found")
            }
    )
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}