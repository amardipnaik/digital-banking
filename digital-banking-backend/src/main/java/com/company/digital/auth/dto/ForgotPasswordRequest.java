package com.company.digital.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ForgotPasswordRequest", description = "Request to generate password reset token")
public record ForgotPasswordRequest(
	@Schema(example = "anita@example.com")
	@NotBlank(message = "loginId is required")
	String loginId
) {
}
