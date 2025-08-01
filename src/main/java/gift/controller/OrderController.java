package gift.controller;

import gift.config.LoginMember;
import gift.dto.OrderRequestDto;
import gift.dto.OrderResponseDto;
import gift.entity.Order;
import gift.repository.OrderRepository;
import gift.service.KakaoMessageService;
import gift.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    
    private final OrderService orderService;
    private final KakaoMessageService kakaoMessageService;
    private final OrderRepository orderRepository;
    
    public OrderController(OrderService orderService, 
                         KakaoMessageService kakaoMessageService,
                         OrderRepository orderRepository) {
        this.orderService = orderService;
        this.kakaoMessageService = kakaoMessageService;
        this.orderRepository = orderRepository;
    }
    
    //주문 생성
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @LoginMember Long memberId,
            @Valid @RequestBody OrderRequestDto requestDto) {
        
        // 1. 주문 생성 (트랜잭션 내에서 DB 작업만)
        OrderResponseDto orderResponse = orderService.createOrder(memberId, requestDto);
        
        // 2. 메시지 전송 (트랜잭션 외부에서 API 호출)
        try {
            Order order = orderRepository.findById(orderResponse.getId())
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
            
            kakaoMessageService.sendOrderMessage(memberId, order);
            log.info("카카오톡 메시지 전송 성공 - 주문 ID: {}", orderResponse.getId());
            
        } catch (Exception e) {
            log.error("카카오톡 메시지 전송 실패 - 주문 ID: {}, 에러: {}", 
                     orderResponse.getId(), e.getMessage(), e);
            // 메시지 전송 실패는 주문 생성에 영향을 주지 않음
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderResponse);
    }
} 