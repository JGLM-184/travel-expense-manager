package com.github.jglm_184.travel_expense_manager.util;

import com.github.jglm_184.travel_expense_manager.dto.TravelCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelUpdateDTO;
import com.github.jglm_184.travel_expense_manager.model.Travel;
import com.github.jglm_184.travel_expense_manager.model.enums.TravelStatus;

import java.time.LocalDate;
import java.util.ArrayList;

public class TravelCreator {

    private static final LocalDate START_DATE = LocalDate.now().plusDays(5);
    private static final LocalDate END_DATE = LocalDate.now().plusDays(10);

    public static Travel createValidTravelInOpenStatus() {
        return Travel.builder()
                .id(1L)
                .purpose("Tech Conference 2026")
                .destination(AddressCreator.createValidAddress())
                .startDate(START_DATE)
                .endDate(END_DATE)
                .status(TravelStatus.OPEN)
                .company(CompanyCreator.createValidActiveCompany())
                .user(UserCreator.createValidActiveUser())
                .expenses(new ArrayList<>())
                .build();
    }

    public static Travel createValidTravelInSubmittedStatus() {
        return Travel.builder()
                .id(1L)
                .purpose("Tech Conference 2026")
                .destination(AddressCreator.createValidAddress())
                .startDate(START_DATE)
                .endDate(END_DATE)
                .status(TravelStatus.SUBMITTED)
                .company(CompanyCreator.createValidActiveCompany())
                .user(UserCreator.createValidActiveUser())
                .expenses(new ArrayList<>())
                .build();
    }

    public static Travel createValidTravelInApprovedStatus() {
        return Travel.builder()
                .id(1L)
                .purpose("Tech Conference 2026")
                .destination(AddressCreator.createValidAddress())
                .startDate(START_DATE)
                .endDate(END_DATE)
                .status(TravelStatus.APPROVED)
                .company(CompanyCreator.createValidActiveCompany())
                .user(UserCreator.createValidActiveUser())
                .expenses(new ArrayList<>())
                .build();
    }

    public static Travel createValidTravelInRejectedStatus() {
        return Travel.builder()
                .id(1L)
                .purpose("Tech Conference 2026")
                .destination(AddressCreator.createValidAddress())
                .startDate(START_DATE)
                .endDate(END_DATE)
                .status(TravelStatus.REJECTED)
                .company(CompanyCreator.createValidActiveCompany())
                .user(UserCreator.createValidActiveUser())
                .expenses(new ArrayList<>())
                .build();
    }

    public static Travel createValidTravelToBeSaved() {
        return Travel.builder()
                .purpose("Tech Conference 2026")
                .destination(AddressCreator.createValidAddress())
                .startDate(START_DATE)
                .endDate(END_DATE)
                .status(TravelStatus.OPEN)
                .company(CompanyCreator.createValidActiveCompany())
                .user(UserCreator.createValidActiveUser())
                .expenses(new ArrayList<>())
                .build();
    }

    public static TravelCreateDTO createValidTravelCreateDTO() {
        return TravelCreateDTO.builder()
                .purpose("Tech Conference 2026")
                .destination(AddressCreator.createValidAddressCreateDTO())
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();
    }

    public static TravelUpdateDTO createValidTravelUpdateDTO() {
        return TravelUpdateDTO.builder()
                .purpose("Tech Conference 2026 Updated")
                .destination(AddressCreator.createValidAddressCreateDTO())
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();
    }

    public static TravelDetailsDTO createValidTravelDetailsDTO() {
        return TravelDetailsDTO.builder()
                .id(1L)
                .purpose("Tech Conference 2026")
                .destination(AddressCreator.createAddressDetailsDTO())
                .startDate(START_DATE)
                .endDate(END_DATE)
                .status(TravelStatus.OPEN)
                .userId(1L)
                .userName("John Doe")
                .expenses(new ArrayList<>())
                .build();
    }

    public static Travel createAnotherValidTravelToBeSaved() {
        return Travel.builder()
                .purpose("Technical Training")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(15))
                .status(TravelStatus.OPEN)
                .build();
    }

}