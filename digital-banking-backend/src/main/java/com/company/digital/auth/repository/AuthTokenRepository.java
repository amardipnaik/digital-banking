package com.company.digital.auth.repository;

import com.company.digital.auth.entity.AuthToken;
import com.company.digital.auth.entity.User;
import com.company.digital.auth.enums.TokenChannel;
import com.company.digital.auth.enums.TokenType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
	List<AuthToken> findAllByUserAndTokenTypeAndChannelAndIsUsedFalse(User user, TokenType tokenType, TokenChannel channel);
	List<AuthToken> findAllByUserAndTokenTypeAndIsUsedFalse(User user, TokenType tokenType);
	Optional<AuthToken> findFirstByUserAndTokenTypeAndChannelAndIsUsedFalseOrderByCreatedAtDesc(User user, TokenType tokenType, TokenChannel channel);
	Optional<AuthToken> findFirstByUserAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(User user, TokenType tokenType);
}
