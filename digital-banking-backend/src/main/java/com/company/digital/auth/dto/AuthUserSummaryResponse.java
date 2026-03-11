package com.company.digital.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthUserSummaryResponse", description = "Compact authenticated user details")
public record AuthUserSummaryResponse(
	@Schema(example = "2001") Long id,
	@Schema(example = "CUSTOMER") String role,
	@Schema(example = "ACTIVE") String status,
	@Schema(example = "anita@example.com") String email,
	@Schema(example = "9876543210") String mobileNumber,
	@Schema(example = "true") boolean emailVerified,
	@Schema(example = "true") boolean mobileVerified
) {
}
