package gift.service;

import gift.dto.KakaoMessageRequestDto;
import gift.dto.KakaoMessageResponseDto;
import gift.dto.KakaoTokenResponseDto;
import gift.entity.Order;
import gift.entity.UserKakaoToken;
import gift.exception.KakaoAuthException;
import gift.repository.UserKakaoTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class KakaoMessageService {
    
    private final RestClient restClient;
    private final UserKakaoTokenRepository userKakaoTokenRepository;
    
    @Value("${kakao.api.host}")
    private String apiHost;

    @Value("${kakao.client-id}")
    private String clientId;
    
    public KakaoMessageService(RestClient restClient, UserKakaoTokenRepository userKakaoTokenRepository) {
        this.restClient = restClient; // WebConfig에 등록한 RestClient 빈
        this.userKakaoTokenRepository = userKakaoTokenRepository;
    }
    
    // 주문 정보를 카카오톡 메시지로 전송
    public void sendOrderMessage(Long memberId, Order order) {
        // 1. 사용자의 카카오 토큰 조회
        UserKakaoToken userToken = userKakaoTokenRepository.findByMemberId(memberId)
            .orElseThrow(() -> new KakaoAuthException("카카오 로그인이 필요합니다."));
        
        // 2. 액세스 토큰 만료됐으면 갱신
        if (userToken.isAccessTokenExpired()) {
            refreshAccessToken(userToken);
        }
        
        // 3. 메시지 전송
        sendMessage(userToken.getAccessToken(), createOrderMessage(order));
    }
    
    // 액세스 토큰 갱신
    private void refreshAccessToken(UserKakaoToken userToken) {
        if (userToken.isRefreshTokenExpired()) {
            throw new KakaoAuthException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
        }
        
        String url = "https://kauth.kakao.com/oauth/token";
        Map<String, String> body = new LinkedHashMap<>();
        body.put("grant_type", "refresh_token");
        body.put("client_id", clientId);
        body.put("refresh_token", userToken.getRefreshToken());
        
        try {
            KakaoTokenResponseDto tokenResponse = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(KakaoTokenResponseDto.class);
            
            if (tokenResponse == null || tokenResponse.accessToken() == null) {
                throw new KakaoAuthException("토큰 갱신에 실패했습니다.");
            }
            
            // 새로운 토큰으로 업데이트
            userToken.updateTokens(
                tokenResponse.accessToken(),
                tokenResponse.refreshToken() != null ? tokenResponse.refreshToken() : userToken.getRefreshToken(),
                tokenResponse.expiresIn(),
                tokenResponse.refreshTokenExpiresIn() != null ? tokenResponse.refreshTokenExpiresIn() : 5184000
            );
            
            userKakaoTokenRepository.save(userToken);
            
        } catch (Exception e) {
            throw new KakaoAuthException("토큰 갱신 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 카카오톡 메시지 전송
    private void sendMessage(String accessToken, KakaoMessageRequestDto messageRequest) {
        String url = apiHost + "/v2/api/talk/memo/default/send";
        
        try {
            KakaoMessageResponseDto response = restClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(Map.of("template_object", messageRequest))
                .retrieve()
                .body(KakaoMessageResponseDto.class);
            
            if (response == null || !response.isSuccess()) {
                throw new KakaoAuthException("메시지 전송에 실패했습니다. result_code: " + 
                    (response != null ? response.getResultCode() : "null"));
            }
            
        } catch (Exception e) {
            throw new KakaoAuthException("메시지 전송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 주문 정보로 Feed 템플릿 메시지 생성
    private KakaoMessageRequestDto createOrderMessage(Order order) {
        KakaoMessageRequestDto.Link link = new KakaoMessageRequestDto.Link(
            "https://your-website.com/orders/" + order.getId(),
            "https://your-website.com/orders/" + order.getId()
        );
        
        KakaoMessageRequestDto.Content content = new KakaoMessageRequestDto.Content(
            "주문이 완료되었습니다!",
            String.format("상품: %s\n옵션: %s\n수량: %d개\n메시지: %s",
                order.getProductOption().getProduct().getName(),
                order.getProductOption().getName(),
                order.getQuantity(),
                order.getMessage() != null ? order.getMessage() : "메시지 없음"),
            "https://your-website.com/images/gift-icon.png",
            800,
            400,
            link
        );
        
        return new KakaoMessageRequestDto("feed", content, link);
    }
} 