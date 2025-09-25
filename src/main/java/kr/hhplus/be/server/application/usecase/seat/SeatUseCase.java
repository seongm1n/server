package kr.hhplus.be.server.application.usecase.seat;

import kr.hhplus.be.server.application.dto.SeatResult;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatUseCase {
    
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public List<SeatResult> getAvailableSeats(Long concertScheduleId) {
        List<Seat> seats = seatRepository.findAvailableSeatsByConcertScheduleId(concertScheduleId);
        return seats.stream()
                .map(SeatResult::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SeatResult getSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));
        return SeatResult.from(seat);
    }

    @Transactional
    public SeatResult reserveSeat(Long seatId, String userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));
        
        seat.reserve(userId);
        Seat savedSeat = seatRepository.save(seat);
        return SeatResult.from(savedSeat);
    }

    @Transactional
    public SeatResult confirmSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));
        
        seat.confirm();
        Seat savedSeat = seatRepository.save(seat);
        return SeatResult.from(savedSeat);
    }

    @Transactional
    public SeatResult releaseSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));
        
        seat.release();
        Seat savedSeat = seatRepository.save(seat);
        return SeatResult.from(savedSeat);
    }
}
