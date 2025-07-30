package gift.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_kakao_tokens")
public class UserKakaoToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;
    
    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;
    
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
    
    @Column(name = "access_token_expires_at", nullable = false)
    private Instant accessTokenExpiresAt;
    
    @Column(name = "refresh_token_expires_at")
    private Instant refreshTokenExpiresAt;
    
    protected UserKakaoToken() {}
    
    public UserKakaoToken(Member member, String accessToken, String refreshToken,
                         Integer expiresIn, Integer refreshTokenExpiresIn) {
        this.member = member;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = Instant.now().plusSeconds(expiresIn);
        this.refreshTokenExpiresAt = Instant.now().plusSeconds(refreshTokenExpiresIn);
    }
    
    // 토큰 갱신
    public void updateTokens(String accessToken, String refreshToken, 
                           Integer expiresIn, Integer refreshTokenExpiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = Instant.now().plusSeconds(expiresIn);
        this.refreshTokenExpiresAt = Instant.now().plusSeconds(refreshTokenExpiresIn);
    }
    
    // 토큰 만료 확인
    public boolean isAccessTokenExpired() {
        return accessTokenExpiresAt.isBefore(Instant.now());
    }
    
    public boolean isRefreshTokenExpired() {
        return refreshTokenExpiresAt.isBefore(Instant.now());
    }

    // Getter
    public Long getId() { return id; }
    public Member getMember() { return member; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Instant getAccessTokenExpiresAt() { return accessTokenExpiresAt; }
    public Instant getRefreshTokenExpiresAt() { return refreshTokenExpiresAt; }
} 