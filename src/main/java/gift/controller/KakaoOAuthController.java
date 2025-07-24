package gift.controller;

import gift.dto.TokenResponseDto;
import gift.service.KakaoOAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/oauth/kakao")
public class KakaoOAuthController {

    private final KakaoOAuthService kakaoOAuthService;

    public KakaoOAuthController(KakaoOAuthService kakaoOAuthService) {
        this.kakaoOAuthService = kakaoOAuthService;
    }

    // 카카오 로그인 페이지
    @GetMapping("/login")
    public void redirectToKakao(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = kakaoOAuthService.createKakaoLoginUrl();
        response.sendRedirect(kakaoAuthUrl);
    }

    // 카카오 콜백 처리
    @GetMapping("/callback")
    public ResponseEntity<TokenResponseDto> kakaoCallback(@RequestParam("code") String code) {
        TokenResponseDto response = kakaoOAuthService.loginWithKakao(code);
        return ResponseEntity.ok(response);
    }
}
