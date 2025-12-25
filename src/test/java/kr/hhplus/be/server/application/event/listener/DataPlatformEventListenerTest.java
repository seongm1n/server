package kr.hhplus.be.server.application.event.listener;

import kr.hhplus.be.server.application.event.PaymentCompletedEvent;
import kr.hhplus.be.server.application.event.ReservationCompletedEvent;
import kr.hhplus.be.server.infrastructure.client.DataPlatformClient;
import kr.hhplus.be.server.infrastructure.client.dto.OrderDataDto;
import kr.hhplus.be.server.infrastructure.client.dto.ReservationDataDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@DisplayName("데이터 플랫폼 이벤트 리스너 테스트")
class DataPlatformEventListenerTest {

    @Mock
    private DataPlatformClient dataPlatformClient;

    private DataPlatformEventListener eventListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventListener = new DataPlatformEventListener(dataPlatformClient);
    }

    @Test
    @DisplayName("결제 완료 이벤트 수신 시 데이터 플랫폼에 주문 정보를 전송한다")
    void handlePaymentCompleted() {
        Long paymentId = 1L;
        String userId = "user123";
        Long reservationId = 100L;
        int amount = 50000;

        PaymentCompletedEvent event = new PaymentCompletedEvent(paymentId, userId, reservationId, amount);

        eventListener.handlePaymentCompleted(event);

        ArgumentCaptor<OrderDataDto> captor = ArgumentCaptor.forClass(OrderDataDto.class);
        verify(dataPlatformClient).sendOrderData(captor.capture());

        OrderDataDto orderData = captor.getValue();
        assertThat(orderData.getPaymentId()).isEqualTo(paymentId);
        assertThat(orderData.getUserId()).isEqualTo(userId);
        assertThat(orderData.getReservationId()).isEqualTo(reservationId);
        assertThat(orderData.getAmount()).isEqualTo(amount);
        assertThat(orderData.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("예약 확정 이벤트 수신 시 데이터 플랫폼에 예약 정보를 전송한다")
    void handleReservationCompleted() {
        Long reservationId = 100L;
        String userId = "user123";
        Long seatId = 10L;
        int price = 50000;

        ReservationCompletedEvent event = new ReservationCompletedEvent(reservationId, userId, seatId, price);

        eventListener.handleReservationCompleted(event);

        ArgumentCaptor<ReservationDataDto> captor = ArgumentCaptor.forClass(ReservationDataDto.class);
        verify(dataPlatformClient).sendReservationData(captor.capture());

        ReservationDataDto reservationData = captor.getValue();
        assertThat(reservationData.getReservationId()).isEqualTo(reservationId);
        assertThat(reservationData.getUserId()).isEqualTo(userId);
        assertThat(reservationData.getSeatId()).isEqualTo(seatId);
        assertThat(reservationData.getPrice()).isEqualTo(price);
        assertThat(reservationData.getCompletedAt()).isNotNull();
    }
}
