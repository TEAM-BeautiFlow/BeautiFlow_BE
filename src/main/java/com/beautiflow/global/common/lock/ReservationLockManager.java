package com.beautiflow.global.common.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationLockManager {

    private final RedissonClient redissonClient;
    private final Map<Long, RLock> lockMap = new ConcurrentHashMap<>();

    public boolean tryLock(Long reservationId, String lockName) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockName);
        boolean locked = lock.tryLock(5, TimeUnit.SECONDS);
        if (locked) {
            lockMap.put(reservationId, lock);
        }
        return locked;
    }

    public void unlock(Long reservationId) {
        RLock lock = lockMap.remove(reservationId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}

