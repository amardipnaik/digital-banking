package com.company.digital.auth.entity;

import com.company.digital.customer.enums.KycStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer_profiles")
public class CustomerProfile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(nullable = false, length = 120)
	private String fullName;

	private LocalDate dateOfBirth;

	@Column(name = "address_line_1", length = 150)
	private String addressLine1;

	@Column(name = "address_line_2", length = 150)
	private String addressLine2;

	@Column(length = 80)
	private String city;

	@Column(length = 80)
	private String state;

	@Column(length = 20)
	private String postalCode;

	@Column(length = 80)
	private String country;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private KycStatus kycStatus = KycStatus.PENDING;

	@Column(length = 50)
	private String governmentId;

	@Column(length = 30)
	private String governmentIdType;

	private Long kycReviewedBy;

	private LocalDateTime kycReviewedAt;

	@Column(length = 255)
	private String kycRemarks;

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
