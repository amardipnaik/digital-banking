package com.company.digital.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MessageResponse", description = "Simple success response message")
public record MessageResponse(
	@Schema(example = "Operation completed successfully") String message
) {
}
