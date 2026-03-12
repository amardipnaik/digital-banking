package com.company.digital.customer.dto;

import com.company.digital.customer.enums.CustomerAdminActionType;
import java.time.LocalDateTime;

public record CustomerActivityItemResponse(
	Long id,
	Long adminUserId,
	CustomerAdminActionType actionType,
	String reason,
	String beforeState,
	String afterState,
	LocalDateTime createdAt
) {
}

