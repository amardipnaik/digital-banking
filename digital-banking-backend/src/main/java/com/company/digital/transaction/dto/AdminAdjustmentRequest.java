package com.company.digital.transaction.dto;

import com.company.digital.transaction.enums.EntrySide;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Schema(name = "AdminAdjustmentRequest", description = "Admin adjustment posting request")
public record AdminAdjustmentRequest(
	@NotNull(message = "accountId is required")
	Long accountId,

	@NotNull(message = "entrySide is required")
	EntrySide entrySide,

	@NotNull(message = "amount is required")
	@DecimalMin(value = "0.01", message = "amount must be greater than 0")
	BigDecimal amount,

	@Pattern(regexp = "^[A-Z]{3}$", message = "currencyCode must be a 3-letter uppercase code")
	String currencyCode,

	@NotBlank(message = "reason is required")
	String reason,

	String idempotencyKey
) {
}

