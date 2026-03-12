package com.company.digital.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "UpdateMeProfileRequest", description = "Authenticated customer profile update request")
public record UpdateMeProfileRequest(
	String fullName,
	LocalDate dateOfBirth,
	String addressLine1,
	String addressLine2,
	String city,
	String state,
	String postalCode,
	String country,
	String governmentId,
	String governmentIdType
) {
}

