package com.company.digital.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ResetPasswordRequest", description = "Request to reset password using a reset token")
public record ResetPasswordRequest(
	@Schema(example = "anita@example.com")
	@NotBlank(message = "loginId is required")
	String loginId,

	@Schema(example = "reset-token")
	@NotBlank(message = "token is required")
	String token,

	@Schema(example = "Password1")
	@NotBlank(message = "newPassword is required")
	@Size(min = 8, message = "newPassword must be at least 8 characters")
	String newPassword,

	@Schema(example = "Password1")
	@NotBlank(message = "confirmPassword is required")
	String confirmPassword
) {
}
