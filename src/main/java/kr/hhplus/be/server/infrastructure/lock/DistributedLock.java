package kr.hhplus.be.server.infrastructure.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {
    boolean tryLock(String key, long timeout, TimeUnit unit);
    void unlock(String key);

    default boolean tryLockWithRetry(String lockKey, long lockTimeout, TimeUnit timeUnit, int maxRetries, long retryDelayMillis) {
        for (int i = 0; i < maxRetries; i++) {
            if (tryLock(lockKey, lockTimeout, timeUnit)) {
                return true;
            }

            if (i < maxRetries - 1) {
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }
}
