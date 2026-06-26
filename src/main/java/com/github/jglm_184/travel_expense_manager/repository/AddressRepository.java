package com.github.jglm_184.travel_expense_manager.repository;

import com.github.jglm_184.travel_expense_manager.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByZipCode(String zipCode);
}
