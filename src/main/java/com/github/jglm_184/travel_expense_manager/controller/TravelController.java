package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.TravelCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelUpdateDTO;
import com.github.jglm_184.travel_expense_manager.service.TravelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("travels")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<TravelDetailsDTO>> getAllTravels(Pageable pageable) {
        Page<TravelDetailsDTO> travels = travelService.findAllTravels(pageable);
        return ResponseEntity.ok(travels);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<TravelDetailsDTO> createTravel(@Valid @RequestBody TravelCreateDTO travelCreateDTO) {
        TravelDetailsDTO createdTravel = travelService.createTravel(travelCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTravel);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<TravelDetailsDTO> updateTravel(
            @PathVariable Long id,
            @Valid @RequestBody TravelUpdateDTO travelUpdateDTO) {
        TravelDetailsDTO updatedTravel = travelService.updateTravel(id, travelUpdateDTO);
        return ResponseEntity.ok(updatedTravel);
    }

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> submitTravel(@PathVariable Long id) {
        travelService.submitTravelForApproval(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> approveTravel(@PathVariable Long id) {
        travelService.approveTravel(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> rejectTravel(@PathVariable Long id) {
        travelService.rejectTravel(id);
        return ResponseEntity.noContent().build();
    }
}