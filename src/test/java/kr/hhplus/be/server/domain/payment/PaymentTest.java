package kr.hhplus.be.server.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("결제 도메인 테스트")
class PaymentTest {

    @Test
    @DisplayName("결제를 생성할 수 있다")
    void createPayment() {
        String userId = "user123";
        Long reservationId = 1L;
        int amount = 50000;

        Payment payment = Payment.create(userId, reservationId, amount);

        assertThat(payment.getUserId()).isEqualTo(userId);
        assertThat(payment.getReservationId()).isEqualTo(reservationId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("결제를 완료할 수 있다")
    void completePayment() {
        Payment payment = Payment.create("user123", 1L, 50000);

        payment.complete();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getCompletedAt()).isNotNull();
    }

}
