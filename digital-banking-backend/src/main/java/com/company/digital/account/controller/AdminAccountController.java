package com.company.digital.account.controller;

import com.company.digital.account.dto.AccountDetailResponse;
import com.company.digital.account.dto.AccountHistoryResponse;
import com.company.digital.account.dto.AdminAccountListResponse;
import com.company.digital.account.dto.UpdateAccountStatusRequest;
import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.enums.AccountType;
import com.company.digital.account.service.AccountService;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.common.api.ApiResponse;
import com.company.digital.common.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
@Tag(name = "Admin Accounts", description = "Admin-only account management APIs")
public class AdminAccountController {

	private final AccountService accountService;

	public AdminAccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping
	@Operation(summary = "List accounts", description = "Returns paginated account list with optional filters")
	public ResponseEntity<ApiResponse<AdminAccountListResponse>> listAccounts(
		@RequestParam(required = false) String search,
		@RequestParam(required = false) AccountStatus status,
		@RequestParam(required = false) AccountType accountType,
		@RequestParam(required = false) Long userId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "createdAt,desc") String sort
	) {
		return ResponseEntity.ok(ApiResponse.success(accountService.listAdminAccounts(search, status, accountType, userId, page, size, sort)));
	}

	@GetMapping("/{accountId}")
	@Operation(summary = "Get account", description = "Returns account detail by account id")
	public ResponseEntity<ApiResponse<AccountDetailResponse>> getAccount(@PathVariable Long accountId) {
		return ResponseEntity.ok(ApiResponse.success(accountService.getAdminAccount(accountId)));
	}

	@PatchMapping("/{accountId}/status")
	@Operation(summary = "Update account status", description = "Updates account lifecycle status and logs history")
	public ResponseEntity<ApiResponse<AccountDetailResponse>> updateStatus(
		@PathVariable Long accountId,
		@Valid @RequestBody UpdateAccountStatusRequest request,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(accountService.updateAccountStatus(accountId, request, actor)));
	}

	@GetMapping("/{accountId}/history")
	@Operation(summary = "Account history", description = "Returns lifecycle history entries for account")
	public ResponseEntity<ApiResponse<AccountHistoryResponse>> getHistory(
		@PathVariable Long accountId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		return ResponseEntity.ok(ApiResponse.success(accountService.getAccountHistory(accountId, page, size)));
	}

	private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "User is not authenticated");
		}
		return user;
	}
}

