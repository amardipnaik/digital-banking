package com.company.digital.auth.util;

import com.company.digital.common.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;

public final class TokenHashingUtil {

	private TokenHashingUtil() {
	}

	public static String hash(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashBytes);
		} catch (NoSuchAlgorithmException exception) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "HASHING_ERROR", "Unable to hash token");
		}
	}
}
