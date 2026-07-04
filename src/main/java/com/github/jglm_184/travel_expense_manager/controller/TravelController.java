package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.TravelCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelUpdateDTO;
import com.github.jglm_184.travel_expense_manager.service.TravelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("travels")
@RequiredArgsConstructor
@Tag(name = "Travel", description = "Endpoints for managing corporate travels, processing expense reports, " +
        "and approval workflows")
public class TravelController {

    private final TravelService travelService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Retrieve paginated travels",
            description = "Retrieves a paginated list of travels. ADMIN sees all records, MANAGER sees records from " +
                    "their company, and EMPLOYEE can only see their own travels. Requires ADMIN, MANAGER, or EMPLOYEE role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of travels"),
                    @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameters provided"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "403", description = "Forbidden. Authenticated user does not have access permissions")
            }
    )
    public ResponseEntity<Page<TravelDetailsDTO>> getAllTravels(@ParameterObject Pageable pageable) {
        Page<TravelDetailsDTO> travels = travelService.findAllTravels(pageable);
        return ResponseEntity.ok(travels);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Create a new travel request",
            description = "Creates a new corporate travel report. The initial status will be OPEN. The destination ZIP " +
                    "code is validated and resolved using ViaCEP. Requires ADMIN, MANAGER, or EMPLOYEE role.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Travel successfully created"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload, destination ZIP code " +
                            "is invalid/not found, or end date is before the start date"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "403", description = "Forbidden. Authenticated user does not have permission")
            }
    )
    public ResponseEntity<TravelDetailsDTO> createTravel(@Valid @RequestBody TravelCreateDTO travelCreateDTO) {
        TravelDetailsDTO createdTravel = travelService.createTravel(travelCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTravel);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Update an existing travel report",
            description = "Updates the purpose, dates, or destination of a travel report by its unique ID. Modifications " +
                    "are only allowed if the travel status is currently OPEN. Employees can only modify their own reports. " +
                    "Requires ADMIN, MANAGER, or EMPLOYEE role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Travel details successfully updated"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body, date constraints validation" +
                            " failed, or travel status is not OPEN"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "404", description = "Travel record not found")
            }
    )
    public ResponseEntity<TravelDetailsDTO> updateTravel(
            @PathVariable Long id,
            @Valid @RequestBody TravelUpdateDTO travelUpdateDTO) {
        TravelDetailsDTO updatedTravel = travelService.updateTravel(id, travelUpdateDTO);
        return ResponseEntity.ok(updatedTravel);
    }

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(
            summary = "Submit a travel report for approval",
            description = "Changes the status of a travel report from OPEN to SUBMITTED, locking it for further modifications. " +
                    "A report must contain at least one expense to be submitted. Requires ADMIN, MANAGER, or EMPLOYEE role.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Travel report successfully submitted for approval"),
                    @ApiResponse(responseCode = "400", description = "Travel report contains no expenses or is not in OPEN status"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "404", description = "Travel record not found")
            }
    )
    public ResponseEntity<Void> submitTravel(@PathVariable Long id) {
        travelService.submitTravelForApproval(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Approve a submitted travel report",
            description = "Approves a submitted travel report, changing its status to APPROVED. Managers can only approve " +
                    "reports belonging to employees of their same company, and are strictly blocked from self-approving " +
                    "their own travel requests. Requires ADMIN or MANAGER role.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Travel report successfully approved"),
                    @ApiResponse(responseCode = "400", description = "Travel report is not in SUBMITTED status, or manager " +
                            "attempted to approve their own report"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "404", description = "Travel record not found")
            }
    )
    public ResponseEntity<Void> approveTravel(@PathVariable Long id) {
        travelService.approveTravel(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Reject a submitted travel report",
            description = "Rejects a submitted travel report, returning its status back to OPEN so the user can make " +
                    "adjustments. Managers can only reject reports from employees within their same company tenant and " +
                    "cannot self-reject. Requires ADMIN or MANAGER role.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Travel report successfully rejected"),
                    @ApiResponse(responseCode = "400", description = "Travel report is not in SUBMITTED status, or manager " +
                            "attempted to reject their own report"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. Token is missing, invalid or expired"),
                    @ApiResponse(responseCode = "404", description = "Travel record not found")
            }
    )
    public ResponseEntity<Void> rejectTravel(@PathVariable Long id) {
        travelService.rejectTravel(id);
        return ResponseEntity.noContent().build();
    }
}