package com.company.digital.transaction.controller;

import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.common.api.ApiResponse;
import com.company.digital.common.exception.ApiException;
import com.company.digital.transaction.dto.DepositRequest;
import com.company.digital.transaction.dto.TransactionPostResponse;
import com.company.digital.transaction.dto.TransferRequest;
import com.company.digital.transaction.dto.WithdrawalRequest;
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
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Customer transaction posting APIs")
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping("/deposit")
	@Operation(summary = "Deposit", description = "Posts deposit on owned active account")
	public ResponseEntity<ApiResponse<TransactionPostResponse>> deposit(
		@Valid @RequestBody DepositRequest request,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(transactionService.deposit(request, actor)));
	}

	@PostMapping("/withdrawal")
	@Operation(summary = "Withdrawal", description = "Posts withdrawal on owned active account")
	public ResponseEntity<ApiResponse<TransactionPostResponse>> withdrawal(
		@Valid @RequestBody WithdrawalRequest request,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(transactionService.withdrawal(request, actor)));
	}

	@PostMapping("/transfer")
	@Operation(summary = "Transfer", description = "Posts transfer from owned source account to target account")
	public ResponseEntity<ApiResponse<TransactionPostResponse>> transfer(
		@Valid @RequestBody TransferRequest request,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(transactionService.transfer(request, actor)));
	}

	private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "User is not authenticated");
		}
		return user;
	}
}

