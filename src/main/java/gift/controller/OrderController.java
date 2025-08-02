package gift.controller;

import gift.config.LoginMember;
import gift.dto.OrderRequestDto;
import gift.dto.OrderResponseDto;
import gift.service.KakaoOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final KakaoOrderService kakaoOrderService;
    
    public OrderController(KakaoOrderService kakaoOrderService) {
        this.kakaoOrderService = kakaoOrderService;
    }
    
    //주문 생성
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @LoginMember Long memberId,
            @Valid @RequestBody OrderRequestDto requestDto) {
        
        OrderResponseDto orderResponse = kakaoOrderService.createOrderWithMessage(memberId, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderResponse);
    }
} 