package com.company.digital.account.controller;

import com.company.digital.account.dto.AccountDetailResponse;
import com.company.digital.account.dto.CreateAccountRequest;
import com.company.digital.account.dto.CreateAccountResponse;
import com.company.digital.account.dto.CustomerAccountListResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Customer account management APIs")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping
	@Operation(summary = "Create account", description = "Creates a new account for authenticated customer")
	public ResponseEntity<ApiResponse<CreateAccountResponse>> createAccount(
		@Valid @RequestBody CreateAccountRequest request,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(accountService.createAccount(request, actor)));
	}

	@GetMapping
	@Operation(summary = "List my accounts", description = "Returns paginated accounts for authenticated customer")
	public ResponseEntity<ApiResponse<CustomerAccountListResponse>> listMyAccounts(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt,desc") String sort,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(accountService.listMyAccounts(actor, page, size, sort)));
	}

	@GetMapping("/{accountId}")
	@Operation(summary = "Get my account", description = "Returns account detail owned by authenticated customer")
	public ResponseEntity<ApiResponse<AccountDetailResponse>> getMyAccount(
		@PathVariable Long accountId,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(accountService.getMyAccount(accountId, actor)));
	}

	private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "User is not authenticated");
		}
		return user;
	}
}

