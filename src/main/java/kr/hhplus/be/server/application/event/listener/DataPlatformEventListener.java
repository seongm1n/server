package kr.hhplus.be.server.application.event.listener;

import kr.hhplus.be.server.application.event.PaymentCompletedEvent;
import kr.hhplus.be.server.application.event.ReservationCompletedEvent;
import kr.hhplus.be.server.infrastructure.client.DataPlatformClient;
import kr.hhplus.be.server.infrastructure.client.dto.OrderDataDto;
import kr.hhplus.be.server.infrastructure.client.dto.ReservationDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class DataPlatformEventListener {
    private static final Logger log = LoggerFactory.getLogger(DataPlatformEventListener.class);

    private final DataPlatformClient dataPlatformClient;

    public DataPlatformEventListener(DataPlatformClient dataPlatformClient) {
        this.dataPlatformClient = dataPlatformClient;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 수신: paymentId={}", event.getPaymentId());

        OrderDataDto orderData = new OrderDataDto(
                event.getPaymentId(),
                event.getUserId(),
                event.getReservationId(),
                event.getAmount(),
                event.getCompletedAt()
        );

        dataPlatformClient.sendOrderData(orderData);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCompleted(ReservationCompletedEvent event) {
        log.info("예약 확정 이벤트 수신: reservationId={}", event.getReservationId());

        ReservationDataDto reservationData = new ReservationDataDto(
                event.getReservationId(),
                event.getUserId(),
                event.getSeatId(),
                event.getPrice(),
                event.getCompletedAt()
        );

        dataPlatformClient.sendReservationData(reservationData);
    }
}
