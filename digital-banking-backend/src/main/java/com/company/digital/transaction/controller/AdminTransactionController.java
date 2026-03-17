package com.company.digital.transaction.controller;

import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.common.api.ApiResponse;
import com.company.digital.common.exception.ApiException;
import com.company.digital.transaction.dto.AdminAdjustmentRequest;
import com.company.digital.transaction.dto.AdminReversalRequest;
import com.company.digital.transaction.dto.TransactionPostResponse;
import com.company.digital.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/transactions")
@Tag(name = "Admin Transactions", description = "Admin operational transaction APIs")
public class AdminTransactionController {

	private final TransactionService transactionService;

	public AdminTransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping("/adjustment")
	@Operation(summary = "Adjustment", description = "Posts admin adjustment for operational correction")
	public ResponseEntity<ApiResponse<TransactionPostResponse>> adjustment(
		@Valid @RequestBody AdminAdjustmentRequest request,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(transactionService.adminAdjustment(request, actor)));
	}

	@PostMapping("/reversal")
	@Operation(summary = "Reversal", description = "Reverses an existing posted transaction")
	public ResponseEntity<ApiResponse<TransactionPostResponse>> reversal(
		@Valid @RequestBody AdminReversalRequest request,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(transactionService.adminReversal(request, actor)));
	}

	private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "User is not authenticated");
		}
		return user;
	}
}

