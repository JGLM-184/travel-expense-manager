package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.CompanyCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyUpdateDTO;
import com.github.jglm_184.travel_expense_manager.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/active")
    public ResponseEntity<Page<CompanyDetailsDTO>> findAllActiveCompanies(@ParameterObject Pageable pageable) {
        return new ResponseEntity<>(companyService.findAllActiveCompanies(pageable), HttpStatus.OK);
    }

    @GetMapping("/inactive")
    public ResponseEntity<Page<CompanyDetailsDTO>> findAllInactiveCompanies(@ParameterObject Pageable pageable) {
        return new ResponseEntity<>(companyService.findAllInactiveCompanies(pageable), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CompanyDetailsDTO> createCompany (@Valid @RequestBody CompanyCreateDTO companyCreateDTO) {
        return new ResponseEntity<>(companyService.createCompany(companyCreateDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDetailsDTO> updateCompany (@PathVariable Long id,
                                                            @Valid @RequestBody CompanyUpdateDTO companyUpdateDTO) {
        return new ResponseEntity<>(companyService.updateCompany(id, companyUpdateDTO), HttpStatus.OK);
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<Void> deactivateCompany (@PathVariable Long id) {
        companyService.activateCompany(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<Void> activateCompany (@PathVariable Long id) {
        companyService.deactivateCompany(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
