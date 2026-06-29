package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.CompanyCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyUpdateDTO;
import com.github.jglm_184.travel_expense_manager.service.CompanyService;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/companies")
@Tag(name = "Company", description = "Endpoints for managing companies, their active status, " +
        "and corporate headquarters")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all active companies paginated",
            description = "Retrieves a paginated list of companies that are currently active in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list" +
                            " of active companies"),
                    @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting " +
                            "parameters provided")
            }
    )
    public ResponseEntity<Page<CompanyDetailsDTO>> findAllActiveCompanies(@ParameterObject Pageable pageable) {
        return new ResponseEntity<>(companyService.findAllActiveCompanies(pageable), HttpStatus.OK);
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all inactive companies paginated",
            description = "Retrieves a paginated list of companies that are currently inactive in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list " +
                            "of inactive companies"),
                    @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameters " +
                            "provided")
            }
    )
    public ResponseEntity<Page<CompanyDetailsDTO>> findAllInactiveCompanies(@ParameterObject Pageable pageable) {
        return new ResponseEntity<>(companyService.findAllInactiveCompanies(pageable), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new company",
            description = "Registers a new company. If only the CNPJ is provided, the system will " +
                    "automatically fetch company data from ReceitaWS. If the address details are missing, " +
                    "it will lookup the address via ViaCEP.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Company successfully created"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload, company CNPJ " +
                            "already exists or is invalid, or zip code from head quarters is invalid/not found")
            }
    )
    public ResponseEntity<CompanyDetailsDTO> createCompany(@Valid @RequestBody CompanyCreateDTO companyCreateDTO) {
        return new ResponseEntity<>(companyService.createCompany(companyCreateDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update an active company",
            description = "Updates the registration details (such as company name, trade name, " +
                    "or headquarters address) of an active company by its unique ID. If a new zip " +
                    "code is provided, it will be validated and processed.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Company details successfully updated"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed " +
                            "(e.g., invalid zip code format)"),
                    @ApiResponse(responseCode = "404", description = "Company not found or is currently inactive")
            }
    )
    public ResponseEntity<CompanyDetailsDTO> updateCompany(@PathVariable Long id,
                                                           @Valid @RequestBody CompanyUpdateDTO companyUpdateDTO) {
        return new ResponseEntity<>(companyService.updateCompany(id, companyUpdateDTO), HttpStatus.OK);
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Activate an inactive company by ID",
            description = "Changes the status of a specific company to active, allowing it to be used across " +
                    "the system again.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Company successfully activated"),
                    @ApiResponse(responseCode = "404", description = "Company not found or is already active")
            }
    )
    public ResponseEntity<Void> activateCompany(@PathVariable Long id) {
        companyService.activateCompany(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deactivate an active company by ID",
            description = "Changes the status of a specific company to inactive. Deactivated companies will no " +
                    "longer appear in the active listings and cannot be used in the system.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Company successfully deactivated"),
                    @ApiResponse(responseCode = "404", description = "Company not found or is already inactive")
            }
    )
    public ResponseEntity<Void> deactivateCompany(@PathVariable Long id) {
        companyService.deactivateCompany(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
