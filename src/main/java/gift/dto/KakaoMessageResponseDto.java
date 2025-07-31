package gift.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KakaoMessageResponseDto {
    
    @JsonProperty("result_code")
    private Integer resultCode;
    
    public KakaoMessageResponseDto() {}
    
    public KakaoMessageResponseDto(Integer resultCode) {
        this.resultCode = resultCode;
    }
    
    // Getter & Setter
    public Integer getResultCode() { return resultCode; }
    public void setResultCode(Integer resultCode) { this.resultCode = resultCode; }

    // 성공 여부 확인 메서드
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }
} 