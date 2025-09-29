package kr.hhplus.be.server.application.usecase.seat;

import kr.hhplus.be.server.application.dto.SeatResult;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    // 이 메서드들은 ReservationUseCase와 PaymentUseCase에서 직접 처리함
    
    @Transactional
    public void expireTemporaryReservations() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(5);
        List<Seat> expiredSeats = seatRepository.findExpiredTemporaryReservations(expirationTime);
        
        for (Seat seat : expiredSeats) {
            seat.release();
            seatRepository.save(seat);
        }
    }
}
