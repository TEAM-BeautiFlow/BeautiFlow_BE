package com.beautiflow.global.common.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationLockManager {

    private final RedissonClient redissonClient;
    private final Map<Long, LockInfo> lockMap = new ConcurrentHashMap<>();

    public boolean tryLock(Long tempReservationId, String lockName) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockName);
        boolean locked = lock.tryLock(5, TimeUnit.SECONDS);
        if (locked) {
            long currentThreadId = Thread.currentThread().getId();
            lockMap.put(tempReservationId, new LockInfo(lock, currentThreadId));
        }
        return locked;
    }

    public void unlock(String lockName) {
        RLock lock = redissonClient.getLock(lockName);
        if (lock.isLocked()) {
            lock.forceUnlock();
        }
    }

}