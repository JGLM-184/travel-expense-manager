package com.github.jglm_184.travel_expense_manager.service;

import com.github.jglm_184.travel_expense_manager.client.ReceitaWSClient;
import com.github.jglm_184.travel_expense_manager.dto.CompanyCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.CompanyUpdateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ReceitaWSResponse;
import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.exception.ResourceNotFoundException;
import com.github.jglm_184.travel_expense_manager.mapper.CompanyMapper;
import com.github.jglm_184.travel_expense_manager.model.Address;
import com.github.jglm_184.travel_expense_manager.model.Company;
import com.github.jglm_184.travel_expense_manager.repository.CompanyRepository;
import com.github.jglm_184.travel_expense_manager.util.FormatterUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final ReceitaWSClient receitaWSClient;
    private final AddressService addressService;
    private final FormatterUtil formatterUtil;


    public Page<CompanyDetailsDTO> findAllActiveCompanies(Pageable pageable) {
        Page<Company> companyPage = companyRepository.findByActiveTrue(pageable);
        return companyPage.map(companyMapper::toDto);
    }

    public Page<CompanyDetailsDTO> findAllInactiveCompanies(Pageable pageable) {
        Page<Company> companyPage = companyRepository.findByActiveFalse(pageable);
        return companyPage.map(companyMapper::toDto);
    }

    @Transactional
    public CompanyDetailsDTO createCompany(CompanyCreateDTO companyCreateDTO) {
        String cnpj = formatterUtil.cleanNumbers(companyCreateDTO.getCnpj());

        if (companyRepository.findByCnpj(cnpj).isPresent()) {
            throw new BusinessException("Company with this CNPJ already exists");
        }

        Company companyToBeSaved;

        if (companyCreateDTO.isFullyFilled()) {
            companyToBeSaved = companyMapper.toCompany(companyCreateDTO);
        } else {
            companyToBeSaved = fetchFromReceitaWs(cnpj);
        }

        companyToBeSaved.setCnpj(cnpj);
        companyToBeSaved.setActive(true);

        Address headquarters = addressService.getOrCreateAddress(companyCreateDTO.getHeadquarters());
        companyToBeSaved.setHeadquarters(headquarters);

        Company savedCompany = companyRepository.save(companyToBeSaved);

        return companyMapper.toDto(savedCompany);
    }

    @Transactional
    public CompanyDetailsDTO updateCompany(Long id, CompanyUpdateDTO companyUpdateDTO) {

        Company companyToBeUpdate = findCompanyActiveById(id);

        String newCompanyName = companyUpdateDTO.getCompanyName();
        String newTradeName = companyUpdateDTO.getTradeName();

        if(newCompanyName != null && !newCompanyName.isBlank()) {
            companyToBeUpdate.setCompanyName(newCompanyName);
        }

        if(newTradeName != null && !newTradeName.isBlank()) {
            companyToBeUpdate.setTradeName(newTradeName);
        }

        Address headquarters = addressService.getOrCreateAddress(companyUpdateDTO.getHeadquarters());
        companyToBeUpdate.setHeadquarters(headquarters);

        Company companyUpdated = companyRepository.save(companyToBeUpdate);

        return companyMapper.toDto(companyUpdated);
    }

    public void deactivateCompany(Long id) {
        Company company = findCompanyActiveById(id);
        company.setActive(false);
        companyRepository.save(company);
    }

    public void activateCompany(Long id) {
        Company company = findCompanyInactiveById(id);
        company.setActive(true);
        companyRepository.save(company);
    }

    private Company findCompanyActiveById(Long id) {
        return companyRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found or is inactive"));
    }

    private Company findCompanyInactiveById(Long id) {
        return companyRepository.findByIdAndActiveFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found or is active"));
    }

    private Company fetchFromReceitaWs(String cnpj) {
        try {
            ReceitaWSResponse response = receitaWSClient.findCnpj(cnpj);

            if (response == null || "ERROR".equals(response.getStatus())) {
                throw new BusinessException("CNPJ Not Found or Invalid on Federal Revenue database.");
            }

            return companyMapper.toCompany(response);
        } catch (HttpClientErrorException e) {
            throw new BusinessException("External API error: Unable to retrieve company data for the provided CNPJ.");
        }
    }
}
