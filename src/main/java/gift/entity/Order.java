package gift.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "order_date_time", nullable = false)
    private LocalDateTime orderDateTime;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    protected Order() {}
    
    // 생성자
    public Order(Member member, ProductOption productOption, Integer quantity, String message) {
        this.member = member;
        this.productOption = productOption;
        this.quantity = quantity;
        this.orderDateTime = LocalDateTime.now();
        this.message = message;
    }
    
    // Getter & Setter
    public Long getId() {
        return id;
    }
    
    public Member getMember() {
        return member;
    }
    
    public ProductOption getProductOption() {
        return productOption;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
} 