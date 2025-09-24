package kr.hhplus.be.server.infrastructure.persistence.queue;

import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.infrastructure.persistence.queue.QueueTokenEntity;
import kr.hhplus.be.server.infrastructure.persistence.queue.QueueTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class QueueTokenRepositoryImpl implements QueueTokenRepository {
    
    private final QueueTokenJpaRepository jpaRepository;
    
    @Override
    public QueueToken save(QueueToken token) {
        if (token.getId() != null) {
            Optional<QueueTokenEntity> existingEntity = jpaRepository.findById(token.getId());
            if (existingEntity.isPresent()) {
                QueueTokenEntity entity = existingEntity.get();
                entity.updateStatus(token.getStatus(), token.getActivatedAt(), token.getExpiresAt());
                return jpaRepository.save(entity).toDomain();
            }
        }
        
        QueueTokenEntity entity = QueueTokenEntity.from(token);
        return jpaRepository.save(entity).toDomain();
    }
    
    @Override
    public void saveAll(List<QueueToken> tokens) {
        List<QueueTokenEntity> entities = tokens.stream()
                .map(QueueTokenEntity::from)
                .collect(Collectors.toList());
        jpaRepository.saveAll(entities);
    }
    
    @Override
    public Optional<QueueToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(QueueTokenEntity::toDomain);
    }
    
    @Override
    public Optional<QueueToken> findActiveByUserId(String userId) {
        return jpaRepository.findActiveByUserId(userId)
                .map(QueueTokenEntity::toDomain);
    }
    
    @Override
    public Long countWaitingTokens() {
        return jpaRepository.countByStatus(QueueStatus.WAITING);
    }
    
    @Override
    public Long countActiveTokens() {
        return jpaRepository.countByStatus(QueueStatus.ACTIVE);
    }
    
    @Override
    public List<QueueToken> getWaitingTokensToActivate(int limit) {
        return jpaRepository.findWaitingTokensOrderByPosition(limit)
                .stream()
                .map(QueueTokenEntity::toDomain)
                .collect(Collectors.toList());
    }
}
