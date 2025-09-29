package kr.hhplus.be.server.infrastructure.persistence.seat;

import kr.hhplus.be.server.domain.seat.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {
    
    @Query("SELECT s FROM SeatEntity s WHERE s.concertScheduleId = :scheduleId AND s.status = :status ORDER BY s.seatNumber")
    List<SeatEntity> findByConcertScheduleIdAndStatus(@Param("scheduleId") Long scheduleId, @Param("status") SeatStatus status);
    
    List<SeatEntity> findByStatusAndReservedAtBefore(SeatStatus status, LocalDateTime expirationTime);
}
