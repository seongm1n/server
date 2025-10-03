package kr.hhplus.be.server.infrastructure.persistence.seat;

import kr.hhplus.be.server.domain.seat.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {
    
    List<SeatEntity> findByConcertScheduleIdAndStatusOrderBySeatNumber(Long concertScheduleId, SeatStatus status);
    
    List<SeatEntity> findByStatusAndReservedAtBefore(SeatStatus status, LocalDateTime expirationTime);
}
