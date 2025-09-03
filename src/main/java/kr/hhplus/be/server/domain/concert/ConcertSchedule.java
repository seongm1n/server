package kr.hhplus.be.server.domain.concert;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ConcertSchedule {
    private Long id;
    private Long concertId;
    private LocalDate date;
    private int totalSeats;
    private LocalDateTime createdAt;

    public static ConcertSchedule create(Long concertId, LocalDate date, int totalSeats) {
        return new ConcertSchedule(null, concertId, date, totalSeats, LocalDateTime.now());
    }
}
