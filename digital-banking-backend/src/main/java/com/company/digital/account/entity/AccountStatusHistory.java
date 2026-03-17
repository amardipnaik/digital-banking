package com.company.digital.account.entity;

import com.company.digital.account.enums.AccountStatus;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "account_status_history")
public class AccountStatusHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private AccountStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AccountStatus toStatus;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "changed_by", nullable = false)
	private User changedBy;

	@Column(length = 255)
	private String reason;

	@Column(columnDefinition = "TEXT")
	private String metadata;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}

