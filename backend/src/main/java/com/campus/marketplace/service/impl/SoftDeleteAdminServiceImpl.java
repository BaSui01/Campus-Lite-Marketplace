package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.BaseEntity;
import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Favorite;
import com.campus.marketplace.common.entity.Follow;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Post;
import com.campus.marketplace.common.entity.Reply;
import com.campus.marketplace.common.entity.Tag;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.SoftDeleteAdminService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 管理端软删除治理实现
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SoftDeleteAdminServiceImpl implements SoftDeleteAdminService {

    private record Target(Class<? extends BaseEntity> entityClass, String table) {}

    private static final Map<String, Target> TARGETS;

    static {
        Map<String, Target> map = new LinkedHashMap<>();
        map.put("post", new Target(Post.class, "t_post"));
        map.put("goods", new Target(Goods.class, "t_goods"));
        map.put("tag", new Target(Tag.class, "t_tag"));
        map.put("reply", new Target(Reply.class, "t_reply"));
        map.put("favorite", new Target(Favorite.class, "t_favorite"));
        map.put("follow", new Target(Follow.class, "t_follow"));
        map.put("campus", new Target(Campus.class, "t_campus"));
        map.put("category", new Target(Category.class, "t_category"));
        TARGETS = Map.copyOf(map);
    }

    private final EntityManager entityManager;

    @Override
    public List<String> listTargets() {
        return TARGETS.keySet().stream().toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(String entityKey, Long id) {
        Target target = resolve(entityKey);
        boolean deleted = getDeletedFlag(target, id);
        if (!deleted) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "记录不存在或未被软删除");
        }

        String jpql = "update " + target.entityClass().getSimpleName() +
                " e set e.deleted = false, e.deletedAt = null where e.id = :id";
        int updated = entityManager.createQuery(jpql)
                .setParameter("id", id)
                .executeUpdate();
        if (updated == 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "恢复失败，记录可能不存在");
        }
        log.info("恢复软删除记录成功 entity={} id={}", entityKey, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purge(String entityKey, Long id) {
        Target target = resolve(entityKey);
        ensureExists(target, id);

        String jpql = "delete from " + target.entityClass().getSimpleName() + " e where e.id = :id";
        int deleted = entityManager.createQuery(jpql)
                .setParameter("id", id)
                .executeUpdate();
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "彻底删除失败，记录可能不存在");
        }
        log.info("彻底删除记录成功 entity={} id={}", entityKey, id);
    }

    private Target resolve(String entityKey) {
        if (entityKey == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "实体标识不能为空");
        }
        String normalized = entityKey.toLowerCase(Locale.ROOT);
        Target target = TARGETS.get(normalized);
        if (target == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的实体类型: " + entityKey);
        }
        return target;
    }

    private boolean getDeletedFlag(Target target, Long id) {
        Object result = selectSingle(target.table(), id);
        if (result == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "记录不存在");
        }
        if (result instanceof Boolean b) {
            return b;
        }
        if (result instanceof Number n) {
            return n.intValue() != 0;
        }
        if (result instanceof String s) {
            return "1".equals(s) || Boolean.parseBoolean(s);
        }
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "无法识别的删除标记类型");
    }

    private void ensureExists(Target target, Long id) {
        selectSingle(target.table(), id);
    }

    private Object selectSingle(String table, Long id) {
        try {
            Query query = entityManager.createNativeQuery("SELECT deleted FROM " + table + " WHERE id = :id");
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "记录不存在");
        }
    }
}
