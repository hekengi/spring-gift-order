package gift.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WishTest {

    @Test
    @DisplayName("위시리스트 수량 감소시킬 수 있음")
    void decreaseQuantity_Success() {
        // given
        Wish wish = new Wish();
        wish.setQuantity(10);

        // when
        wish.decreaseQuantity(3);

        // then
        assertThat(wish.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("위시리스트 수량을 0으로 만들 수 있음")
    void decreaseQuantity_ToZero() {
        // given
        Wish wish = new Wish();
        wish.setQuantity(5);

        // when
        wish.decreaseQuantity(5);

        // then
        assertThat(wish.getQuantity()).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("감소 수량이 1 미만이면 예외가 발생")
    void decreaseQuantity_InvalidAmount_ThrowsException(int invalidAmount) {
        // given
        Wish wish = new Wish();
        wish.setQuantity(10);

        // when & then
        assertThatThrownBy(() -> wish.decreaseQuantity(invalidAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("감소 수량은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("감소할 수량이 현재 수량보다 많으면 예외가 발생")
    void decreaseQuantity_InsufficientQuantity_ThrowsException() {
        // given
        Wish wish = new Wish();
        wish.setQuantity(5);

        // when & then
        assertThatThrownBy(() -> wish.decreaseQuantity(10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("위시리스트 수량이 부족합니다.");
    }

    @Test
    @DisplayName("감소할 수량이 현재 수량과 같으면 정상적으로 처리")
    void decreaseQuantity_ExactQuantity_Success() {
        // given
        Wish wish = new Wish();
        wish.setQuantity(5);

        // when
        wish.decreaseQuantity(5);

        // then
        assertThat(wish.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("위시리스트 수량이 0일 때 감소시키려 하면 예외가 발생")
    void decreaseQuantity_ZeroQuantity_ThrowsException() {
        // given
        Wish wish = new Wish();
        wish.setQuantity(0);

        // when & then
        assertThatThrownBy(() -> wish.decreaseQuantity(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("위시리스트 수량이 부족합니다.");
    }
} 