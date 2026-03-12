package com.company.digital.customer.dto;

import com.company.digital.auth.enums.UserStatus;
import com.company.digital.customer.enums.KycStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerDetailResponse(
	Long userId,
	String fullName,
	String email,
	String mobileNumber,
	UserStatus userStatus,
	KycStatus kycStatus,
	LocalDate dateOfBirth,
	String addressLine1,
	String addressLine2,
	String city,
	String state,
	String postalCode,
	String country,
	String governmentId,
	String governmentIdType,
	String kycRemarks,
	boolean emailVerified,
	boolean mobileVerified,
	boolean deleted,
	LocalDateTime deletedAt
) {
}

