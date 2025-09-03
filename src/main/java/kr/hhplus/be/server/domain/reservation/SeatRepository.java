package kr.hhplus.be.server.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface SeatRepository {
    Optional<Seat> findById(Long id);
    Seat save(Seat seat);
    List<Seat> findAvailableSeatsByScheduleId(Long scheduleId);
}
