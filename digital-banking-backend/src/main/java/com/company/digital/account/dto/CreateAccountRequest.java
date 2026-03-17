package com.company.digital.account.dto;

import com.company.digital.account.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(name = "CreateAccountRequest", description = "Customer request to open a new bank account")
public record CreateAccountRequest(
	@NotNull(message = "accountType is required")
	AccountType accountType,

	@Pattern(regexp = "^[A-Z]{3}$", message = "currencyCode must be a 3-letter uppercase code")
	String currencyCode
) {
}

