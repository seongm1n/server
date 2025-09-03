package kr.hhplus.be.server.domain.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Seat {
    private Long id;
    private Long concertScheduleId;
    private int seatNumber;
    private int price;
    private SeatStatus status;
    private String reservedBy;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;

    public static Seat create(Long concertScheduleId, int seatNumber, int price) {
        return new Seat(null, concertScheduleId, seatNumber, price, SeatStatus.AVAILABLE, null, null, null);
    }

    public void reserve(String userId) {
        if (status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }
        
        this.status = SeatStatus.TEMPORARILY_RESERVED;
        this.reservedBy = userId;
        this.reservedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(5);
    }

    public void confirm() {
        this.status = SeatStatus.CONFIRMED;
    }


    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
