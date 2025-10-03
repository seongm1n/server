package kr.hhplus.be.server.infrastructure.persistence.reservation;

import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {
    
    private final ReservationJpaRepository jpaRepository;
    
    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() != null) {
            Optional<ReservationEntity> existingEntity = jpaRepository.findById(reservation.getId());
            if (existingEntity.isPresent()) {
                ReservationEntity entity = existingEntity.get();
                entity.updateStatus(reservation.getStatus());
                return jpaRepository.save(entity).toDomain();
            }
        }
        
        ReservationEntity entity = ReservationEntity.from(reservation);
        return jpaRepository.save(entity).toDomain();
    }
    
    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaRepository.findById(id)
                .map(ReservationEntity::toDomain);
    }
}
