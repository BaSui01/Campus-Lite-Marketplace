package com.campus.marketplace.concurrency;

import com.campus.marketplace.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 并发示例：验证 Redisson 分布式锁的互斥性（白盒关注并发特性）
 */
class RedissonLockConcurrencyIT extends IntegrationTestBase {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    @DisplayName("Redisson 分布式锁在高并发下保证互斥")
    void redissonLockEnsuresMutualExclusion() throws Exception {
        final int threads = 32;
        final String lockKey = "test:lock:concurrency";
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch ready = new CountDownLatch(threads);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger inSection = new AtomicInteger(0);
        AtomicInteger maxInSection = new AtomicInteger(0);
        AtomicInteger executed = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    RLock lock = redissonClient.getLock(lockKey);
                    if (lock.tryLock(5, 5, TimeUnit.SECONDS)) {
                        try {
                            int now = inSection.incrementAndGet();
                            maxInSection.getAndUpdate(prev -> Math.max(prev, now));
                            // 模拟临界区耗时
                            Thread.sleep(20);
                        } finally {
                            inSection.decrementAndGet();
                            lock.unlock();
                            executed.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }));
        }

        // 等待所有线程就绪后同时开始
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        done.await(30, TimeUnit.SECONDS);

        // 清理
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(maxInSection.get()).as("临界区最大并发应为1").isEqualTo(1);
        assertThat(executed.get()).as("应有所有线程成功进入临界区").isEqualTo(threads);
    }
}
