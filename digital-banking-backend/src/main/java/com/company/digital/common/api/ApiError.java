package com.company.digital.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "ApiError", description = "Standard API error payload")
public record ApiError(
	@Schema(example = "AUTH_INVALID_CREDENTIALS") String code,
	@Schema(example = "Invalid login credentials") String message,
	@Schema(description = "Validation or additional error details") List<String> details
) {}
