package kr.hhplus.be.server.infrastructure.persistence.concert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertScheduleEntity, Long> {
    
    @Query("SELECT DISTINCT cs.concertDate FROM ConcertScheduleEntity cs WHERE cs.concertId = :concertId")
    List<LocalDate> findAvailableDatesByConcertId(@Param("concertId") Long concertId);
}
