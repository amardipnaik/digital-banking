package com.company.digital.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "UpdateCustomerProfileRequest", description = "Admin request to update customer profile fields")
public record UpdateCustomerProfileRequest(
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

