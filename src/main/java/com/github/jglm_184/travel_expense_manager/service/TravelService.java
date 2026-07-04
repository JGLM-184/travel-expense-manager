package com.github.jglm_184.travel_expense_manager.service;

import com.github.jglm_184.travel_expense_manager.dto.TravelCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelUpdateDTO;
import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.exception.ResourceNotFoundException;
import com.github.jglm_184.travel_expense_manager.mapper.TravelMapper;
import com.github.jglm_184.travel_expense_manager.model.Address;
import com.github.jglm_184.travel_expense_manager.model.Travel;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.model.enums.TravelStatus;
import com.github.jglm_184.travel_expense_manager.repository.TravelRepository;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TravelService {

    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final TravelMapper travelMapper;
    private final AddressService addressService;

    public Page<TravelDetailsDTO> findAllTravels(Pageable pageable) {
        User currentUser = getAuthenticatedUser();
        Page<Travel> travelPage;

        if (currentUser.getRole().name().equals("ROLE_ADMIN")) {
            travelPage = travelRepository.findAll(pageable);
        } else if (currentUser.getRole().name().equals("ROLE_MANAGER")) {
            travelPage = travelRepository.findByCompanyId(currentUser.getCompany().getId(), pageable);
        } else {
            travelPage = travelRepository.findByUserId(currentUser.getId(), pageable);
        }

        return travelPage.map(travelMapper::toDto);
    }

    @Transactional
    public TravelDetailsDTO createTravel(TravelCreateDTO travelCreateDTO) {
        validateTravelDates(travelCreateDTO.getStartDate(), travelCreateDTO.getEndDate());

        User currentUser = getAuthenticatedUser();

        Travel travelToBeSaved = travelMapper.toTravel(travelCreateDTO);
        travelToBeSaved.setStatus(TravelStatus.OPEN);
        travelToBeSaved.setUser(currentUser);
        travelToBeSaved.setCompany(currentUser.getCompany());

        Address destination = addressService.getOrCreateAddress(travelCreateDTO.getDestination());
        travelToBeSaved.setDestination(destination);

        Travel savedTravel = travelRepository.save(travelToBeSaved);
        return travelMapper.toDto(savedTravel);
    }

    @Transactional
    public TravelDetailsDTO updateTravel(Long id, TravelUpdateDTO travelUpdateDTO) {
        validateTravelDates(travelUpdateDTO.getStartDate(), travelUpdateDTO.getEndDate());

        Travel travelToBeUpdated = findTravelByIdAndVerifyAccess(id);

        if (travelToBeUpdated.getStatus() != TravelStatus.OPEN) {
            throw new BusinessException("Cannot update a travel report that is not in OPEN status");
        }

        travelToBeUpdated.setPurpose(travelUpdateDTO.getPurpose());
        travelToBeUpdated.setStartDate(travelUpdateDTO.getStartDate());
        travelToBeUpdated.setEndDate(travelUpdateDTO.getEndDate());

        Address destination = addressService.getOrCreateAddress(travelUpdateDTO.getDestination());
        travelToBeUpdated.setDestination(destination);

        Travel updatedTravel = travelRepository.save(travelToBeUpdated);
        return travelMapper.toDto(updatedTravel);
    }

    @Transactional
    public void submitTravelForApproval(Long id) {
        Travel travel = findTravelByIdAndVerifyAccess(id);

        if (travel.getStatus() != TravelStatus.OPEN) {
            throw new BusinessException("Only travel reports in OPEN status can be submitted");
        }

        if (travel.getExpenses().isEmpty()) {
            throw new BusinessException("Cannot submit a travel report without any expenses");
        }

        travel.setStatus(TravelStatus.SUBMITTED);
        travelRepository.save(travel);
    }

    @Transactional
    public void approveTravel(Long id) {
        Travel travel = findTravelByIdAndVerifyAccess(id);
        User currentUser = getAuthenticatedUser();

        if (travel.getStatus() != TravelStatus.SUBMITTED) {
            throw new BusinessException("Only SUBMITTED travel reports can be approved");
        }

        // Regra de negócio: Impossibilita auto-aprovação de MANAGER
        if (travel.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Managers cannot approve their own travel expense reports");
        }

        travel.setStatus(TravelStatus.APPROVED);
        travelRepository.save(travel);
    }

    @Transactional
    public void rejectTravel(Long id) {
        Travel travel = findTravelByIdAndVerifyAccess(id);
        User currentUser = getAuthenticatedUser();

        if (travel.getStatus() != TravelStatus.SUBMITTED) {
            throw new BusinessException("Only SUBMITTED travel reports can be rejected");
        }

        if (travel.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Managers cannot reject their own travel expense reports");
        }

        travel.setStatus(TravelStatus.REJECTED);
        travelRepository.save(travel);
    }

    private void validateTravelDates(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new BusinessException("The end date cannot be before the start date");
        }
    }

    private Travel findTravelByIdAndVerifyAccess(Long id) {
        Travel travel = travelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Travel report not found"));

        User currentUser = getAuthenticatedUser();

        if (currentUser.getRole().name().equals("ROLE_EMPLOYEE") && !travel.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Travel report not found");
        }

        if (currentUser.getRole().name().equals("ROLE_MANAGER") && !travel.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new ResourceNotFoundException("Travel report not found");
        }

        return travel;
    }

    private User getAuthenticatedUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(Long.parseLong(userIdStr))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user metadata not found"));
    }
}