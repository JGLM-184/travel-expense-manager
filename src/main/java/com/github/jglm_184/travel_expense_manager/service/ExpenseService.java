package com.github.jglm_184.travel_expense_manager.service;

import com.github.jglm_184.travel_expense_manager.dto.ExpenseCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ExpenseDetailsDTO;
import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.exception.ResourceNotFoundException;
import com.github.jglm_184.travel_expense_manager.mapper.ExpenseMapper;
import com.github.jglm_184.travel_expense_manager.model.Expense;
import com.github.jglm_184.travel_expense_manager.model.Travel;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.model.enums.TravelStatus;
import com.github.jglm_184.travel_expense_manager.repository.ExpenseRepository;
import com.github.jglm_184.travel_expense_manager.repository.TravelRepository;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;

    @Transactional
    public ExpenseDetailsDTO addExpenseToTravel(Long travelId, ExpenseCreateDTO expenseCreateDTO) {
        Travel travel = findTravelByIdAndVerifyAccess(travelId);

        if (travel.getStatus() != TravelStatus.OPEN) {
            throw new BusinessException("Cannot add expenses to a travel report that is not OPEN");
        }

        if (expenseCreateDTO.getDate().isBefore(travel.getStartDate()) ||
                expenseCreateDTO.getDate().isAfter(travel.getEndDate())) {
            throw new BusinessException("The expense date must be within the travel start and end dates");
        }

        Expense expenseToBeSaved = expenseMapper.toExpense(expenseCreateDTO);
        expenseToBeSaved.setTravel(travel);

        Expense savedExpense = expenseRepository.save(expenseToBeSaved);
        return expenseMapper.toDto(savedExpense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = findExpenseByIdAndVerifyAccess(id);

        if (expense.getTravel().getStatus() != TravelStatus.OPEN) {
            throw new BusinessException("Cannot delete expenses from a travel report that is not OPEN");
        }

        expenseRepository.delete(expense);
    }

        private Expense findExpenseByIdAndVerifyAccess(Long id) {
            Expense expense = expenseRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

            User currentUser = getAuthenticatedUser();

            System.out.println("Current role: " + currentUser.getRole());
            System.out.println("Role name: " + currentUser.getRole().name());
            System.out.println("Current id: " + currentUser.getId());
            System.out.println("Owner id: " + expense.getTravel().getUser().getId());

            if (currentUser.getRole().name().equals("ROLE_EMPLOYEE") &&
                    !expense.getTravel().getUser().getId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Expense not found");
            }

            if (currentUser.getRole().name().equals("ROLE_MANAGER") &&
                    !expense.getTravel().getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new ResourceNotFoundException("Expense not found");
            }

            return expense;
        }

    private Travel findTravelByIdAndVerifyAccess(Long id) {
        Travel travel = travelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Travel report not found"));

        User currentUser = getAuthenticatedUser();

        if (currentUser.getRole().name().equals("ROLE_EMPLOYEE") && !travel.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Travel report not found");
        }

        if (currentUser.getRole().name().equals("ROLE_MANAGER") && !travel.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new ResourceNotFoundException("Travel report not found");
        }

        return travel;
    }

    private User getAuthenticatedUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(Long.parseLong(userIdStr))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user metadata not found"));
    }
}