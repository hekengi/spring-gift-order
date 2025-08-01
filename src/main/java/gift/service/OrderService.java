package gift.service;

import gift.dto.OrderRequestDto;
import gift.dto.OrderResponseDto;
import gift.entity.*;
import gift.exception.OrderException;
import gift.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductOptionRepository productOptionRepository;
    private final WishRepository wishRepository;
    private final MemberRepository memberRepository;
    
    public OrderService(OrderRepository orderRepository, 
                       ProductOptionRepository productOptionRepository,
                       WishRepository wishRepository,
                       MemberRepository memberRepository) {
        this.orderRepository = orderRepository;
        this.productOptionRepository = productOptionRepository;
        this.wishRepository = wishRepository;
        this.memberRepository = memberRepository;
    }
    
    public OrderResponseDto createOrder(Long memberId, OrderRequestDto requestDto) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new OrderException("존재하지 않는 회원입니다."));
        
        // 2. 상품 옵션 조회
        ProductOption productOption = productOptionRepository.findById(requestDto.getOptionId())
            .orElseThrow(() -> new OrderException("존재하지 않는 상품 옵션입니다."));
        
        // 3. 상품 옵션 수량 차감
        productOption.subtract(requestDto.getQuantity());
        
        // 4. 위시리스트에서 상품 수량 차감 (있는 경우)
        Product product = productOption.getProduct();
        wishRepository.findByMemberIdAndProductId(memberId, product.getId())
            .ifPresent(wish -> {
                int currentWishQuantity = wish.getQuantity();
                int orderQuantity = requestDto.getQuantity();
                
                if (currentWishQuantity <= orderQuantity) {
                    // 위시리스트 수량이 주문 수량보다 적거나 같으면 완전 삭제
                    wishRepository.delete(wish);
                } else {
                    // 위시리스트 수량이 주문 수량보다 많으면 차감만
                    wish.decreaseQuantity(orderQuantity);
                }
            });
        
        // 5. 주문 생성
        Order order = new Order(member, productOption, requestDto.getQuantity(), requestDto.getMessage());
        Order savedOrder = orderRepository.save(order);
        
        // 6. 응답 DTO 생성
        return new OrderResponseDto(
            savedOrder.getId(),
            savedOrder.getProductOption().getId(),
            savedOrder.getQuantity(),
            savedOrder.getOrderDateTime(),
            savedOrder.getMessage()
        );
    }
} 