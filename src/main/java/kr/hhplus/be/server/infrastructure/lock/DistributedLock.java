package kr.hhplus.be.server.infrastructure.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {
    boolean tryLock(String key, long timeout, TimeUnit unit);
    void unlock(String key);
}
