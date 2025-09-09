package kr.hhplus.be.server.domain.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Reservation {
    private Long id;
    private String userId;
    private Long seatId;
    private int price;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    public static Reservation create(String userId, Long seatId, int price) {
        LocalDateTime now = LocalDateTime.now();
        return new Reservation(null, userId, seatId, price, 
                              ReservationStatus.PENDING, now);
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
