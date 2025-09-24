package kr.hhplus.be.server.infrastructure.persistence.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    
    private final PaymentJpaRepository jpaRepository;
    
    @Override
    public Payment save(Payment payment) {
        if (payment.getId() != null) {
            Optional<PaymentEntity> existingEntity = jpaRepository.findById(payment.getId());
            if (existingEntity.isPresent()) {
                PaymentEntity entity = existingEntity.get();
                entity.updateStatus(payment.getStatus(), payment.getCompletedAt(), payment.getFailureReason());
                return jpaRepository.save(entity).toDomain();
            }
        }
        
        PaymentEntity entity = PaymentEntity.from(payment);
        return jpaRepository.save(entity).toDomain();
    }
}