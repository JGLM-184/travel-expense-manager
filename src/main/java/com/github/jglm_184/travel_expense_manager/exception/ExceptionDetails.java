package com.github.jglm_184.travel_expense_manager.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExceptionDetails {
    private String title;
    private int status;
    private String details;
    private String timestamp;
    private String fields;
    private String fieldsMessage;
}
