package gift.service;

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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Transactional(readOnly = true)
public class KakaoOAuthService {

    private final JwtService jwtService;
    private final RestClient restClient;
    private final MemberRepository memberRepository;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public KakaoOAuthService(JwtService jwtService, RestClient.Builder builder, MemberRepository memberRepository) {
        this.jwtService = jwtService;
        this.restClient = builder.build();
        this.memberRepository = memberRepository;
    }

    public String createKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
    }

    //카카오 로그인 → JWT 발급 (자동 회원가입)
    public TokenResponseDto loginWithKakao(String code) {
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

        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(KakaoTokenResponseDto.class);
    }

    //카카오 사용자 정보 요청
    private KakaoUserInfoResponseDto fetchUserInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        return restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfoResponseDto.class);
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
