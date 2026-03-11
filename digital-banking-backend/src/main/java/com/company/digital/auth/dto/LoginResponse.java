package com.company.digital.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "Login success payload with JWT token")
public record LoginResponse(
	@Schema(example = "jwt-token") String accessToken,
	@Schema(example = "Bearer") String tokenType,
	@Schema(example = "86400") long expiresIn,
	AuthUserSummaryResponse user
) {
}
