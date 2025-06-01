package com.zenika.distributor.controller;


import com.zenika.distributor.exception.*; // Ensure this import matches your exception package
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException; // For the first WARN in your log

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  // This should handle your InvalidCoinException
  @ExceptionHandler(InvalidCoinException.class)
  public ResponseEntity<Map<String, String>> handleInvalidCoinException(InvalidCoinException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "Invalid Coin"); // General error type
    errorResponse.put("message", ex.getMessage()); // Specific message from the exception
    // You could also add accepted values here if desired, or rely on ex.getMessage()
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<Map<String, String>> handleInsufficientFundsException(InsufficientFundsException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "Insufficient Funds");
    errorResponse.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(errorResponse);
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleProductNotFoundException(ProductNotFoundException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "Product Not Found");
    errorResponse.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  @ExceptionHandler(NoItemSelectedException.class)
  public ResponseEntity<Map<String, String>> handleNoItemSelectedException(NoItemSelectedException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "No Item Selected");
    errorResponse.put("message", ex.getMessage());
    return ResponseEntity.badRequest().body(errorResponse);
  }

  // Handles validation errors from @Valid on request bodies
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      fieldErrors.put(fieldName, errorMessage);
    });
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", "Validation Failed");
    errorResponse.put("fieldErrors", fieldErrors);
    return ResponseEntity.badRequest().body(errorResponse);
  }

  // Handles "Required request body is missing"
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "Malformed JSON Request or Missing Body");
    // You can make this message more specific if you parse ex.getMessage()
    errorResponse.put("message", "The request body is missing or not readable. Please ensure you are sending a valid JSON payload.");
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(Exception.class) // Generic fallback
  public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
    // It's good practice to log the full exception here for debugging
    ex.printStackTrace(); // Or use a proper logger
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "Internal Server Error");
    errorResponse.put("message", "An unexpected error occurred. Please try again later.");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
