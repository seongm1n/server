package kr.hhplus.be.server.infrastructure.persistence.seat;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepository {
    
    private final SeatJpaRepository jpaRepository;
    
    @Override
    public Optional<Seat> findById(Long id) {
        return jpaRepository.findById(id)
                .map(SeatEntity::toDomain);
    }
    
    @Override
    public Seat save(Seat seat) {
        if (seat.getId() != null) {
            Optional<SeatEntity> existingEntity = jpaRepository.findById(seat.getId());
            if (existingEntity.isPresent()) {
                SeatEntity entity = existingEntity.get();
                entity.updateReservation(seat.getStatus(), seat.getReservedBy(), seat.getReservedAt());
                return jpaRepository.save(entity).toDomain();
            }
        }
        
        SeatEntity entity = SeatEntity.from(seat);
        return jpaRepository.save(entity).toDomain();
    }
    
    @Override
    public List<Seat> findAvailableSeatsByConcertScheduleId(Long concertScheduleId) {
        return jpaRepository.findByConcertScheduleIdAndStatus(concertScheduleId, SeatStatus.AVAILABLE)
                .stream()
                .map(SeatEntity::toDomain)
                .collect(Collectors.toList());
    }
}
