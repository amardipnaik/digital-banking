package com.company.digital.balance.controller;

import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.balance.dto.BalanceSummaryResponse;
import com.company.digital.balance.dto.MiniStatementResponse;
import com.company.digital.balance.service.BalanceService;
import com.company.digital.common.api.ApiResponse;
import com.company.digital.common.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/balances")
@Tag(name = "Balance Enquiry", description = "Customer balance enquiry APIs")
public class BalanceController {

	private final BalanceService balanceService;

	public BalanceController(BalanceService balanceService) {
		this.balanceService = balanceService;
	}

	@GetMapping("/accounts/{accountId}")
	@Operation(summary = "Balance snapshot", description = "Returns available and ledger balance for owned account")
	public ResponseEntity<ApiResponse<BalanceSummaryResponse>> getBalance(@PathVariable Long accountId, Authentication authentication) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(balanceService.getMyBalance(accountId, actor)));
	}

	@GetMapping("/accounts/{accountId}/mini-statement")
	@Operation(summary = "Mini statement", description = "Returns latest transactions for owned account")
	public ResponseEntity<ApiResponse<MiniStatementResponse>> getMiniStatement(
		@PathVariable Long accountId,
		@RequestParam(defaultValue = "10") int limit,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(balanceService.getMyMiniStatement(accountId, limit, actor)));
	}

	private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "User is not authenticated");
		}
		return user;
	}
}

