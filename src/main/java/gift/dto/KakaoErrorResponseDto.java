package gift.dto;

public record KakaoErrorResponseDto(
        String error,
        String error_description,
        Integer code,
        String msg
) {}