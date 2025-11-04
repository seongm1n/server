package kr.hhplus.be.server.infrastructure.lock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisDistributedLock implements DistributedLock {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "lock:";

    public RedisDistributedLock(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        String lockKey = LOCK_PREFIX + key;

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", timeout, unit);

        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public void unlock(String key) {
        redisTemplate.delete(LOCK_PREFIX + key);
    }
}
