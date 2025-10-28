package com.campus.marketplace.repository.base;

import com.campus.marketplace.common.entity.SoftDeletable;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用软删除仓库基类：拦截 delete、deleteById、deleteAll 等方法改为软删
 */
public class SoftDeleteJpaRepository<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> {

    private final EntityManager em;

    public SoftDeleteJpaRepository(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.em = em;
    }

    public SoftDeleteJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.em = em;
    }

    private boolean softDeleteIfSupported(T entity) {
        if (entity instanceof SoftDeletable sd) {
            sd.markDeleted();
            // 直接 merge 保存，避免触发 save() 上可能的代理
            em.merge(entity);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void delete(T entity) {
        if (!softDeleteIfSupported(entity)) {
            super.delete(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        T entity = findById(id).orElse(null);
        if (entity == null || !softDeleteIfSupported(entity)) {
            super.deleteById(id);
        }
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<? extends T> entities) {
        List<T> hardDeletes = new ArrayList<>();
        for (T e : entities) {
            if (!softDeleteIfSupported(e)) {
                hardDeletes.add(e);
            }
        }
        if (!hardDeletes.isEmpty()) {
            super.deleteAll(hardDeletes);
        }
    }

    @Override
    @Transactional
    public void deleteAllById(Iterable<? extends ID> ids) {
        List<ID> hardIds = new ArrayList<>();
        for (ID id : ids) {
            T entity = findById(id).orElse(null);
            if (entity == null || !softDeleteIfSupported(entity)) {
                hardIds.add(id);
            }
        }
        if (!hardIds.isEmpty()) {
            super.deleteAllById(hardIds);
        }
    }
}
