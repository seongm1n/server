package kr.hhplus.be.server.domain.seat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatRepository {
    Optional<Seat> findById(Long id);
    Seat save(Seat seat);
    List<Seat> findAvailableSeatsByConcertScheduleId(Long concertScheduleId);
    List<Seat> findExpiredTemporaryReservations(LocalDateTime expirationTime);
}
