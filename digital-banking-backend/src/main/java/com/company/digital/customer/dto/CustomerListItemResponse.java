package com.company.digital.customer.dto;

import com.company.digital.auth.enums.UserStatus;
import com.company.digital.customer.enums.KycStatus;

public record CustomerListItemResponse(
	Long userId,
	String fullName,
	String email,
	String mobileNumber,
	UserStatus userStatus,
	KycStatus kycStatus
) {
}

