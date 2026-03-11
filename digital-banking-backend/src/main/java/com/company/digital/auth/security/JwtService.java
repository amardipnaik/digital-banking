package com.company.digital.auth.security;

import com.company.digital.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final String jwtSecret;
	private final long jwtExpirationSeconds;

	public JwtService(
		@Value("${app.auth.jwt-secret}") String jwtSecret,
		@Value("${app.auth.jwt-expiration-seconds:86400}") long jwtExpirationSeconds
	) {
		this.jwtSecret = jwtSecret;
		this.jwtExpirationSeconds = jwtExpirationSeconds;
	}

	public String generateToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", user.getId());
		claims.put("email", user.getEmail());
		claims.put("role", user.getRole().getCode().name());

		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(jwtExpirationSeconds);

		return Jwts.builder()
			.claims(claims)
			.subject(String.valueOf(user.getId()))
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.signWith(getSigningKey())
			.compact();
	}

	public AuthenticatedUser parseUser(String token) {
		Claims claims = parseClaims(token);
		Long userId = claims.get("userId", Long.class);
		String email = claims.get("email", String.class);
		String role = claims.get("role", String.class);
		return new AuthenticatedUser(userId, email, role);
	}

	public boolean isValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	public long getJwtExpirationSeconds() {
		return jwtExpirationSeconds;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSigningKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private SecretKey getSigningKey() {
		byte[] keyBytes;
		try {
			keyBytes = Decoders.BASE64.decode(jwtSecret);
		} catch (Exception ignored) {
			keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		}

		if (keyBytes.length < 32) {
			byte[] padded = new byte[32];
			System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
			for (int i = keyBytes.length; i < 32; i++) {
				padded[i] = 'x';
			}
			keyBytes = padded;
		}

		return Keys.hmacShaKeyFor(keyBytes);
	}
}
