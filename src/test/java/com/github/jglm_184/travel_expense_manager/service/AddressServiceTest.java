package com.github.jglm_184.travel_expense_manager.service;

import com.github.jglm_184.travel_expense_manager.client.ViaCepClient;
import com.github.jglm_184.travel_expense_manager.dto.AddressCreateDTO;
import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.exception.ResourceNotFoundException;
import com.github.jglm_184.travel_expense_manager.mapper.AddressMapper;
import com.github.jglm_184.travel_expense_manager.model.Address;
import com.github.jglm_184.travel_expense_manager.repository.AddressRepository;
import com.github.jglm_184.travel_expense_manager.util.AddressCreator;
import com.github.jglm_184.travel_expense_manager.util.FormatterUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for AddressService")
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private AddressMapper addressMapper;
    @Mock
    private ViaCepClient viaCepClient;
    @Mock
    private FormatterUtil formatterUtil;

    @InjectMocks
    private AddressService addressServiceMock;

    @BeforeEach
    void setUp() {
        BDDMockito.when(addressRepository.findByZipCode(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(AddressCreator.createValidAddress()));

        BDDMockito.when(addressRepository.save(ArgumentMatchers.any(Address.class)))
                .thenReturn(AddressCreator.createValidAddress());

        BDDMockito.when(viaCepClient.findAddress(ArgumentMatchers.anyString()))
                .thenReturn(AddressCreator.createViaCepResponse());

        BDDMockito.when(addressMapper.toAddress(ArgumentMatchers.any(AddressCreateDTO.class)))
                .thenReturn(AddressCreator.createValidAddress());

        BDDMockito.when(formatterUtil.cleanNumbers(ArgumentMatchers.anyString()))
                .thenReturn("01001000");
    }

    @Test
    @DisplayName("getOrCreateAddress returns a registered address when the provided ZIP code " +
            "is already registered in the database when successful")
    void getOrCreateAddress_ReturnsRegisteredAdddress_WhenSuccessful() {
        Address validAddress = AddressCreator.createValidAddress();
        Long expectedId = validAddress.getId();
        String expectedZipCode = validAddress.getZipCode();

        AddressCreateDTO dto = AddressCreator.createAddressCreateDTO();
        Address addressForAssertion = addressServiceMock.getOrCreateAddress(dto);

        Assertions.assertThat(addressForAssertion).isNotNull();
        Assertions.assertThat(addressForAssertion.getId()).isEqualTo(expectedId);
        Assertions.assertThat(addressForAssertion.getZipCode()).isEqualTo(expectedZipCode);

        BDDMockito.verify(addressRepository, BDDMockito.never()).save(ArgumentMatchers.any(Address.class));
        BDDMockito.verifyNoInteractions(viaCepClient);
    }

    @Test
    @DisplayName("getOrCreateAddress save a new address and returns when the provided ZIP code " +
            "is not registered in the database when successful")
    void getOrCreateAddress_SaveAndReturnsdAdddress_WhenSuccessful() {
        BDDMockito.when(addressRepository.findByZipCode(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        AddressCreateDTO dto = AddressCreator.createAddressCreateDTO();
        Address addressForAssertion = addressServiceMock.getOrCreateAddress(dto);

        Assertions.assertThat(addressForAssertion)
                .isNotNull()
                .isEqualTo(AddressCreator.createValidAddress());

        BDDMockito.then(addressRepository).should().save(ArgumentMatchers.any(Address.class));
    }

    @Test
    @DisplayName("getOrCreateAddress returns a resource not found exception when successful")
    void getOrCreateAddress_ResourceNotFoundException_WhenSuccessful() {
        BDDMockito.when(addressRepository.findByZipCode(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        BDDMockito.when(viaCepClient.findAddress(ArgumentMatchers.anyString()))
                .thenReturn(AddressCreator.createViaCepResponseWithError());

        AddressCreateDTO dto = AddressCreator.createAddressCreateDTONotFullyFilled();
        Throwable thrown = Assertions.catchThrowable(() -> addressServiceMock.getOrCreateAddress(dto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("The provided zipCode does not exist.");

        BDDMockito.then(addressRepository).should(BDDMockito.never()).save(ArgumentMatchers.any(Address.class));
    }

    @Test
    @DisplayName("getOrCreateAddress returns a business exception when zip code format is invalid")
    void getOrCreateAddress_ReturnsBusinessException_WhenZipCodeFormatIsInvalid() {
        BDDMockito.when(addressRepository.findByZipCode(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        BDDMockito.when(viaCepClient.findAddress(ArgumentMatchers.anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        AddressCreateDTO dto = AddressCreator.createInvalidAddressCreateDTO();

        Throwable thrown = Assertions.catchThrowable(() -> addressServiceMock.getOrCreateAddress(dto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid zipCode format for external lookup.");

        BDDMockito.then(addressRepository).should(BDDMockito.never()).save(ArgumentMatchers.any(Address.class));
    }
}