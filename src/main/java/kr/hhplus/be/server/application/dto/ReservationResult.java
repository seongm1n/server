package kr.hhplus.be.server.application.dto;

import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationResult {
    private Long id;
    private String userId;
    private Long seatId;
    private int price;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    public ReservationResult(Long reservationId, int price, LocalDateTime expiresAt) {
        this.id = reservationId;
        this.price = price;
        this.expiresAt = expiresAt;
    }
    
    public static ReservationResult from(Reservation reservation) {
        return new ReservationResult(
            reservation.getId(),
            reservation.getUserId(),
            reservation.getSeatId(),
            reservation.getPrice(),
            reservation.getStatus(),
            reservation.getCreatedAt(),
            null
        );
    }
    
    public Long getReservationId() {
        return this.id;
    }
}
