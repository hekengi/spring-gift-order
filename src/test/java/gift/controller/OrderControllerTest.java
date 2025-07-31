package gift.controller;

import gift.dto.OrderRequestDto;
import gift.dto.OrderResponseDto;
import gift.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private OrderRequestDto orderRequestDto;
    private OrderResponseDto orderResponseDto;

    @BeforeEach
    void setUp() {
        orderRequestDto = new OrderRequestDto(1L, 2, "테스트 메시지");
        orderResponseDto = new OrderResponseDto(1L, 1L, 2, LocalDateTime.now(), "테스트 메시지");
    }

    @Test
    @DisplayName("정상 주문 생성 테스트")
    void createOrder_Success() {
        // given
        when(orderService.createOrder(eq(1L), any(OrderRequestDto.class)))
                .thenReturn(orderResponseDto);

        // when
        ResponseEntity<OrderResponseDto> response = orderController.createOrder(1L, orderRequestDto);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getOptionId()).isEqualTo(1L);
        assertThat(response.getBody().getQuantity()).isEqualTo(2);
        assertThat(response.getBody().getMessage()).isEqualTo("테스트 메시지");
    }

} 