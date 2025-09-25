package kr.hhplus.be.server.application.dto;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SeatResult {
    private Long id;
    private Long concertScheduleId;
    private int seatNumber;
    private int price;
    private SeatStatus status;
    private String reservedBy;
    private LocalDateTime reservedAt;

    public static SeatResult from(Seat seat) {
        return new SeatResult(
            seat.getId(),
            seat.getConcertScheduleId(),
            seat.getSeatNumber(),
            seat.getPrice(),
            seat.getStatus(),
            seat.getReservedBy(),
            seat.getReservedAt()
        );
    }
}