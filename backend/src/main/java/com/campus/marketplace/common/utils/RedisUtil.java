package com.campus.marketplace.common.utils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Redis 工具类（支持内存降级）
 *
 * <p>当 {@code app.redis.mode=redis} 且 Redis 可用时，转发到 {@link RedisTemplate}；
 * 否则退化为线程安全的内存实现，保证开发/测试环境无需 Redis 也能启动。</p>
 *
 * <p>注意：内存模式仅用于本地开发或 CI 场景，不具备分布式能力。</p>
 *
 * @author BaSui
 * @date 2025-10-25
 */
@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final boolean useRedis;

    private final ConcurrentMap<String, ValueWrapper> store = new ConcurrentHashMap<>();

    public RedisUtil(ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider,
                     @Value("${app.redis.mode:redis}") String redisMode) {
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        this.useRedis = this.redisTemplate != null && "redis".equalsIgnoreCase(redisMode);
    }

    private boolean isRedisEnabled() {
        return useRedis;
    }

    // ========== String 操作 ==========

    public void set(String key, Object value) {
        if (isRedisEnabled()) {
            redisTemplate.opsForValue().set(key, value);
            return;
        }
        setValue(key, value, null);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        if (isRedisEnabled()) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return;
        }
        setValue(key, value, System.currentTimeMillis() + unit.toMillis(timeout));
    }

    public Object get(String key) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForValue().get(key);
        }
        ValueWrapper wrapper = getWrapper(key);
        return wrapper == null ? null : wrapper.value;
    }

    public Boolean delete(String key) {
        if (isRedisEnabled()) {
            return redisTemplate.delete(key);
        }
        return store.remove(key) != null;
    }

    public Long delete(Collection<String> keys) {
        if (isRedisEnabled()) {
            return redisTemplate.delete(keys);
        }
        long removed = 0;
        for (String key : keys) {
            if (delete(key)) {
                removed++;
            }
        }
        return removed;
    }

    public Boolean hasKey(String key) {
        if (isRedisEnabled()) {
            return redisTemplate.hasKey(key);
        }
        return getWrapper(key) != null;
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        if (isRedisEnabled()) {
            return redisTemplate.expire(key, timeout, unit);
        }
        ValueWrapper wrapper = getWrapper(key);
        if (wrapper == null) {
            return false;
        }
        wrapper.expireAt = System.currentTimeMillis() + unit.toMillis(timeout);
        return true;
    }

    public Long getExpire(String key) {
        if (isRedisEnabled()) {
            return redisTemplate.getExpire(key);
        }
        ValueWrapper wrapper = getWrapper(key);
        if (wrapper == null) {
            return -2L;
        }
        if (wrapper.expireAt == null) {
            return -1L;
        }
        long remaining = wrapper.expireAt - System.currentTimeMillis();
        return remaining > 0 ? TimeUnit.SECONDS.convert(remaining, TimeUnit.MILLISECONDS) : -2L;
    }

    public Long increment(String key) {
        return increment(key, 1);
    }

    public Long increment(String key, long delta) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForValue().increment(key, delta);
        }
        ValueWrapper wrapper = getWrapper(key);
        long current = 0;
        Long expireAt = null;
        if (wrapper != null && wrapper.value instanceof Number number) {
            current = number.longValue();
            expireAt = wrapper.expireAt;
        }
        current += delta;
        setValue(key, current, expireAt);
        return current;
    }

    public Long decrement(String key) {
        return increment(key, -1);
    }

    // ========== Hash 操作 ==========

    public void hSet(String key, String hashKey, Object value) {
        if (isRedisEnabled()) {
            redisTemplate.opsForHash().put(key, hashKey, value);
            return;
        }
        Map<String, Object> map = getOrCreateMap(key);
        map.put(hashKey, value);
    }

    public void hSetAll(String key, Map<String, Object> map) {
        if (isRedisEnabled()) {
            redisTemplate.opsForHash().putAll(key, map);
            return;
        }
        Map<String, Object> data = getOrCreateMap(key);
        data.putAll(map);
    }

    public Object hGet(String key, String hashKey) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForHash().get(key, hashKey);
        }
        Map<String, Object> map = getMap(key);
        return map == null ? null : map.get(hashKey);
    }

    public Boolean hHasKey(String key, String hashKey) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForHash().hasKey(key, hashKey);
        }
        Map<String, Object> map = getMap(key);
        return map != null && map.containsKey(hashKey);
    }

    public Long hDelete(String key, Object... hashKeys) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForHash().delete(key, hashKeys);
        }
        Map<String, Object> map = getMap(key);
        if (map == null) {
            return 0L;
        }
        long removed = 0;
        for (Object hashKey : hashKeys) {
            if (map.remove(String.valueOf(hashKey)) != null) {
                removed++;
            }
        }
        return removed;
    }

    public Map<Object, Object> hEntries(String key) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForHash().entries(key);
        }
        Map<String, Object> map = getMap(key);
        if (map == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }

    // ========== Set 操作 ==========

    public Long sAdd(String key, Object... values) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForSet().add(key, values);
        }
        Set<Object> set = getOrCreateSet(key);
        long added = 0;
        for (Object value : values) {
            if (set.add(value)) {
                added++;
            }
        }
        return added;
    }

    public Set<Object> sMembers(String key) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForSet().members(key);
        }
        Set<Object> set = getSet(key);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    public Boolean sIsMember(String key, Object value) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForSet().isMember(key, value);
        }
        Set<Object> set = getSet(key);
        return set != null && set.contains(value);
    }

    public Long sRemove(String key, Object... values) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForSet().remove(key, values);
        }
        Set<Object> set = getSet(key);
        if (set == null) {
            return 0L;
        }
        long removed = 0;
        for (Object value : values) {
            if (set.remove(value)) {
                removed++;
            }
        }
        return removed;
    }

    public Long sSize(String key) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForSet().size(key);
        }
        Set<Object> set = getSet(key);
        return set == null ? 0L : (long) set.size();
    }

    // ========== Sorted Set 操作 ==========

    public Boolean zAdd(String key, Object value, double score) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForZSet().add(key, value, score);
        }
        Map<Object, Double> map = getOrCreateZSet(key);
        Double previous = map.put(value, score);
        return !Objects.equals(previous, score);
    }

    public Double zIncrementScore(String key, Object value, double delta) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForZSet().incrementScore(key, value, delta);
        }
        Map<Object, Double> map = getOrCreateZSet(key);
        double newScore = map.getOrDefault(value, 0.0d) + delta;
        map.put(value, newScore);
        return newScore;
    }

    public Set<Object> zRange(String key, long start, long end) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForZSet().range(key, start, end);
        }
        return zRangeInternal(key, start, end, false);
    }

    public Set<Object> zReverseRange(String key, long start, long end) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForZSet().reverseRange(key, start, end);
        }
        return zRangeInternal(key, start, end, true);
    }

    public Long zRemove(String key, Object... values) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForZSet().remove(key, values);
        }
        Map<Object, Double> map = getZSet(key);
        if (map == null) {
            return 0L;
        }
        long removed = 0;
        for (Object value : values) {
            if (map.remove(value) != null) {
                removed++;
            }
        }
        return removed;
    }

    // ========== List 操作 ==========

    public Long lPush(String key, Object value) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForList().rightPush(key, value);
        }
        Deque<Object> deque = getOrCreateDeque(key);
        deque.addLast(value);
        return (long) deque.size();
    }

    public Long lLeftPush(String key, Object value) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForList().leftPush(key, value);
        }
        Deque<Object> deque = getOrCreateDeque(key);
        deque.addFirst(value);
        return (long) deque.size();
    }

    public List<Object> lRange(String key, long start, long end) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForList().range(key, start, end);
        }
        Deque<Object> deque = getDeque(key);
        if (deque == null || deque.isEmpty()) {
            return Collections.emptyList();
        }
        List<Object> list = new ArrayList<>(deque);
        int size = list.size();
        int from = normalizeIndex(start, size);
        int to = normalizeIndex(end, size);
        if (from > to || from >= size) {
            return Collections.emptyList();
        }
        to = Math.min(to, size - 1);
        return list.subList(from, to + 1);
    }

    public Long lSize(String key) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForList().size(key);
        }
        Deque<Object> deque = getDeque(key);
        return deque == null ? 0L : (long) deque.size();
    }

    public Object lPop(String key) {
        if (isRedisEnabled()) {
            return redisTemplate.opsForList().rightPop(key);
        }
        Deque<Object> deque = getDeque(key);
        if (deque == null) {
            return null;
        }
        return deque.pollLast();
    }

    // ========== 高级操作 ==========

    public Set<String> scan(String pattern) {
        if (isRedisEnabled()) {
            return redisTemplate.keys(pattern);
        }
        Pattern regex = Pattern.compile(convertPattern(pattern));
        return store.entrySet().stream()
                .filter(entry -> {
                    ValueWrapper wrapper = entry.getValue();
                    if (wrapper == null || wrapper.expired()) {
                        store.remove(entry.getKey(), wrapper);
                        return false;
                    }
                    return true;
                })
                .map(Map.Entry::getKey)
                .filter(key -> regex.matcher(key).matches())
                .collect(Collectors.toSet());
    }

    public Long deleteByPattern(String pattern) {
        Set<String> keys = scan(pattern);
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return delete(keys);
    }

    // ========== 内存模式辅助 ==========

    private void setValue(String key, Object value, Long expireAt) {
        ValueWrapper wrapper = new ValueWrapper();
        wrapper.value = value;
        wrapper.expireAt = expireAt;
        store.put(key, wrapper);
    }

    private ValueWrapper getWrapper(String key) {
        ValueWrapper wrapper = store.get(key);
        if (wrapper != null && wrapper.expired()) {
            store.remove(key, wrapper);
            return null;
        }
        return wrapper;
    }

    private Map<String, Object> getMap(String key) {
        ValueWrapper wrapper = getWrapper(key);
        if (wrapper == null || !(wrapper.value instanceof Map<?, ?> map)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> cast = (Map<String, Object>) map;
        return cast;
    }

    private Map<String, Object> getOrCreateMap(String key) {
        ValueWrapper wrapper = store.compute(key, (k, existing) -> {
            if (existing == null || existing.expired() || !(existing.value instanceof Map<?, ?>)) {
                ValueWrapper created = new ValueWrapper();
                created.value = new ConcurrentHashMap<String, Object>();
                return created;
            }
            return existing;
        });
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) wrapper.value;
        return map;
    }

    private Set<Object> getSet(String key) {
        ValueWrapper wrapper = getWrapper(key);
        if (wrapper == null || !(wrapper.value instanceof Set<?> set)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Set<Object> cast = (Set<Object>) set;
        return cast;
    }

    private Set<Object> getOrCreateSet(String key) {
        ValueWrapper wrapper = store.compute(key, (k, existing) -> {
            if (existing == null || existing.expired() || !(existing.value instanceof Set<?>)) {
                ValueWrapper created = new ValueWrapper();
                created.value = Collections.synchronizedSet(new HashSet<>());
                return created;
            }
            return existing;
        });
        @SuppressWarnings("unchecked")
        Set<Object> set = (Set<Object>) wrapper.value;
        return set;
    }

    private Map<Object, Double> getZSet(String key) {
        ValueWrapper wrapper = getWrapper(key);
        if (wrapper == null || !(wrapper.value instanceof Map<?, ?> map)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<Object, Double> cast = (Map<Object, Double>) map;
        return cast;
    }

    private Map<Object, Double> getOrCreateZSet(String key) {
        ValueWrapper wrapper = store.compute(key, (k, existing) -> {
            if (existing == null || existing.expired() || !(existing.value instanceof Map<?, ?>)) {
                ValueWrapper created = new ValueWrapper();
                created.value = new ConcurrentHashMap<Object, Double>();
                return created;
            }
            return existing;
        });
        @SuppressWarnings("unchecked")
        Map<Object, Double> map = (Map<Object, Double>) wrapper.value;
        return map;
    }

    private Deque<Object> getDeque(String key) {
        ValueWrapper wrapper = getWrapper(key);
        if (wrapper == null || !(wrapper.value instanceof Deque<?> deque)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Deque<Object> cast = (Deque<Object>) deque;
        return cast;
    }

    private Deque<Object> getOrCreateDeque(String key) {
        ValueWrapper wrapper = store.compute(key, (k, existing) -> {
            if (existing == null || existing.expired() || !(existing.value instanceof Deque<?>)) {
                ValueWrapper created = new ValueWrapper();
                created.value = new ConcurrentLinkedDeque<>();
                return created;
            }
            return existing;
        });
        @SuppressWarnings("unchecked")
        Deque<Object> deque = (Deque<Object>) wrapper.value;
        return deque;
    }

    private Set<Object> zRangeInternal(String key, long start, long end, boolean reverse) {
        Map<Object, Double> map = getZSet(key);
        if (map == null || map.isEmpty()) {
            return Collections.emptySet();
        }
        List<Map.Entry<Object, Double>> entries = new ArrayList<>(map.entrySet());
        entries.sort((o1, o2) -> {
            int compare = Double.compare(o1.getValue(), o2.getValue());
            if (compare == 0) {
                return String.valueOf(o1.getKey()).compareTo(String.valueOf(o2.getKey()));
            }
            return compare;
        });
        if (reverse) {
            Collections.reverse(entries);
        }
        int size = entries.size();
        int from = normalizeIndex(start, size);
        int to = normalizeIndex(end, size);
        if (from > to || from >= size) {
            return Collections.emptySet();
        }
        to = Math.min(to, size - 1);
        LinkedHashSet<Object> result = new LinkedHashSet<>();
        for (int i = from; i <= to; i++) {
            result.add(entries.get(i).getKey());
        }
        return result;
    }

    private int normalizeIndex(long index, int size) {
        if (index < 0) {
            long adj = size + index;
            return (int) Math.max(adj, 0);
        }
        return (int) Math.min(index, Integer.MAX_VALUE);
    }

    private String convertPattern(String pattern) {
        StringBuilder regex = new StringBuilder();
        char[] chars = pattern.toCharArray();
        for (char c : chars) {
            switch (c) {
                case '*':
                    regex.append(".*");
                    break;
                case '?':
                    regex.append('.');
                    break;
                case '.':
                case '\\':
                case '+':
                case '[':
                case ']':
                case '(':
                case ')':
                case '{':
                case '}':
                case '^':
                case '$':
                case '|':
                    regex.append('\\').append(c);
                    break;
                default:
                    regex.append(c);
            }
        }
        return regex.toString();
    }

    private static final class ValueWrapper {
        volatile Object value;
        volatile Long expireAt;

        boolean expired() {
            return expireAt != null && expireAt <= System.currentTimeMillis();
        }
    }
}
