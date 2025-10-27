package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.PointsLog;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.PointsType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.PointsLogRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分服务实现类
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final UserRepository userRepository;
    private final PointsLogRepository pointsLogRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, PointsType type, String description) {
        log.info("增加积分: userId={}, type={}, points={}", userId, type, type.getDefaultPoints());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Integer points = type.getDefaultPoints();
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);

        PointsLog pointsLog = PointsLog.builder()
                .userId(userId)
                .type(type)
                .points(points)
                .balance(user.getPoints())
                .description(description)
                .build();
        pointsLogRepository.save(pointsLog);

        log.info("积分增加成功: userId={}, points={}, balance={}", userId, points, user.getPoints());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long userId, Integer points, String description) {
        log.info("扣除积分: userId={}, points={}", userId, points);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getPoints() < points) {
            log.warn("积分不足: userId={}, current={}, required={}", userId, user.getPoints(), points);
            throw new BusinessException(ErrorCode.POINTS_INSUFFICIENT);
        }

        user.setPoints(user.getPoints() - points);
        userRepository.save(user);

        PointsLog pointsLog = PointsLog.builder()
                .userId(userId)
                .type(PointsType.CONSUME)
                .points(-points)
                .balance(user.getPoints())
                .description(description)
                .build();
        pointsLogRepository.save(pointsLog);

        log.info("积分扣除成功: userId={}, points={}, balance={}", userId, points, user.getPoints());
    }

    @Override
    public Page<PointsLog> getPointsLog(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return pointsLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Integer getCurrentPoints(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getPoints();
    }
}
