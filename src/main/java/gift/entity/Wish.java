package gift.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "wish")
public class Wish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public Wish() {}

    public Wish(Member member, Product product, Integer quantity) {
        this.member = member;
        this.product = product;
        this.quantity = quantity;
    }

    // 위시리스트 수량 감소 메서드
    public void decreaseQuantity(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("감소 수량은 1 이상이어야 합니다.");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("위시리스트 수량이 부족합니다.");
        }
        this.quantity -= amount;
    }

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}