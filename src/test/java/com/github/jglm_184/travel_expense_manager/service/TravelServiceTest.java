package com.github.jglm_184.travel_expense_manager.service;

import com.github.jglm_184.travel_expense_manager.dto.TravelCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelUpdateDTO;
import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.exception.ResourceNotFoundException;
import com.github.jglm_184.travel_expense_manager.mapper.TravelMapper;
import com.github.jglm_184.travel_expense_manager.model.Travel;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import com.github.jglm_184.travel_expense_manager.model.enums.TravelStatus;
import com.github.jglm_184.travel_expense_manager.repository.TravelRepository;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import com.github.jglm_184.travel_expense_manager.util.AddressCreator;
import com.github.jglm_184.travel_expense_manager.util.TravelCreator;
import com.github.jglm_184.travel_expense_manager.util.UserCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for TravelService")
class TravelServiceTest {

    @Mock
    private TravelRepository travelRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TravelMapper travelMapper;
    @Mock
    private AddressService addressService;

    @InjectMocks
    private TravelService travelService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = UserCreator.createValidActiveUser();
        currentUser.setRole(Role.ROLE_EMPLOYEE);

        Authentication authentication = BDDMockito.mock(Authentication.class);
        BDDMockito.when(authentication.getName()).thenReturn(currentUser.getId().toString());

