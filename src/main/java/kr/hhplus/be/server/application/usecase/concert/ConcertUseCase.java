package kr.hhplus.be.server.application.usecase.concert;

import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ConcertUseCase {
    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;

    public ConcertUseCase(ConcertRepository concertRepository, SeatRepository seatRepository) {
        this.concertRepository = concertRepository;
        this.seatRepository = seatRepository;
    }

    public List<LocalDate> getAvailableDates(Long concertId) {
        return concertRepository.getAvailableDates(concertId);
    }

    public List<Integer> getAvailableSeats(Long concertScheduleId) {
        return seatRepository.findAvailableSeatsByConcertScheduleId(concertScheduleId)
                .stream()
                .map(seat -> seat.getSeatNumber())
                .toList();
    }
}
