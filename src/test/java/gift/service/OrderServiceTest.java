package gift.service;

import gift.dto.OrderRequestDto;
import gift.dto.OrderResponseDto;
import gift.entity.*;
import gift.exception.OrderException;
import gift.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private ProductOptionRepository productOptionRepository;
    
    @Mock
    private WishRepository wishRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private KakaoMessageService kakaoMessageService;

    @InjectMocks
    private OrderService orderService;

    private Member member;
    private Product product;
    private ProductOption productOption;
    private OrderRequestDto orderRequestDto;

    @BeforeEach
    void setUp() {
        member = new Member("test@test.com", "password", MemberRole.USER);
        member.setId(1L);
        
        product = new Product("테스트 상품", 10000, "test.jpg");
        product.setId(1L);
        
        // ProductOption을 Mock으로 생성
        productOption = mock(ProductOption.class);
        when(productOption.getId()).thenReturn(1L);
        when(productOption.getProduct()).thenReturn(product);
        
        orderRequestDto = new OrderRequestDto(1L, 2, "테스트 메시지");
    }

    @Test
    @DisplayName("정상적인 주문 생성 테스트")
    void createOrder_Success() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productOptionRepository.findById(1L)).thenReturn(Optional.of(productOption));
        when(wishRepository.findByMemberIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        
        Order savedOrder = new Order(member, productOption, 2, "테스트 메시지");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        doNothing().when(kakaoMessageService).sendOrderMessage(1L, savedOrder);

        // when
        OrderResponseDto result = orderService.createOrder(1L, orderRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOptionId()).isEqualTo(1L);
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getMessage()).isEqualTo("테스트 메시지");
        
        verify(productOption).subtract(2);
        verify(orderRepository).save(any(Order.class));
        verify(kakaoMessageService).sendOrderMessage(1L, savedOrder);
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 주문 시 예외 발생")
    void createOrder_NonExistentMember_ThrowsException() {
        // given
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(999L, orderRequestDto))
                .isInstanceOf(OrderException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 상품 옵션으로 주문 시 예외 발생")
    void createOrder_NonExistentProductOption_ThrowsException() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productOptionRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(1L, new OrderRequestDto(999L, 2, "테스트")))
                .isInstanceOf(OrderException.class)
                .hasMessage("존재하지 않는 상품 옵션입니다.");
    }

    @Test
    @DisplayName("재고보다 많은 수량으로 주문 시 예외 발생")
    void createOrder_InsufficientStock_ThrowsException() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productOptionRepository.findById(1L)).thenReturn(Optional.of(productOption));
        
        // ProductOption.subtract() 메서드가 예외를 던지도록 설정
        doThrow(new IllegalArgumentException("수량이 부족합니다.")).when(productOption).subtract(20);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(1L, new OrderRequestDto(1L, 20, "테스트")))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("주문 수량이 재고보다 많습니다");
    }

    @Test
    @DisplayName("위시리스트에 있는 상품 주문 시 위시리스트에서 삭제")
    void createOrder_WithWishlistItem_RemovesFromWishlist() {
        // given - 위시리스트 수량(2) <= 주문 수량(2)이므로 삭제됨
        Wish wish = new Wish(member, product, 2);  // 위시리스트 수량: 2
        wish.setId(1L);
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productOptionRepository.findById(1L)).thenReturn(Optional.of(productOption));
        when(wishRepository.findByMemberIdAndProductId(1L, 1L)).thenReturn(Optional.of(wish));
        
        Order savedOrder = new Order(member, productOption, 2, "테스트 메시지");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        doNothing().when(kakaoMessageService).sendOrderMessage(1L, savedOrder);

        // when
        orderService.createOrder(1L, orderRequestDto);  // 주문 수량: 2

        // then
        verify(wishRepository).delete(wish);
    }

    @Test
    @DisplayName("위시리스트에 있는 상품 주문 시 위시리스트에서 차감")
    void createOrder_WithWishlistItem_SubtractsFromWishlist() {
        // given - 위시리스트 수량(5) > 주문 수량(2)이므로 차감됨
        Wish wish = new Wish(member, product, 5);  // 위시리스트 수량: 5
        wish.setId(1L);
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productOptionRepository.findById(1L)).thenReturn(Optional.of(productOption));
        when(wishRepository.findByMemberIdAndProductId(1L, 1L)).thenReturn(Optional.of(wish));
        
        Order savedOrder = new Order(member, productOption, 2, "테스트 메시지");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        doNothing().when(kakaoMessageService).sendOrderMessage(1L, savedOrder);

        // when
        orderService.createOrder(1L, orderRequestDto);  // 주문 수량: 2

        // then
        verify(wishRepository, never()).delete(wish);
        // 위시리스트 수량이 5에서 3으로 차감되었는지 확인
        assertThat(wish.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("카카오톡 메시지 전송 실패 시에도 주문은 성공")
    void createOrder_KakaoMessageFailure_OrderStillSucceeds() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productOptionRepository.findById(1L)).thenReturn(Optional.of(productOption));
        when(wishRepository.findByMemberIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        
        Order savedOrder = new Order(member, productOption, 2, "테스트 메시지");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        doThrow(new RuntimeException("카카오 API 오류")).when(kakaoMessageService).sendOrderMessage(1L, savedOrder);

        // when
        OrderResponseDto result = orderService.createOrder(1L, orderRequestDto);

        // then
        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }
} 