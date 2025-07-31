package gift.service;

import gift.entity.Member;
import gift.entity.MemberRole;
import gift.entity.Order;
import gift.entity.UserKakaoToken;
import gift.exception.KakaoAuthException;
import gift.repository.UserKakaoTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KakaoMessageServiceTest {

    @Mock
    private UserKakaoTokenRepository userKakaoTokenRepository;

    @InjectMocks
    private KakaoMessageService kakaoMessageService;

    private Member member;
    private UserKakaoToken userToken;
    private Order order;

    @BeforeEach
    void setUp() {
        member = new Member("test@test.com", "password", MemberRole.USER);
        member.setId(1L);
        
        userToken = new UserKakaoToken(member, "access_token", "refresh_token", 3600, 5184000);
        
        order = new Order(member, null, 2, "테스트 메시지");
    }

    @Test
    @DisplayName("카카오 토큰이 없을 때 예외 발생")
    void sendOrderMessage_NoToken_ThrowsException() {
        // given
        when(userKakaoTokenRepository.findByMemberId(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> kakaoMessageService.sendOrderMessage(1L, order))
                .isInstanceOf(KakaoAuthException.class)
                .hasMessage("카카오 로그인이 필요합니다.");
    }

    @Test
    @DisplayName("리프레시 토큰이 만료되었을 때 예외 발생")
    void sendOrderMessage_ExpiredRefreshToken_ThrowsException() {
        // given - 리프레시 토큰도 만료된 토큰
        UserKakaoToken expiredToken = new UserKakaoToken(member, "expired_token", "expired_refresh_token", -3600, -5184000);
        
        when(userKakaoTokenRepository.findByMemberId(1L)).thenReturn(Optional.of(expiredToken));

        // when & then
        assertThatThrownBy(() -> kakaoMessageService.sendOrderMessage(1L, order))
                .isInstanceOf(KakaoAuthException.class)
                .hasMessage("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
    }
} 