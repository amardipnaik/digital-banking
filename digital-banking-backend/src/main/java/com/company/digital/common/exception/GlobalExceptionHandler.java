package com.company.digital.common.exception;

import com.company.digital.common.api.ApiError;
import com.company.digital.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception) {
		ApiError error = new ApiError(exception.getCode(), exception.getMessage(), List.of());
		return ResponseEntity.status(exception.getStatus()).body(ApiResponse.error(error));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
		List<String> details = exception.getBindingResult()
			.getAllErrors()
			.stream()
			.map(error -> {
				if (error instanceof FieldError fieldError) {
					return fieldError.getField() + ": " + fieldError.getDefaultMessage();
				}
				return error.getDefaultMessage();
			})
			.toList();

		ApiError error = new ApiError("VALIDATION_ERROR", "Request validation failed", details);
		return ResponseEntity.badRequest().body(ApiResponse.error(error));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
		List<String> details = exception.getConstraintViolations().stream()
			.map(v -> v.getPropertyPath() + ": " + v.getMessage())
			.toList();
		ApiError error = new ApiError("VALIDATION_ERROR", "Constraint validation failed", details);
		return ResponseEntity.badRequest().body(ApiResponse.error(error));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception exception) {
		ApiError error = new ApiError("INTERNAL_SERVER_ERROR", "Unexpected server error", List.of());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error));
	}
}
