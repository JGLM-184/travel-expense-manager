package com.github.jglm_184.travel_expense_manager.repository;

import com.github.jglm_184.travel_expense_manager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Page<User> findByNameContainingIgnoreCaseAndDepartmentContainingIgnoreCaseAndActiveTrue(
            String name,
            String department,
            Pageable pageable);

    Page<User> findByCompanyIdAndNameContainingIgnoreCaseAndDepartmentContainingIgnoreCaseAndActiveTrue(
            Long companyId,
            String name,
            String department,
            Pageable pageable);

    Page<User> findByActiveTrue(Pageable pageable);

    Page<User> findByCompanyIdAndActiveTrue(Long companyId, Pageable pageable);

    Page<User> findByActiveFalse(Pageable pageable);

    Page<User> findByCompanyIdAndActiveFalse(Long companyId, Pageable pageable);

    Optional<User> findByIdAndActiveTrue(Long id);

    Optional<User> findByIdAndActiveFalse(Long id);

    Optional<User> findByCpf(String cpf);
}
