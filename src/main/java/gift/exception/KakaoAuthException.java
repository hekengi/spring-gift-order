package gift.exception;

import gift.dto.KakaoErrorResponseDto;

public class KakaoAuthException extends RuntimeException {
    private final KakaoErrorResponseDto error;

    public KakaoAuthException(KakaoErrorResponseDto error) {
        super("카카오 인증 에러 발생");
        this.error = error;
    }

    public KakaoAuthException(String message) {
        super(message);
        this.error = null;
    }

    public KakaoErrorResponseDto getError() {
        return error;
    }
}
