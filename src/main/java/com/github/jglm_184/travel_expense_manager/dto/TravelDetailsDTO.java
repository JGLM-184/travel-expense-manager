package com.github.jglm_184.travel_expense_manager.dto;

import com.github.jglm_184.travel_expense_manager.model.enums.TravelStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TravelDetailsDTO {

    private Long id;
    private String purpose;
    private AddressDetailsDTO destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private TravelStatus status;

    private Long userId;
    private String userName;

    private List<ExpenseDetailsDTO> expenses;
}