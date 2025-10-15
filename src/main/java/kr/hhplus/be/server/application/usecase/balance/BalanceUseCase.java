package kr.hhplus.be.server.application.usecase.balance;

import kr.hhplus.be.server.application.dto.BalanceResult;
import kr.hhplus.be.server.domain.user.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BalanceUseCase {
    private final UserBalanceRepository userBalanceRepository;

    public BalanceUseCase(UserBalanceRepository userBalanceRepository) {
        this.userBalanceRepository = userBalanceRepository;
    }

    @Retryable(
        retryFor = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
    )
    public BalanceResult charge(String userId, int amount) {
        UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                .orElse(UserBalance.create(userId, 0));

        userBalance.charge(amount);
        UserBalance saved = userBalanceRepository.save(userBalance);

        return new BalanceResult(saved.getBalance());
    }

    @Retryable(
        retryFor = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
    )
    public BalanceResult use(String userId, int amount) {
        UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        userBalance.use(amount);
        UserBalance saved = userBalanceRepository.save(userBalance);

        return new BalanceResult(saved.getBalance());
    }

    public BalanceResult getBalance(String userId) {
        UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                .orElse(UserBalance.create(userId, 0));
        
        return new BalanceResult(userBalance.getBalance());
    }
}
