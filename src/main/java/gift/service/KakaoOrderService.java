package gift.service;

import gift.dto.OrderRequestDto;
import gift.dto.OrderResponseDto;
import gift.entity.Order;
import gift.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KakaoOrderService {

    private static final Logger log = LoggerFactory.getLogger(KakaoOrderService.class);

    private final OrderService orderService;
    private final KakaoMessageService kakaoMessageService;
    private final OrderRepository orderRepository;

    public KakaoOrderService(OrderService orderService, 
                           KakaoMessageService kakaoMessageService,
                           OrderRepository orderRepository) {
        this.orderService = orderService;
        this.kakaoMessageService = kakaoMessageService;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponseDto createOrderWithMessage(Long memberId, OrderRequestDto requestDto) {
        // 1. 주문 생성 (트랜잭션 내에서 DB 작업만)
        OrderResponseDto orderResponse = orderService.createOrder(memberId, requestDto);

        // 2. 메시지 전송 (트랜잭션 외부에서 API 호출)
        sendKakaoMessage(memberId, orderResponse.getId());

        return orderResponse;
    }

    private void sendKakaoMessage(Long memberId, Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

            kakaoMessageService.sendOrderMessage(memberId, order);
            log.info("카카오톡 메시지 전송 성공 - 주문 ID: {}", orderId);

        } catch (Exception e) {
            log.error("카카오톡 메시지 전송 실패 - 주문 ID: {}, 에러: {}", 
                     orderId, e.getMessage(), e);
            // 메시지 전송 실패는 주문 생성에 영향을 주지 않음
        }
    }
} 