package kr.hhplus.be.server.infrastructure.client.dto;

import java.time.LocalDateTime;

public class OrderDataDto {
    private Long paymentId;
    private String userId;
    private Long reservationId;
    private int amount;
    private LocalDateTime completedAt;

    public OrderDataDto() {
    }

    public OrderDataDto(Long paymentId, String userId, Long reservationId, int amount, LocalDateTime completedAt) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.completedAt = completedAt;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "OrderDataDto{" +
                "paymentId=" + paymentId +
                ", userId='" + userId + '\'' +
                ", reservationId=" + reservationId +
                ", amount=" + amount +
                ", completedAt=" + completedAt +
                '}';
    }
}
