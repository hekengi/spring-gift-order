package gift;

import gift.dto.OrderRequestDto;
import gift.entity.Member;
import gift.entity.MemberRole;
import gift.entity.Product;
import gift.entity.ProductOption;
import gift.exception.OrderException;
import gift.repository.MemberRepository;
import gift.repository.ProductOptionRepository;
import gift.repository.ProductRepository;
import gift.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OrderIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    private Member member;
    private Product product;
    private ProductOption productOption;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        member = new Member("test@test.com", "password", MemberRole.USER);
        member = memberRepository.save(member);

        product = new Product("테스트 상품", 10000, "test.jpg");
        product = productRepository.save(product);

        productOption = new ProductOption(product, "테스트 옵션", 10);
        productOption = productOptionRepository.save(productOption);
    }

    @Test
    @DisplayName("전체 주문 플로우 통합 테스트")
    void orderFlow_IntegrationTest() {
        // given
        OrderRequestDto orderRequest = new OrderRequestDto(
            productOption.getId(), 2, "통합 테스트 메시지"
        );

        // when
        var result = orderService.createOrder(member.getId(), orderRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOptionId()).isEqualTo(productOption.getId());
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getMessage()).isEqualTo("통합 테스트 메시지");

        // 상품 옵션 수량이 차감되었는지 확인
        ProductOption updatedOption = productOptionRepository.findById(productOption.getId()).orElseThrow();
        assertThat(updatedOption.getQuantity()).isEqualTo(8); // 10 - 2 = 8
    }

    @Test
    @DisplayName("재고 부족 시 주문 실패 테스트")
    void orderFlow_InsufficientStock_ThrowsException() {
        // given - 재고보다 많은 수량 주문
        OrderRequestDto orderRequest = new OrderRequestDto(
            productOption.getId(), 15, "재고 부족 테스트"
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(member.getId(), orderRequest))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("주문 수량이 재고보다 많습니다");
    }
} 