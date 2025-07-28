package gift;

import gift.dto.TokenResponseDto;
import gift.entity.Member;
import gift.entity.MemberRole;
import gift.exception.KakaoAuthException;
import gift.repository.MemberRepository;
import gift.service.JwtService;
import gift.service.KakaoOAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class KakaoOAuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private RestClient restClient;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private KakaoOAuthService kakaoOAuthService;

    @BeforeEach
    void setUp() {
        // 테스트용 설정값 주입
        try {
            java.lang.reflect.Field clientIdField = KakaoOAuthService.class.getDeclaredField("clientId");
            clientIdField.setAccessible(true);
            clientIdField.set(kakaoOAuthService, "test_client_id");

            java.lang.reflect.Field redirectUriField = KakaoOAuthService.class.getDeclaredField("redirectUri");
            redirectUriField.setAccessible(true);
            redirectUriField.set(kakaoOAuthService, "http://localhost:8080/oauth/kakao/callback");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("카카오 로그인 URL 생성 성공")
    void createKakaoLoginUrl_Success() {
        // when
        String loginUrl = kakaoOAuthService.createKakaoLoginUrl();

        // then
        assertThat(loginUrl).contains("https://kauth.kakao.com/oauth/authorize");
        assertThat(loginUrl).contains("client_id=test_client_id");
        assertThat(loginUrl).contains("redirect_uri=http://localhost:8080/oauth/kakao/callback");
        assertThat(loginUrl).contains("response_type=code");
    }

    @Test
    @DisplayName("인가 코드가 null인 경우")
    void loginWithKakao_NullCode() {
        // when & then
        assertThatThrownBy(() -> kakaoOAuthService.loginWithKakao(null))
                .isInstanceOf(KakaoAuthException.class)
                .hasMessageContaining("카카오 인증 실패");
    }

    @Test
    @DisplayName("인가 코드가 빈 문자열인 경우")
    void loginWithKakao_EmptyCode() {
        // when & then
        assertThatThrownBy(() -> kakaoOAuthService.loginWithKakao(""))
                .isInstanceOf(KakaoAuthException.class)
                .hasMessageContaining("카카오 인증 실패");
    }

    @Test
    @DisplayName("인가 코드가 공백인 경우")
    void loginWithKakao_BlankCode() {
        // when & then
        assertThatThrownBy(() -> kakaoOAuthService.loginWithKakao("   "))
                .isInstanceOf(KakaoAuthException.class)
                .hasMessageContaining("카카오 인증 실패");
    }
} 