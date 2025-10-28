package kr.hhplus.be.server.application.usecase.balance;

import kr.hhplus.be.server.application.dto.BalanceResult;
import kr.hhplus.be.server.domain.user.*;
import kr.hhplus.be.server.infrastructure.lock.DistributedLock;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class BalanceUseCase {
    private final UserBalanceRepository userBalanceRepository;
    private final DistributedLock distributedLock;

    public BalanceUseCase(UserBalanceRepository userBalanceRepository, DistributedLock distributedLock) {
        this.userBalanceRepository = userBalanceRepository;
        this.distributedLock = distributedLock;
    }

    @CacheEvict(value = "balance", key = "#userId")
    public BalanceResult charge(String userId, int amount) {
        String lockKey = "balance:charge:" + userId;

        boolean lockAcquired = tryLockWithRetry(lockKey, 3, 100);
        if (!lockAcquired) {
            throw new IllegalStateException("잔액 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                    .orElse(UserBalance.create(userId, 0));

            userBalance.charge(amount);
            UserBalance saved = userBalanceRepository.save(userBalance);

            return new BalanceResult(saved.getBalance());
        } finally {
            distributedLock.unlock(lockKey);
        }
    }

    @CacheEvict(value = "balance", key = "#userId")
    public BalanceResult use(String userId, int amount) {
        String lockKey = "balance:use:" + userId;

        boolean lockAcquired = tryLockWithRetry(lockKey, 3, 100);
        if (!lockAcquired) {
            throw new IllegalStateException("잔액 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            userBalance.use(amount);
            UserBalance saved = userBalanceRepository.save(userBalance);

            return new BalanceResult(saved.getBalance());
        } finally {
            distributedLock.unlock(lockKey);
        }
    }

    @Cacheable(value = "balance", key = "#userId")
    public BalanceResult getBalance(String userId) {
        UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                .orElse(UserBalance.create(userId, 0));

        return new BalanceResult(userBalance.getBalance());
    }

    private boolean tryLockWithRetry(String lockKey, int maxRetries, long delayMillis) {
        for (int i = 0; i < maxRetries; i++) {
            if (distributedLock.tryLock(lockKey, 5, TimeUnit.SECONDS)) {
                return true;
            }

            if (i < maxRetries - 1) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }
}
