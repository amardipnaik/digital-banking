package com.company.digital.auth.entity;

import com.company.digital.auth.enums.UserStatus;
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
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@Column(nullable = false, unique = true, length = 20)
	private String mobileNumber;

	@Column(nullable = false, length = 255)
	private String passwordHash;

	@Column(nullable = false)
	private boolean emailVerified = false;

	private LocalDateTime emailVerifiedAt;

	@Column(nullable = false)
	private boolean mobileVerified = false;

	private LocalDateTime mobileVerifiedAt;

	@Column(nullable = false)
	private short failedLoginAttempts = 0;

	private LocalDateTime lastFailedLoginAt;

	private LocalDateTime lockUntil;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private UserStatus status = UserStatus.PENDING_VERIFICATION;

	@Column(length = 255)
	private String disabledReason;

	private Long disabledBy;

	private LocalDateTime disabledAt;

	private LocalDateTime lastLoginAt;

	@Column(length = 45)
	private String lastLoginIp;

	@Column(nullable = false)
	private boolean isDeleted = false;

	private Long deletedBy;

	private LocalDateTime deletedAt;

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
