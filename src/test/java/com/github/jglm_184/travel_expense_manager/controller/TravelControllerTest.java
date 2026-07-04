package com.github.jglm_184.travel_expense_manager.controller;

import com.github.jglm_184.travel_expense_manager.dto.TravelCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.TravelUpdateDTO;
import com.github.jglm_184.travel_expense_manager.service.TravelService;
import com.github.jglm_184.travel_expense_manager.util.TravelCreator;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for TravelController")
class TravelControllerTest {

    @Mock
    private TravelService travelService;

    @InjectMocks
    private TravelController travelController;

    @BeforeEach
    void setUp() {
        PageImpl<TravelDetailsDTO> travelPage = new PageImpl<>(
                List.of(TravelCreator.createValidTravelDetailsDTO()));

        BDDMockito.when(travelService.findAllTravels(ArgumentMatchers.any()))
                .thenReturn(travelPage);

        BDDMockito.when(travelService.createTravel(ArgumentMatchers.any(TravelCreateDTO.class)))
                .thenReturn(TravelCreator.createValidTravelDetailsDTO());

        BDDMockito.when(travelService.updateTravel(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TravelUpdateDTO.class)))
                .thenReturn(TravelCreator.createValidTravelDetailsDTO());

        BDDMockito.doNothing()
                .when(travelService)
                .submitTravelForApproval(ArgumentMatchers.anyLong());

        BDDMockito.doNothing()
                .when(travelService)
                .approveTravel(ArgumentMatchers.anyLong());

        BDDMockito.doNothing()
                .when(travelService)
                .rejectTravel(ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("Returns a page of travels when travels exist")
    void getAllTravels_ReturnsPageOfTravelDetails_WhenTravelsExist() {
        String expectedPurpose = TravelCreator.createValidTravelDetailsDTO().getPurpose();

        ResponseEntity<Page<TravelDetailsDTO>> responseEntity =
                travelController.getAllTravels(null);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        Assertions.assertThat(responseEntity.getBody().toList()).isNotEmpty().hasSize(1);

        Assertions.assertThat(responseEntity.getBody().toList().get(0).getPurpose())
                .isEqualTo(expectedPurpose);
    }

    @Test
    @DisplayName("Saves and returns travel details when travel data is valid")
    void createTravel_SavesAndReturnsTravelDetails_WhenTravelDataIsValid() {
        TravelCreateDTO inputDto = TravelCreator.createValidTravelCreateDTO();
        TravelDetailsDTO expectedDetails = TravelCreator.createValidTravelDetailsDTO();

        ResponseEntity<TravelDetailsDTO> responseEntity = travelController.createTravel(inputDto);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isNotNull().isEqualTo(expectedDetails);
    }

    @Test
    @DisplayName("Updates and returns travel details when travel data is valid")
    void updateTravel_UpdatesAndReturnsTravelDetails_WhenTravelDataIsValid() {
        TravelUpdateDTO updateDto = TravelCreator.createValidTravelUpdateDTO();
        TravelDetailsDTO expectedDetails = TravelCreator.createValidTravelDetailsDTO();

        ResponseEntity<TravelDetailsDTO> responseEntity = travelController.updateTravel(1L, updateDto);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull().isEqualTo(expectedDetails);
    }

    @Test
    @DisplayName("Submits travel report when travel ID is valid")
    void submitTravel_SubmitsTravelReport_WhenTravelExists() {
        ResponseEntity<Void> responseEntity = travelController.submitTravel(1L);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Approves travel report when travel ID is valid")
    void approveTravel_ApprovesTravelReport_WhenTravelExists() {
        ResponseEntity<Void> responseEntity = travelController.approveTravel(1L);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Rejects travel report when travel ID is valid")
    void rejectTravel_RejectsTravelReport_WhenTravelExists() {
        ResponseEntity<Void> responseEntity = travelController.rejectTravel(1L);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}