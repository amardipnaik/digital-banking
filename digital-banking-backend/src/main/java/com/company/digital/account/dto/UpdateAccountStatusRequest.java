package com.company.digital.account.dto;

import com.company.digital.account.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateAccountStatusRequest", description = "Admin request to update account lifecycle status")
public record UpdateAccountStatusRequest(
	@NotNull(message = "status is required")
	AccountStatus status,
	String reason
) {
}

