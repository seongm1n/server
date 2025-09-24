package kr.hhplus.be.server.infrastructure.persistence.concert;

import kr.hhplus.be.server.domain.concert.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {
    
    private final ConcertScheduleJpaRepository concertScheduleJpaRepository;
    
    @Override
    public List<LocalDate> getAvailableDates(Long concertId) {
        return concertScheduleJpaRepository.findAvailableDatesByConcertId(concertId);
    }
}
