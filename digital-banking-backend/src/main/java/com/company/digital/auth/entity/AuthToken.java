package com.company.digital.auth.entity;

import com.company.digital.auth.enums.TokenChannel;
import com.company.digital.auth.enums.TokenType;
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
@Table(name = "auth_tokens")
public class AuthToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TokenType tokenType;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private TokenChannel channel;

	@Column(nullable = false, unique = true, length = 255)
	private String tokenHash;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	private LocalDateTime consumedAt;

	@Column(nullable = false)
	private short attemptCount = 0;

	@Column(nullable = false)
	private short maxAttempts = 3;

	@Column(nullable = false)
	private boolean isUsed = false;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
