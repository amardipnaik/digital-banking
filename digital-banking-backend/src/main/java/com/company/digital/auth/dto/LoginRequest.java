package com.company.digital.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Payload to login with email/mobile")
public record LoginRequest(
	@Schema(example = "anita@example.com")
	@NotBlank(message = "loginId is required")
	String loginId,

	@Schema(example = "Password1")
	@NotBlank(message = "password is required")
	String password,

	@Schema(example = "DEVICE-123")
	String deviceId
) {
}
