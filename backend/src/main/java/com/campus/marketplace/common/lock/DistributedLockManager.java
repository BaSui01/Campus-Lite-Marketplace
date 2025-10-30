package com.campus.marketplace.common.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

/**
 * 分布式锁统一入口，支持 Redisson 与本地降级双模式。
 *
 * <p>在生产环境下委托 Redisson；当 Redis 未启用时自动退化为基于 {@link ReentrantLock}
 * 的进程内锁，保证业务代码无需改动即可运行。</p>
 */
@Component
public class DistributedLockManager {

    private final ObjectProvider<RedissonClient> redissonProvider;
    private final ConcurrentMap<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    public DistributedLockManager(ObjectProvider<RedissonClient> redissonProvider) {
        this.redissonProvider = redissonProvider;
    }

    /**
     * 尝试获取指定名称的锁。
     *
     * @param key       锁标识
     * @param waitTime  等待时间
     * @param leaseTime 租约时间（Redisson 模式生效）
     * @param unit      时间单位
     * @return 锁句柄，调用方需在使用完毕后 close()/unlock
     */
    public LockHandle tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        RedissonClient client = redissonProvider.getIfAvailable();
        if (client != null) {
            RLock lock = client.getLock(key);
            boolean acquired = false;
            try {
                acquired = lock.tryLock(waitTime, leaseTime, unit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            boolean finalAcquired = acquired;
            return new LockHandle(finalAcquired, lock::isHeldByCurrentThread, () -> {
                if (finalAcquired && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            });
        }

        ReentrantLock localLock = localLocks.computeIfAbsent(key, k -> new ReentrantLock());
        boolean acquired = false;
        try {
            acquired = localLock.tryLock(waitTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean finalAcquired = acquired;
        return new LockHandle(finalAcquired, localLock::isHeldByCurrentThread, () -> {
            if (finalAcquired && localLock.isHeldByCurrentThread()) {
                localLock.unlock();
            }
        });
    }

    /**
     * 锁句柄，支持 try-with-resources。
     */
    public static final class LockHandle implements AutoCloseable {

        private final boolean acquired;
        private final BooleanSupplier heldByCurrentThread;
        private final Runnable unlocker;

        private LockHandle(boolean acquired, BooleanSupplier heldByCurrentThread, Runnable unlocker) {
            this.acquired = acquired;
            this.heldByCurrentThread = heldByCurrentThread;
            this.unlocker = unlocker;
        }

        /**
         * 是否成功获得锁。
         */
        public boolean acquired() {
            return acquired;
        }

        /**
         * 当前线程是否仍持有锁。
         */
        public boolean isHeldByCurrentThread() {
            return acquired && heldByCurrentThread.getAsBoolean();
        }

        /**
         * 主动释放锁。
         */
        public void unlock() {
            if (acquired) {
                unlocker.run();
            }
        }

        @Override
        public void close() {
            unlock();
        }
    }
}
