package kr.hhplus.be.server.domain.seat;

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

    public static Seat create(Long concertScheduleId, int seatNumber, int price) {
        return new Seat(null, concertScheduleId, seatNumber, price, SeatStatus.AVAILABLE, null, null);
    }

    public void reserve(String userId) {
        if (status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }
        
        this.status = SeatStatus.TEMPORARILY_RESERVED;
        this.reservedBy = userId;
        this.reservedAt = LocalDateTime.now();
    }

    public void confirm() {
        this.status = SeatStatus.CONFIRMED;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
        this.reservedBy = null;
        this.reservedAt = null;
    }
}
