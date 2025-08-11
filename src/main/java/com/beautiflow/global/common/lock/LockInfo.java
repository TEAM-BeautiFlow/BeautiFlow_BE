package com.beautiflow.global.common.lock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.redisson.api.RLock;

@Getter
@AllArgsConstructor
public class LockInfo {
    private final RLock lock;
    private final long threadId;
}
