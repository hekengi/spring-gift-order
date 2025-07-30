package gift.repository;

import gift.entity.UserKakaoToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserKakaoTokenRepository extends JpaRepository<UserKakaoToken, Long> {
    Optional<UserKakaoToken> findByMemberId(Long memberId);
    Optional<UserKakaoToken> findByAccessToken(String accessToken);
    
    @Query("SELECT ukt FROM UserKakaoToken ukt WHERE ukt.accessTokenExpiresAt < :now")
    List<UserKakaoToken> findExpiredAccessTokens(@Param("now") Instant now);
    
    @Query("SELECT ukt FROM UserKakaoToken ukt WHERE ukt.refreshTokenExpiresAt < :now")
    List<UserKakaoToken> findExpiredRefreshTokens(@Param("now") Instant now);
    
    boolean existsByMemberId(Long memberId);
} 