package kr.hhplus.be.server.infrastructure.client.dto;

import java.time.LocalDateTime;

public class ReservationDataDto {
    private Long reservationId;
    private String userId;
    private Long seatId;
    private int price;
    private LocalDateTime completedAt;

    public ReservationDataDto() {
    }

    public ReservationDataDto(Long reservationId, String userId, Long seatId, int price, LocalDateTime completedAt) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.seatId = seatId;
        this.price = price;
        this.completedAt = completedAt;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "ReservationDataDto{" +
                "reservationId=" + reservationId +
                ", userId='" + userId + '\'' +
                ", seatId=" + seatId +
                ", price=" + price +
                ", completedAt=" + completedAt +
                '}';
    }
}
