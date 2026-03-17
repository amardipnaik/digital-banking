package com.company.digital.transaction.service;

import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.transaction.dto.AdminAdjustmentRequest;
import com.company.digital.transaction.dto.AdminReversalRequest;
import com.company.digital.transaction.dto.DepositRequest;
import com.company.digital.transaction.dto.TransactionPostResponse;
import com.company.digital.transaction.dto.TransferRequest;
import com.company.digital.transaction.dto.WithdrawalRequest;

public interface TransactionService {
	TransactionPostResponse deposit(DepositRequest request, AuthenticatedUser actor);

	TransactionPostResponse withdrawal(WithdrawalRequest request, AuthenticatedUser actor);

	TransactionPostResponse transfer(TransferRequest request, AuthenticatedUser actor);

	TransactionPostResponse adminAdjustment(AdminAdjustmentRequest request, AuthenticatedUser actor);

	TransactionPostResponse adminReversal(AdminReversalRequest request, AuthenticatedUser actor);
}