        SecurityContext securityContext = BDDMockito.mock(SecurityContext.class);
        BDDMockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        BDDMockito.when(userRepository.findById(currentUser.getId()))
                .thenReturn(Optional.of(currentUser));
    }

    private void changeCurrentUserRole(Role role, Long id) {
        currentUser.setRole(role);
        currentUser.setId(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        BDDMockito.when(authentication.getName()).thenReturn(id.toString());
        BDDMockito.when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));
    }

    @Test
    @DisplayName("Returns page of travels for employee when user role is EMPLOYEE")
    void findAllTravels_ReturnsPageOfEmployeeTravels_WhenUserRoleIsEmployee() {
        PageImpl<Travel> travelPage = new PageImpl<>(List.of(TravelCreator.createValidTravelInOpenStatus()));
        BDDMockito.when(travelRepository.findByUserId(ArgumentMatchers.eq(currentUser.getId()), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(travelPage);
        BDDMockito.when(travelMapper.toDto(ArgumentMatchers.any(Travel.class)))
                .thenReturn(TravelCreator.createValidTravelDetailsDTO());

        Page<TravelDetailsDTO> result = travelService.findAllTravels(PageRequest.of(0, 1));

        Assertions.assertThat(result).isNotNull().isNotEmpty();
        BDDMockito.then(travelRepository).should().findByUserId(ArgumentMatchers.anyLong(), ArgumentMatchers.any(Pageable.class));
    }

    @Test
    @DisplayName("Returns page of company travels for manager when user role is MANAGER")
    void findAllTravels_ReturnsPageOfCompanyTravels_WhenUserRoleIsManager() {
        changeCurrentUserRole(Role.ROLE_MANAGER, 2L);
        PageImpl<Travel> travelPage = new PageImpl<>(List.of(TravelCreator.createValidTravelInOpenStatus()));

        BDDMockito.when(travelRepository.findByCompanyId(ArgumentMatchers.eq(currentUser.getCompany().getId()), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(travelPage);
        BDDMockito.when(travelMapper.toDto(ArgumentMatchers.any(Travel.class)))
                .thenReturn(TravelCreator.createValidTravelDetailsDTO());

        Page<TravelDetailsDTO> result = travelService.findAllTravels(PageRequest.of(0, 1));

        Assertions.assertThat(result).isNotNull();
        BDDMockito.then(travelRepository).should().findByCompanyId(ArgumentMatchers.anyLong(), ArgumentMatchers.any(Pageable.class));
    }

    @Test
    @DisplayName("Saves and returns travel details when DTO is fully filled")
    void createTravel_SavesAndReturnsTravelDetails_WhenDTOIsFullyFilled() {
        TravelCreateDTO travelCreateDTO = TravelCreator.createValidTravelCreateDTO();
        Travel travelSaved = TravelCreator.createValidTravelInOpenStatus();
        TravelDetailsDTO expectedResponse = TravelCreator.createValidTravelDetailsDTO();

        BDDMockito.when(travelMapper.toTravel(ArgumentMatchers.any(TravelCreateDTO.class)))
                .thenReturn(travelSaved);
        BDDMockito.when(addressService.getOrCreateAddress(ArgumentMatchers.any()))
                .thenReturn(AddressCreator.createValidAddress());
        BDDMockito.when(travelRepository.save(ArgumentMatchers.any(Travel.class)))
                .thenReturn(travelSaved);
        BDDMockito.when(travelMapper.toDto(ArgumentMatchers.any(Travel.class)))
                .thenReturn(expectedResponse);

        TravelDetailsDTO actualResponse = travelService.createTravel(travelCreateDTO);

        Assertions.assertThat(actualResponse).isNotNull();
        Assertions.assertThat(actualResponse.getStatus()).isEqualTo(TravelStatus.OPEN);
        BDDMockito.then(travelRepository).should().save(ArgumentMatchers.any(Travel.class));
    }

    @Test
    @DisplayName("Throws BusinessException when end date is before start date")
    void createTravel_ThrowsBusinessException_WhenEndDateIsBeforeStartDate() {
        TravelCreateDTO dto = TravelCreator.createValidTravelCreateDTO();
        dto.setStartDate(LocalDate.of(2026, 8, 20));
        dto.setEndDate(LocalDate.of(2026, 8, 15));

        Throwable thrown = Assertions.catchThrowable(() -> travelService.createTravel(dto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("The end date cannot be before the start date");
        BDDMockito.verify(travelRepository, BDDMockito.never()).save(ArgumentMatchers.any(Travel.class));
    }

    @Test
    @DisplayName("Updates and returns travel details when travel exists and status is OPEN")
    void updateTravel_UpdatesAndReturnsTravelDetails_WhenTravelExistsAndStatusIsOpen() {
        TravelUpdateDTO updateDto = TravelCreator.createValidTravelUpdateDTO();
        Travel existingTravel = TravelCreator.createValidTravelInOpenStatus();
        existingTravel.getUser().setId(currentUser.getId());
        TravelDetailsDTO expectedResponse = TravelCreator.createValidTravelDetailsDTO();

        BDDMockito.when(travelRepository.findById(1L)).thenReturn(Optional.of(existingTravel));
        BDDMockito.when(addressService.getOrCreateAddress(ArgumentMatchers.any())).thenReturn(AddressCreator.createValidAddress());
        BDDMockito.when(travelRepository.save(ArgumentMatchers.any(Travel.class))).thenReturn(existingTravel);
        BDDMockito.when(travelMapper.toDto(ArgumentMatchers.any(Travel.class))).thenReturn(expectedResponse);

        TravelDetailsDTO actualResponse = travelService.updateTravel(1L, updateDto);

        Assertions.assertThat(actualResponse).isNotNull();
        BDDMockito.then(travelRepository).should().save(ArgumentMatchers.any(Travel.class));
    }

    @Test
    @DisplayName("Throws BusinessException when travel report status is not OPEN on update")
    void updateTravel_ThrowsBusinessException_WhenTravelStatusIsNotOpen() {
        TravelUpdateDTO updateDto = TravelCreator.createValidTravelUpdateDTO();
        Travel existingSubmittedTravel = TravelCreator.createValidTravelInSubmittedStatus();
        existingSubmittedTravel.getUser().setId(currentUser.getId());

        BDDMockito.when(travelRepository.findById(1L)).thenReturn(Optional.of(existingSubmittedTravel));

        Throwable thrown = Assertions.catchThrowable(() -> travelService.updateTravel(1L, updateDto));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot update a travel report that is not in OPEN status");
    }

    @Test
    @DisplayName("Throws BusinessException when submitting travel report without expenses")
    void submitTravelForApproval_ThrowsBusinessException_WhenReportHasNoExpenses() {
        Travel existingTravel = TravelCreator.createValidTravelInOpenStatus();
        existingTravel.getUser().setId(currentUser.getId());
        existingTravel.getExpenses().clear();

        BDDMockito.when(travelRepository.findById(1L)).thenReturn(Optional.of(existingTravel));

        Throwable thrown = Assertions.catchThrowable(() -> travelService.submitTravelForApproval(1L));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot submit a travel report without any expenses");
    }

    @Test
    @DisplayName("Approves travel report successfully when user is manager and report belongs to another employee")
    void approveTravel_ApprovesReport_WhenUserIsManagerAndNotOwner() {
        changeCurrentUserRole(Role.ROLE_MANAGER, 5L);
        Travel submittedTravel = TravelCreator.createValidTravelInSubmittedStatus();
        submittedTravel.getUser().setId(1L);
        submittedTravel.getCompany().setId(currentUser.getCompany().getId());

        BDDMockito.when(travelRepository.findById(1L)).thenReturn(Optional.of(submittedTravel));

        travelService.approveTravel(1L);

        Assertions.assertThat(submittedTravel.getStatus()).isEqualTo(TravelStatus.APPROVED);
        BDDMockito.then(travelRepository).should().save(submittedTravel);
    }

    @Test
    @DisplayName("Throws BusinessException when manager tries to approve their own travel report")
    void approveTravel_ThrowsBusinessException_WhenManagerIsOwner() {
        changeCurrentUserRole(Role.ROLE_MANAGER, 1L);
        Travel submittedTravel = TravelCreator.createValidTravelInSubmittedStatus();
        submittedTravel.getUser().setId(1L);

        BDDMockito.when(travelRepository.findById(1L)).thenReturn(Optional.of(submittedTravel));

        Throwable thrown = Assertions.catchThrowable(() -> travelService.approveTravel(1L));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Managers cannot approve their own travel expense reports");
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when employee tries to access travel report of another user")
    void findTravelById_ThrowsResourceNotFoundException_WhenEmployeeAccessesOtherUserReport() {
        Travel otherUserTravel = TravelCreator.createValidTravelInOpenStatus();
        otherUserTravel.getUser().setId(99L);

        BDDMockito.when(travelRepository.findById(1L)).thenReturn(Optional.of(otherUserTravel));

        Throwable thrown = Assertions.catchThrowable(() -> travelService.approveTravel(1L));

        Assertions.assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Travel report not found");
    }
}