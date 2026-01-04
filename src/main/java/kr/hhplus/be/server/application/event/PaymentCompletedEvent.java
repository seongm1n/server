package kr.hhplus.be.server.application.event;

import java.time.LocalDateTime;

public class PaymentCompletedEvent {
    private final Long paymentId;
    private final String userId;
    private final Long reservationId;
    private final int amount;
    private final LocalDateTime completedAt;

    public PaymentCompletedEvent(Long paymentId, String userId, Long reservationId, int amount) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.completedAt = LocalDateTime.now();
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getUserId() {
        return userId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public int getAmount() {
        return amount;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
