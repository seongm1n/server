package kr.hhplus.be.server.infrastructure.persistence.seat;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {

    List<SeatEntity> findByConcertScheduleIdAndStatusOrderBySeatNumber(Long concertScheduleId, SeatStatus status);

    List<SeatEntity> findByStatusAndReservedAtBefore(SeatStatus status, LocalDateTime expirationTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatEntity s WHERE s.id = :id")
    Optional<SeatEntity> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatEntity s WHERE s.status = :status AND s.reservedAt < :expirationTime")
    List<SeatEntity> findExpiredWithLock(@Param("status") SeatStatus status, @Param("expirationTime") LocalDateTime expirationTime);
}
