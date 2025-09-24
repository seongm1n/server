package kr.hhplus.be.server.infrastructure.persistence.queue;

import kr.hhplus.be.server.domain.queue.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QueueTokenJpaRepository extends JpaRepository<QueueTokenEntity, Long> {
    
    Optional<QueueTokenEntity> findByToken(String token);
    
    @Query("SELECT q FROM QueueTokenEntity q WHERE q.userId = :userId AND q.status = 'ACTIVE'")
    Optional<QueueTokenEntity> findActiveByUserId(@Param("userId") String userId);
    
    Long countByStatus(QueueStatus status);
    
    @Query("SELECT q FROM QueueTokenEntity q WHERE q.status = 'WAITING' ORDER BY q.position ASC LIMIT :limit")
    List<QueueTokenEntity> findWaitingTokensOrderByPosition(@Param("limit") int limit);
}