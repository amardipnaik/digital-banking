package com.company.digital.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "MeResponse", description = "Current authenticated user details")
public record MeResponse(
	@Schema(example = "2001") Long id,
	@Schema(example = "CUSTOMER") String role,
	@Schema(example = "anita@example.com") String email,
	@Schema(example = "9876543210") String mobileNumber,
	@Schema(example = "ACTIVE") String status,
	@Schema(example = "true") boolean emailVerified,
	@Schema(example = "true") boolean mobileVerified,
	@Schema(example = "Anita Sharma") String fullName,
	LocalDate dateOfBirth,
	String addressLine1,
	String addressLine2,
	String city,
	String state,
	String postalCode,
	String country,
	String governmentId,
	String governmentIdType,
	@Schema(example = "PENDING") String kycStatus
) {
}
