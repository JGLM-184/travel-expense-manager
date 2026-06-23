package com.github.jglm_184.travel_expense_manager.repository;

import com.github.jglm_184.travel_expense_manager.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByIdAndActiveTrue(Long id);

    Optional<Company> findByIdAndActiveFalse(Long id);

    Optional<Company> findByCnpj(String cnpj);

    Page<Company> findByActiveTrue(Pageable pageable);

    Page<Company> findByActiveFalse(Pageable pageable);
}
