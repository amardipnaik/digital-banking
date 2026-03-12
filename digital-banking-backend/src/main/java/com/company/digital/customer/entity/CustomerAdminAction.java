package com.company.digital.customer.entity;

import com.company.digital.auth.entity.User;
import com.company.digital.customer.enums.CustomerAdminActionType;
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
@Table(name = "customer_admin_actions")
public class CustomerAdminAction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_user_id", nullable = false)
	private User customerUser;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "admin_user_id", nullable = false)
	private User adminUser;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private CustomerAdminActionType actionType;

	@Column(length = 255)
	private String reason;

	@Column(columnDefinition = "TEXT")
	private String beforeState;

	@Column(columnDefinition = "TEXT")
	private String afterState;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}

