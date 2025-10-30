package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.dto.response.CampusMigrationValidationResponse;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.CampusRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Campus Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class CampusServiceImpl implements com.campus.marketplace.service.CampusService {

    private final CampusRepository campusRepository;
    private final GoodsRepository goodsRepository;
    private final PostRepository postRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public List<Campus> listAll() {
        return campusRepository.findAll();
    }

    @Override
    @Transactional
    public Campus create(String code, String name) {
        if (campusRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "校园编码已存在");
        }
        Campus campus = Campus.builder()
                .code(code)
                .name(name)
                .status(CampusStatus.ACTIVE)
                .build();
        return campusRepository.save(campus);
    }

    @Override
    @Transactional
    public Campus update(Long id, String name, CampusStatus status) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "校区不存在"));
        campus.setName(name);
        campus.setStatus(status);
        return campusRepository.save(campus);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "校区不存在"));
        // 约束校验：存在关联数据则不允许删除（最小校验，避免长查询）
        long goodsCount = goodsRepository.countByCampusId(id);
        long postCount = postRepository.countByCampusId(id);
        long orderCount = orderRepository.countByCampusId(id);
        long userCount = userRepository.countByCampusId(id);
        if (goodsCount > 0 || postCount > 0 || orderCount > 0 || userCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "存在关联数据，禁止删除");
        }
        campusRepository.delete(campus);
    }

    @Override
    @Transactional(readOnly = true)
    public CampusMigrationValidationResponse validateUserMigration(Long fromCampusId, Long toCampusId) {
        if (fromCampusId == null || toCampusId == null || fromCampusId.equals(toCampusId)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "无效的迁移参数");
        }
        Campus from = campusRepository.findById(fromCampusId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "源校区不存在"));
        Campus to = campusRepository.findById(toCampusId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "目标校区不存在"));
        // 使用 from 进行审计日志，避免未使用变量告警
        log.debug("校区迁移校验：from(id={}, code={}), to(id={}, code={})",
                from.getId(), from.getCode(), to.getId(), to.getCode());
        if (to.getStatus() != CampusStatus.ACTIVE) {
            return CampusMigrationValidationResponse.builder()
                    .fromCampusId(fromCampusId)
                    .toCampusId(toCampusId)
                    .goodsCount(0)
                    .postCount(0)
                    .orderCount(0)
                    .userCount(0)
                    .allowed(false)
                    .reason("目标校区未启用")
                    .build();
        }
        long goods = goodsRepository.countByCampusId(fromCampusId);
        long posts = postRepository.countByCampusId(fromCampusId);
        long orders = orderRepository.countByCampusId(fromCampusId);
        long users = userRepository.countByCampusId(fromCampusId);
        boolean allowed = goods == 0 && posts == 0 && orders == 0;
        String reason = allowed ? null : "源校区存在关联数据（物品/帖子/订单），请先处理后再迁移用户";
        return CampusMigrationValidationResponse.builder()
                .fromCampusId(fromCampusId)
                .toCampusId(toCampusId)
                .goodsCount(goods)
                .postCount(posts)
                .orderCount(orders)
                .userCount(users)
                .allowed(allowed)
                .reason(reason)
                .build();
    }

    @Override
    @Transactional
    public int migrateUsers(Long fromCampusId, Long toCampusId) {
        CampusMigrationValidationResponse validation = validateUserMigration(fromCampusId, toCampusId);
        if (!validation.isAllowed()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, validation.getReason());
        }
        return userRepository.updateCampusByCampusId(fromCampusId, toCampusId);
    }
}
