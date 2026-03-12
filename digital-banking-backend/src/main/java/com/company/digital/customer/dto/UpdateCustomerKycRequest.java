package com.company.digital.customer.dto;

import com.company.digital.customer.enums.KycStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateCustomerKycRequest", description = "Admin request to update customer KYC status")
public record UpdateCustomerKycRequest(
	@NotNull(message = "kycStatus is required")
	KycStatus kycStatus,
	String remarks
) {
}

