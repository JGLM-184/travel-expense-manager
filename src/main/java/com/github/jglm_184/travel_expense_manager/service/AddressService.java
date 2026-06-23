package com.github.jglm_184.travel_expense_manager.service;

import com.github.jglm_184.travel_expense_manager.client.ViaCepClient;
import com.github.jglm_184.travel_expense_manager.dto.AddressCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.ViaCepResponse;
import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.exception.ResourceNotFoundException;
import com.github.jglm_184.travel_expense_manager.mapper.AddressMapper;
import com.github.jglm_184.travel_expense_manager.model.Address;
import com.github.jglm_184.travel_expense_manager.repository.AddressRepository;
import com.github.jglm_184.travel_expense_manager.util.FormatterUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final ViaCepClient viaCepClient;
    private final FormatterUtil formatterUtil;

    @Transactional
    public Address getOrCreateAddress(AddressCreateDTO addressCreateDTO) {
        String zipCode = formatterUtil.cleanNumbers(addressCreateDTO.getZipCode());

        return addressRepository.findByZipCode(zipCode)
                .orElseGet(() -> {
                    Address address;

                    if (addressCreateDTO.isFullyFilled()) {
                        address = addressMapper.toAddress(addressCreateDTO);
                    } else {
                        address = fetchFromViaCep(zipCode);
                    }

                    address.setZipCode(zipCode);

                    return addressRepository.save(address);
                });
    }

    private Address fetchFromViaCep(String zipCode) {
        try {
            ViaCepResponse response = viaCepClient.findAddress(zipCode);

            if (response == null || response.isError()) {
                throw new ResourceNotFoundException("The provided zipCode does not exist.");
            }

            return addressMapper.toAddress(response);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            throw new BusinessException("Invalid zipCode format for external lookup.");
        }
    }
}
