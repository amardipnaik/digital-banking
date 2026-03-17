package com.company.digital.balance.controller;

import com.company.digital.balance.dto.BalanceSummaryResponse;
import com.company.digital.balance.dto.MiniStatementResponse;
import com.company.digital.balance.service.BalanceService;
import com.company.digital.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/balances")
@Tag(name = "Admin Balance Enquiry", description = "Admin balance enquiry APIs")
public class AdminBalanceController {

	private final BalanceService balanceService;

	public AdminBalanceController(BalanceService balanceService) {
		this.balanceService = balanceService;
	}

	@GetMapping("/accounts/{accountId}")
	@Operation(summary = "Admin balance snapshot", description = "Returns available and ledger balance for any account")
	public ResponseEntity<ApiResponse<BalanceSummaryResponse>> getBalance(@PathVariable Long accountId) {
		return ResponseEntity.ok(ApiResponse.success(balanceService.getAdminBalance(accountId)));
	}

	@GetMapping("/accounts/{accountId}/mini-statement")
	@Operation(summary = "Admin mini statement", description = "Returns latest transactions for any account")
	public ResponseEntity<ApiResponse<MiniStatementResponse>> getMiniStatement(
		@PathVariable Long accountId,
		@RequestParam(defaultValue = "10") int limit
	) {
		return ResponseEntity.ok(ApiResponse.success(balanceService.getAdminMiniStatement(accountId, limit)));
	}
}

