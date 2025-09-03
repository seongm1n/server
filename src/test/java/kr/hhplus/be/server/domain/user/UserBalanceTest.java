package kr.hhplus.be.server.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("사용자 잔액 도메인 테스트")
class UserBalanceTest {

    @Test
    @DisplayName("사용자 잔액을 생성할 수 있다")
    void createUserBalance() {
        String userId = "user123";
        int initialBalance = 100000;

        UserBalance userBalance = UserBalance.create(userId, initialBalance);

        assertThat(userBalance.getUserId()).isEqualTo(userId);
        assertThat(userBalance.getBalance()).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("잔액을 충전할 수 있다")
    void chargeBalance() {
        UserBalance userBalance = UserBalance.create("user123", 50000);
        int chargeAmount = 30000;

        userBalance.charge(chargeAmount);

        assertThat(userBalance.getBalance()).isEqualTo(80000);
    }

    @Test
    @DisplayName("잔액을 사용할 수 있다")
    void useBalance() {
        UserBalance userBalance = UserBalance.create("user123", 100000);
        int useAmount = 50000;

        userBalance.use(useAmount);

        assertThat(userBalance.getBalance()).isEqualTo(50000);
    }

    @Test
    @DisplayName("잔액이 부족하면 사용할 수 없다")
    void cannotUseIfInsufficientBalance() {
        UserBalance userBalance = UserBalance.create("user123", 30000);
        int useAmount = 50000;

        assertThatThrownBy(() -> userBalance.use(useAmount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("잔액이 부족합니다.");
    }

    @Test
    @DisplayName("음수 금액은 충전할 수 없다")
    void cannotChargeNegativeAmount() {
        UserBalance userBalance = UserBalance.create("user123", 50000);

        assertThatThrownBy(() -> userBalance.charge(-10000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0보다 커야 합니다.");
    }

}
