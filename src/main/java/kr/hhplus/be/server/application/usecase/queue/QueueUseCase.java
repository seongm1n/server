package kr.hhplus.be.server.application.usecase.queue;

import kr.hhplus.be.server.application.dto.QueueTokenResult;
import kr.hhplus.be.server.domain.queue.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueUseCase {
    private final QueueTokenRepository queueTokenRepository;

    public QueueUseCase(QueueTokenRepository queueTokenRepository) {
        this.queueTokenRepository = queueTokenRepository;
    }

    public QueueTokenResult issueToken(String userId) {
        Long waitingCount = queueTokenRepository.countWaitingTokens();
        int position = waitingCount.intValue() + 1;
        
        QueueToken token = QueueToken.create(userId, position);
        QueueToken savedToken = queueTokenRepository.save(token);
        
        return new QueueTokenResult(savedToken.getToken(), savedToken.getPosition(), 
                                   savedToken.getStatus().name());
    }

    public QueueTokenResult getQueueStatus(String token) {
        QueueToken queueToken = queueTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));
        
        return new QueueTokenResult(queueToken.getToken(), queueToken.getPosition(), 
                                   queueToken.getStatus().name());
    }

    public void activateWaitingTokens(int maxActiveTokens) {
        Long activeCount = queueTokenRepository.countActiveTokens();
        int slotsAvailable = maxActiveTokens - activeCount.intValue();
        
        if (slotsAvailable > 0) {
            List<QueueToken> tokensToActivate = queueTokenRepository.getWaitingTokensToActivate(slotsAvailable);
            tokensToActivate.forEach(QueueToken::activate);
            queueTokenRepository.saveAll(tokensToActivate);
        }
    }
}
