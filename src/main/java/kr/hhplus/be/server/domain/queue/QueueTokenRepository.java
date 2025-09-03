package kr.hhplus.be.server.domain.queue;

import java.util.List;
import java.util.Optional;

public interface QueueTokenRepository {
    QueueToken save(QueueToken token);
    void saveAll(List<QueueToken> tokens);
    Optional<QueueToken> findByToken(String token);
    Long countWaitingTokens();
    Long countActiveTokens();
    List<QueueToken> getWaitingTokensToActivate(int limit);
}
