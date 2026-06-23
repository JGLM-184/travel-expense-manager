package com.github.jglm_184.travel_expense_manager.handler;

import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.exception.ExceptionDetails;
import com.github.jglm_184.travel_expense_manager.exception.ResourceNotFoundException;
import com.github.jglm_184.travel_expense_manager.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    private final DateUtil dateUtil;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionDetails> handleResourceNotFound(ResourceNotFoundException ex) {
        ExceptionDetails details = ExceptionDetails.builder()
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .details(ex.getMessage())
                .timestamp(dateUtil.formatLocalDateTimeToDatabaseStyle(LocalDateTime.now()))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(details);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionDetails> handleBusinessException(BusinessException ex) {
        ExceptionDetails details = ExceptionDetails.builder()
                .title("Bad Request / Business Rule Violation")
                .status(HttpStatus.BAD_REQUEST.value())
                .details(ex.getMessage())
                .timestamp(dateUtil.formatLocalDateTimeToDatabaseStyle(LocalDateTime.now()))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(details);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDetails> handleValidation(MethodArgumentNotValidException ex) {
        String fields = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField).collect(Collectors.joining(", "));

        String messages = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).collect(Collectors.joining("; "));

        ExceptionDetails details = ExceptionDetails.builder()
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .details("One or more fields are invalid.")
                .fields(fields)
                .fieldsMessage(messages)
                .timestamp(dateUtil.formatLocalDateTimeToDatabaseStyle(LocalDateTime.now()))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(details);
    }
}
