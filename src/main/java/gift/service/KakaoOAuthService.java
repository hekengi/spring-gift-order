package gift.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.KakaoErrorResponseDto;
import gift.dto.KakaoTokenResponseDto;
import gift.dto.KakaoUserInfoResponseDto;
import gift.dto.TokenResponseDto;
import gift.entity.Member;
import gift.entity.MemberRole;
import gift.exception.KakaoAuthException;
import gift.repository.MemberRepository;
import gift.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
@Transactional(readOnly = true)
public class KakaoOAuthService {

    private final JwtService jwtService;
    private final RestClient restClient;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public KakaoOAuthService(JwtService jwtService, RestClient restClient, MemberRepository memberRepository, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.restClient = restClient;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
    }

    public String createKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
    }

    //카카오 로그인 → JWT 발급 (자동 회원가입)
    public TokenResponseDto loginWithKakao(String code) {
        // 입력 검증
        if (code == null || code.trim().isEmpty()) {
            throw new KakaoAuthException("카카오 인증 실패: 인가 코드가 필요합니다");
        }

        KakaoTokenResponseDto tokenResponse = requestAccessToken(code);
        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            throw new KakaoAuthException("카카오 토큰 발급 실패");
        }

        KakaoUserInfoResponseDto userInfo = fetchUserInfo(tokenResponse.accessToken());
        if (userInfo == null) {
            throw new KakaoAuthException("카카오 사용자 정보 요청 실패");
        }

        Member member = findOrCreateMember(userInfo);
        String jwtToken = jwtService.generateToken(member.getId(), member.getRole().name());
        return new TokenResponseDto(jwtToken);
    }

    // 카카오 Access Token 요청
    private KakaoTokenResponseDto requestAccessToken(String code) {
        String url = "https://kauth.kakao.com/oauth/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        try {
            return restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        String errorBody = response.getBody().toString();
                        KakaoErrorResponseDto error = parseKakaoError(errorBody);
                        throw new KakaoAuthException(error);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new KakaoAuthException("카카오 서버 오류: 잠시 후 다시 시도해주세요");
                    })
                    .body(KakaoTokenResponseDto.class);
        } catch (RestClientException e) {
            throw new KakaoAuthException("카카오 인증 실패: " + e.getMessage());
        }
    }

    //카카오 사용자 정보 요청
    private KakaoUserInfoResponseDto fetchUserInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        try {
            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        String errorBody = response.getBody().toString();
                        KakaoErrorResponseDto error = parseKakaoError(errorBody);
                        throw new KakaoAuthException(error);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new KakaoAuthException("카카오 서버 오류: 잠시 후 다시 시도해주세요");
                    })
                    .body(KakaoUserInfoResponseDto.class);
        } catch (RestClientException e) {
            throw new KakaoAuthException("카카오 사용자 정보 요청 실패: " + e.getMessage());
        }
    }

    // 카카오 에러 응답 파싱
    private KakaoErrorResponseDto parseKakaoError(String errorBody) {
        try {
            return objectMapper.readValue(errorBody, KakaoErrorResponseDto.class);
        } catch (Exception e) {
            // 파싱 실패 시 기본 에러 응답 생성
            return new KakaoErrorResponseDto("unknown_error", "알 수 없는 오류", null, "카카오 API 응답을 파싱할 수 없습니다");
        }
    }

    // DB에 회원이 없으면 자동 회원가입
    private Member findOrCreateMember(KakaoUserInfoResponseDto userInfo) {
        final String email;
        if (userInfo.kakaoAccount() != null && userInfo.kakaoAccount().profile() != null) {
            email = userInfo.kakaoAccount().profile().nickname() + "@kakao.com";
        } else {
            email = "unknown@kakao.com";
        }
        return memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    Member newMember = new Member(email, "kakao-temp", MemberRole.USER);
                    return memberRepository.save(newMember);
                });
    }
}
