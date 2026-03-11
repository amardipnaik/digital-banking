package com.company.digital.auth.controller;

import com.company.digital.auth.dto.MessageResponse;
import com.company.digital.auth.dto.UpdateUserStatusRequest;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.auth.service.AuthService;
import com.company.digital.common.api.ApiResponse;
import com.company.digital.common.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth/users")
@Tag(name = "Admin Auth", description = "Admin-only user status controls")
public class AdminAuthController {

	private final AuthService authService;

	public AdminAuthController(AuthService authService) {
		this.authService = authService;
	}

	@PatchMapping("/{userId}/status")
	@Operation(summary = "Update user status", description = "Updates user status to ACTIVE or DISABLED")
	public ResponseEntity<ApiResponse<MessageResponse>> updateUserStatus(
		@PathVariable Long userId,
		@Valid @RequestBody UpdateUserStatusRequest request,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(authService.updateUserStatus(userId, request, actor)));
	}

	private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "User is not authenticated");
		}
		return user;
	}
}
