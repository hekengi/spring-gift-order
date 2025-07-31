package gift.dto;

import java.time.LocalDateTime;

public class OrderResponseDto {
    
    private Long id;
    private Long optionId;
    private Integer quantity;
    private LocalDateTime orderDateTime;
    private String message;
    
    public OrderResponseDto() {}
    
    public OrderResponseDto(Long id, Long optionId, Integer quantity, LocalDateTime orderDateTime, String message) {
        this.id = id;
        this.optionId = optionId;
        this.quantity = quantity;
        this.orderDateTime = orderDateTime;
        this.message = message;
    }
    
    // Getter & Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }
    
    public void setOrderDateTime(LocalDateTime orderDateTime) {
        this.orderDateTime = orderDateTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
} 