package com.github.jglm_184.travel_expense_manager.repository;

import com.github.jglm_184.travel_expense_manager.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
