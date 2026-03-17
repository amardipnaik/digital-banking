package com.company.digital.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "AdminReversalRequest", description = "Admin reversal request")
public record AdminReversalRequest(
	@NotBlank(message = "originalTransactionRef is required")
	String originalTransactionRef,

	@NotBlank(message = "reason is required")
	String reason
) {
}

