package kr.hhplus.be.server.infrastructure.client;

import kr.hhplus.be.server.infrastructure.client.dto.OrderDataDto;
import kr.hhplus.be.server.infrastructure.client.dto.ReservationDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class MockDataPlatformClient implements DataPlatformClient {
    private static final Logger log = LoggerFactory.getLogger(MockDataPlatformClient.class);

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendOrderData(OrderDataDto orderData) {
        try {
            log.info("데이터 플랫폼 전송 - 주문 정보: {}", orderData);
            log.info("주문 데이터 전송 성공: paymentId={}", orderData.getPaymentId());
        } catch (Exception e) {
            log.error("주문 데이터 전송 실패", e);
            throw e;
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendReservationData(ReservationDataDto reservationData) {
        try {
            log.info("데이터 플랫폼 전송 - 예약 정보: {}", reservationData);
            log.info("예약 데이터 전송 성공: reservationId={}", reservationData.getReservationId());
        } catch (Exception e) {
            log.error("예약 데이터 전송 실패", e);
            throw e;
        }
    }
}
