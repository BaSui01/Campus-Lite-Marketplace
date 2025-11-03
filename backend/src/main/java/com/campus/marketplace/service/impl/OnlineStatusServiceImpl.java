package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.utils.RedisUtil;
import com.campus.marketplace.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线状态服务实现类
 * 
 * 使用 Redis 存储用户最后活跃时间
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineStatusServiceImpl implements OnlineStatusService {

    private final RedisUtil redis;

    private static final String ONLINE_STATUS_KEY_PREFIX = "user:online:";
    private static final int ONLINE_THRESHOLD_MINUTES = 5; // 5分钟内活跃视为在线

    @Override
    public void updateLastActiveTime(Long userId) {
        String key = ONLINE_STATUS_KEY_PREFIX + userId;
        redis.set(key, LocalDateTime.now().toString(), 10, java.util.concurrent.TimeUnit.MINUTES); // 10分钟过期
        log.debug("更新用户在线状态: userId={}", userId);
    }

    @Override
    public boolean isUserOnline(Long userId) {
        String key = ONLINE_STATUS_KEY_PREFIX + userId;
        String lastActiveTimeStr = (String) redis.get(key);

        if (lastActiveTimeStr == null) {
            return false;
        }

        try {
            LocalDateTime lastActiveTime = LocalDateTime.parse(lastActiveTimeStr);
            long minutesSinceLastActive = Duration.between(lastActiveTime, LocalDateTime.now()).toMinutes();

            return minutesSinceLastActive <= ONLINE_THRESHOLD_MINUTES;
        } catch (Exception e) {
            log.error("解析用户最后活跃时间失败: userId={}, value={}", userId, lastActiveTimeStr, e);
            return false;
        }
    }

    @Override
    public Map<Long, Boolean> batchCheckOnlineStatus(List<Long> userIds) {
        Map<Long, Boolean> result = new HashMap<>();

        for (Long userId : userIds) {
            result.put(userId, isUserOnline(userId));
        }

        return result;
    }
}
