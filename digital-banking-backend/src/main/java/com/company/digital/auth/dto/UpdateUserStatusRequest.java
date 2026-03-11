package com.company.digital.auth.dto;

import com.company.digital.auth.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateUserStatusRequest", description = "Admin request to enable/disable user")
public record UpdateUserStatusRequest(
	@NotNull(message = "status is required")
	@Schema(implementation = UserStatus.class)
	UserStatus status,

	@Schema(example = "Suspicious login attempts")
	String reason
) {
}
