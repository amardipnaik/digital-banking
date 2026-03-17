package com.company.digital.account.entity;

import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.enums.AccountType;
import com.company.digital.auth.entity.User;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, unique = true, length = 34)
	private String accountNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AccountType accountType;

	@Column(nullable = false, length = 3)
	private String currencyCode = "INR";

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AccountStatus status = AccountStatus.PENDING_APPROVAL;

	private LocalDateTime openedAt;

	private LocalDateTime closedAt;

	@Column(length = 255)
	private String closedReason;

	private Long createdBy;

	private Long updatedBy;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}

