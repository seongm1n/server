package kr.hhplus.be.server.application.event;

import java.time.LocalDateTime;

public class ReservationCompletedEvent {
    private final Long reservationId;
    private final String userId;
    private final Long seatId;
    private final int price;
    private final LocalDateTime completedAt;

    public ReservationCompletedEvent(Long reservationId, String userId, Long seatId, int price) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.seatId = seatId;
        this.price = price;
        this.completedAt = LocalDateTime.now();
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public int getPrice() {
        return price;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
