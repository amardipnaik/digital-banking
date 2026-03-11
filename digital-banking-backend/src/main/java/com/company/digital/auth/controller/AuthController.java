package com.company.digital.auth.controller;

import com.company.digital.auth.dto.AuthUserSummaryResponse;
import com.company.digital.auth.dto.ForgotPasswordRequest;
import com.company.digital.auth.dto.LoginRequest;
import com.company.digital.auth.dto.LoginResponse;
import com.company.digital.auth.dto.MeResponse;
import com.company.digital.auth.dto.MessageResponse;
import com.company.digital.auth.dto.RegisterCustomerRequest;
import com.company.digital.auth.dto.ResetPasswordRequest;
import com.company.digital.auth.dto.VerificationConfirmRequest;
import com.company.digital.auth.dto.VerificationRequest;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.auth.service.AuthService;
import com.company.digital.common.api.ApiResponse;
import com.company.digital.common.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and credential management APIs")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register/customer")
	@Operation(summary = "Register customer", description = "Registers a customer account with pending verification")
	public ResponseEntity<ApiResponse<AuthUserSummaryResponse>> registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authService.registerCustomer(request)));
	}

	@PostMapping("/login")
	@Operation(summary = "Login", description = "Login using email/mobile and password")
	public ResponseEntity<ApiResponse<LoginResponse>> login(
		@Valid @RequestBody LoginRequest request,
		HttpServletRequest httpServletRequest
	) {
		String ipAddress = httpServletRequest.getRemoteAddr();
		String userAgent = httpServletRequest.getHeader("User-Agent");
		return ResponseEntity.ok(ApiResponse.success(authService.login(request, ipAddress, userAgent)));
	}

	@PostMapping("/verification/request")
	@Operation(summary = "Request verification token", description = "Creates email/mobile verification token")
	public ResponseEntity<ApiResponse<MessageResponse>> requestVerification(@Valid @RequestBody VerificationRequest request) {
		return ResponseEntity.ok(ApiResponse.success(authService.requestVerification(request)));
	}

	@PostMapping("/verification/confirm")
	@Operation(summary = "Confirm verification token", description = "Verifies email/mobile token and updates user verification status")
	public ResponseEntity<ApiResponse<MessageResponse>> confirmVerification(@Valid @RequestBody VerificationConfirmRequest request) {
		return ResponseEntity.ok(ApiResponse.success(authService.confirmVerification(request)));
	}

	@PostMapping("/password/forgot")
	@Operation(summary = "Forgot password", description = "Generates password reset token")
	public ResponseEntity<ApiResponse<MessageResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		return ResponseEntity.ok(ApiResponse.success(authService.forgotPassword(request)));
	}

	@PostMapping("/password/reset")
	@Operation(summary = "Reset password", description = "Resets password using reset token")
	public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		return ResponseEntity.ok(ApiResponse.success(authService.resetPassword(request)));
	}

	@PostMapping("/logout")
	@Operation(summary = "Logout", description = "Logs out the current user")
	public ResponseEntity<ApiResponse<MessageResponse>> logout(Authentication authentication) {
		AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(authService.logout(authenticatedUser)));
	}

	@GetMapping("/me")
	@Operation(summary = "Current user", description = "Returns authenticated user details")
	public ResponseEntity<ApiResponse<MeResponse>> me(Authentication authentication) {
		AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(authService.me(authenticatedUser)));
	}

	private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "User is not authenticated");
		}
		return user;
	}
}
