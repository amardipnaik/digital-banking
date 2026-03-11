package com.company.digital.auth.dto;

import com.company.digital.auth.enums.TokenChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "VerificationRequest", description = "Request to issue email/mobile verification token")
public record VerificationRequest(
	@Schema(example = "anita@example.com")
	@NotBlank(message = "loginId is required")
	String loginId,

	@NotNull(message = "channel is required")
	@Schema(implementation = TokenChannel.class)
	TokenChannel channel
) {
}
