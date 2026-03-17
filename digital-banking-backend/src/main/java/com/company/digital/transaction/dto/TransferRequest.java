package com.company.digital.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Schema(name = "TransferRequest", description = "Customer transfer request")
public record TransferRequest(
	@NotNull(message = "sourceAccountId is required")
	Long sourceAccountId,

	@NotNull(message = "targetAccountId is required")
	Long targetAccountId,

	@NotNull(message = "amount is required")
	@DecimalMin(value = "0.01", message = "amount must be greater than 0")
	BigDecimal amount,

	@Pattern(regexp = "^[A-Z]{3}$", message = "currencyCode must be a 3-letter uppercase code")
	String currencyCode,
	String remarks,
	String idempotencyKey
) {
}

