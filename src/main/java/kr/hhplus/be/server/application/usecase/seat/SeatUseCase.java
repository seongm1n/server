package kr.hhplus.be.server.application.usecase.seat;

import kr.hhplus.be.server.application.dto.SeatResult;
import kr.hhplus.be.server.application.event.SeatReservedEvent;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatUseCase {

    private final SeatRepository seatRepository;

    @Cacheable(value = "seats", key = "#concertScheduleId")
    @Transactional(readOnly = true)
    public List<SeatResult> getAvailableSeats(Long concertScheduleId) {
        List<Seat> seats = seatRepository.findAvailableSeatsByConcertScheduleId(concertScheduleId);
        return seats.stream()
                .map(SeatResult::from)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "seats", key = "#event.concertScheduleId")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSeatReservedEvent(SeatReservedEvent event) {
    }
    
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
