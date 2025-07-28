package gift;

import gift.controller.KakaoOAuthController;
import gift.dto.TokenResponseDto;
import gift.exception.KakaoAuthException;
import gift.service.KakaoOAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KakaoOAuthControllerTest {

    @Mock
    private KakaoOAuthService kakaoOAuthService;

    @InjectMocks
    private KakaoOAuthController kakaoOAuthController;

    @Test
    @DisplayName("카카오 콜백 처리 성공")
    void kakaoCallback_Success() {
        // given
        String code = "test_authorization_code";
        TokenResponseDto expectedResponse = new TokenResponseDto("test_jwt_token");
        when(kakaoOAuthService.loginWithKakao(code)).thenReturn(expectedResponse);

        // when
        ResponseEntity<TokenResponseDto> response = kakaoOAuthController.kakaoCallback(code);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        assertThat(response.getBody().getToken()).isEqualTo("test_jwt_token");
    }

    @Test
    @DisplayName("카카오 콜백 - 서비스에서 예외 발생")
    void kakaoCallback_ServiceException() {
        // given
        String code = "invalid_code";
        when(kakaoOAuthService.loginWithKakao(code))
                .thenThrow(new KakaoAuthException("카카오 인증 실패: 인가 코드가 필요합니다"));

        // when & then
        assertThatThrownBy(() -> kakaoOAuthController.kakaoCallback(code))
                .isInstanceOf(KakaoAuthException.class)
                .hasMessageContaining("카카오 인증 실패");
    }

    @Test
    @DisplayName("카카오 콜백 - 빈 인가 코드")
    void kakaoCallback_EmptyCode() {
        // given
        when(kakaoOAuthService.loginWithKakao(""))
                .thenThrow(new KakaoAuthException("카카오 인증 실패: 인가 코드가 필요합니다"));

        // when & then
        assertThatThrownBy(() -> kakaoOAuthController.kakaoCallback(""))
                .isInstanceOf(KakaoAuthException.class)
                .hasMessageContaining("카카오 인증 실패");
    }
} 