package com.company.digital.transaction.entity;

import com.company.digital.account.entity.Account;
import com.company.digital.auth.entity.User;
import com.company.digital.transaction.enums.EntrySide;
import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "account_transactions")
public class AccountTransaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	@Column(nullable = false, unique = true, length = 64)
	private String transactionRef;

	@Column(length = 80)
	private String idempotencyKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TransactionType transactionType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private EntrySide entrySide;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 3)
	private String currencyCode;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balanceBefore;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balanceAfter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "counterparty_account_id")
	private Account counterpartyAccount;

	@Column(length = 64)
	private String transferGroupRef;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TransactionStatus status = TransactionStatus.POSTED;

	@Column(length = 255)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}

