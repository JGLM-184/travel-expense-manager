package com.github.jglm_184.travel_expense_manager.repository;

import com.github.jglm_184.travel_expense_manager.model.Travel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelRepository extends JpaRepository<Travel, Long> {
    Page<Travel> findByCompanyId(Long companyId, Pageable pageable);

    Page<Travel> findByUserId(Long userId, Pageable pageable);
}
