package gift.controller;

import gift.config.LoginMember;
import gift.dto.OrderRequestDto;
import gift.dto.OrderResponseDto;
import gift.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    //주문 생성
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @LoginMember Long memberId,
            @Valid @RequestBody OrderRequestDto requestDto) {
        
        OrderResponseDto responseDto = orderService.createOrder(memberId, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseDto);
    }
} 