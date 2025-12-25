package kr.hhplus.be.server.infrastructure.client;

import kr.hhplus.be.server.infrastructure.client.dto.OrderDataDto;
import kr.hhplus.be.server.infrastructure.client.dto.ReservationDataDto;

public interface DataPlatformClient {
    void sendOrderData(OrderDataDto orderData);
    void sendReservationData(ReservationDataDto reservationData);
}
