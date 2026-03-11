package com.company.digital.auth.entity;

import com.company.digital.auth.enums.LoginResult;
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
@Table(name = "login_activity_logs")
public class LoginActivityLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, length = 150)
	private String loginIdentifier;

	private Short attemptNo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LoginResult result;

	@Column(length = 120)
	private String failureReason;

	@Column(length = 45)
	private String ipAddress;

	@Column(length = 500)
	private String userAgent;

	@Column(length = 120)
	private String deviceId;

	@Column(length = 120)
	private String locationHint;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
