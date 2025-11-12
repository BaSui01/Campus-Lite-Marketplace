package com.campus.marketplace.repository;

import com.campus.marketplace.entity.LoginDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 登录设备 Repository
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Repository
public interface LoginDeviceRepository extends JpaRepository<LoginDevice, Long> {

    /**
     * 查询用户的所有登录设备
     *
     * @param userId 用户ID
     * @return 登录设备列表
     */
    List<LoginDevice> findByUserIdOrderByLastActiveAtDesc(Long userId);

    /**
     * 查询用户的当前设备
     *
     * @param userId 用户ID
     * @return 当前设备
     */
    Optional<LoginDevice> findByUserIdAndIsCurrentTrue(Long userId);

    /**
     * 查询用户的指定设备
     *
     * @param id     设备ID
     * @param userId 用户ID
     * @return 设备信息
     */
    Optional<LoginDevice> findByIdAndUserId(Long id, Long userId);

    /**
     * 删除用户的指定设备
     *
     * @param id     设备ID
     * @param userId 用户ID
     */
    void deleteByIdAndUserId(Long id, Long userId);

    /**
     * 将用户的所有设备设置为非当前设备
     *
     * @param userId 用户ID
     */
    @Modifying
    @Query("UPDATE LoginDevice d SET d.isCurrent = false WHERE d.userId = :userId")
    void setAllDevicesNotCurrent(@Param("userId") Long userId);

    /**
     * 删除指定时间之前的设备记录
     *
     * @param before 时间点
     */
    @Modifying
    @Query("DELETE FROM LoginDevice d WHERE d.lastActiveAt < :before")
    void deleteByLastActiveAtBefore(@Param("before") LocalDateTime before);

    /**
     * 统计用户的设备数量
     *
     * @param userId 用户ID
     * @return 设备数量
     */
    long countByUserId(Long userId);
}
