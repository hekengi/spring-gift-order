package gift.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderRequestDto {
    
    @NotNull(message = "상품 옵션 ID는 필수입니다.")
    private Long optionId;
    
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;
    
    private String message;
    
    public OrderRequestDto() {}
    
    public OrderRequestDto(Long optionId, Integer quantity, String message) {
        this.optionId = optionId;
        this.quantity = quantity;
        this.message = message;
    }
    
    // Getter & Setter
    public Long getOptionId() {
        return optionId;
    }
    
    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
} 