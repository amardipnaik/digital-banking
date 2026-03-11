package com.company.digital.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(name = "RegisterCustomerRequest", description = "Payload to register a new customer")
public record RegisterCustomerRequest(
	@Schema(example = "Anita Sharma")
	@NotBlank(message = "fullName is required")
	String fullName,

	@Schema(example = "anita@example.com")
	@Email(message = "email must be valid")
	@NotBlank(message = "email is required")
	String email,

	@Schema(example = "9876543210")
	@NotBlank(message = "mobileNumber is required")
	@Pattern(regexp = "^[0-9]{10,15}$", message = "mobileNumber must be 10-15 digits")
	String mobileNumber,

	@Schema(example = "1994-07-21")
	LocalDate dateOfBirth,

	@Schema(example = "Password1")
	@NotBlank(message = "password is required")
	@Size(min = 8, message = "password must be at least 8 characters")
	String password,

	@Schema(example = "Password1")
	@NotBlank(message = "confirmPassword is required")
	String confirmPassword
) {
}
