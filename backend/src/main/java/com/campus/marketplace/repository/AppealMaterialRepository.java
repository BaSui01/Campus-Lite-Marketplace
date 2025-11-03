package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.AppealMaterial;
import com.campus.marketplace.common.enums.MaterialStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 申诉材料数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface AppealMaterialRepository extends JpaRepository<AppealMaterial, Long> {

    /**
     * 根据申诉ID查询所有材料
     * 
     * @param appealId 申诉ID
     * @return 材料列表
     */
    List<AppealMaterial> findByAppealIdOrderByUploadedAtDesc(String appealId);

    /**
     * 根据申诉ID分页查询材料
     * 
     * @param appealId 申诉ID
     * @param pageable 分页参数
     * @return 材料分页结果
     */
    Page<AppealMaterial> findByAppealIdOrderByUploadedAtDesc(String appealId, Pageable pageable);

    /**
     * 根据文件类型查询材料
     * 
     * @param appealId 申诉ID
     * @param fileType 文件类型
     * @return 材料列表
     */
    List<AppealMaterial> findByAppealIdAndFileTypeOrderByUploadedAtDesc(String appealId, String fileType);

    /**
     * 根据文件状态查询材料
     * 
     * @param appealId 申诉ID
     * @param status 文件状态
     * @return 材料列表
     */
    List<AppealMaterial> findByAppealIdAndStatusOrderByUploadedAtDesc(String appealId, MaterialStatus status);

    /**
     * 根据申诉ID和文件哈希查询材料
     * 
     * @param appealId 申诉ID
     * @param fileHash 文件哈希值
     * @return 材料列表
     */
    List<AppealMaterial> findByAppealIdAndFileHash(String appealId, String fileHash);

    /**
     * 查询用户上传的材料
     * 
     * @param uploadedBy 用户ID
     * @param pageable 分页参数
     * @return 材料分页结果
     */
    Page<AppealMaterial> findByUploadedByOrderByUploadedAtDesc(Long uploadedBy, Pageable pageable);

    /**
     * 根据文件类型过滤查询
     * 
     * @param appealId 申诉ID
     * @param fileTypes 文件类型列表
     * @return 材料列表
     */
    @Query("SELECT am FROM AppealMaterial am WHERE am.appealId = :appealId AND am.fileType LIKE CONCAT(:fileType, '%')")
    List<AppealMaterial> findByAppealIdAndFileTypeStartingWith(
        @Param("appealId") String appealId, 
        @Param("fileType") String fileType
    );

    /**
     * 查询需要病毒扫描的材料
     * 
     * @return 待扫描材料列表
     */
    @Query("SELECT am FROM AppealMaterial am WHERE am.virusScanResult = 'PENDING' AND am.status = 'UPLOADED'")
    List<AppealMaterial> findPendingVirusScanMaterials();

    /**
     * 查询指定时间范围内上传的材料
     * 
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageable 分页参数
     * @return 材料分页结果
     */
    @Query("SELECT am FROM AppealMaterial am WHERE am.uploadedAt BETWEEN :startDate AND :endDate")
    Page<AppealMaterial> findByUploadedAtBetween(
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * 统计用户上传的材料数量
     * 
     * @param uploadedBy 用户ID
     * @return 材料数量
     */
    @Query("SELECT COUNT(am) FROM AppealMaterial am WHERE am.uploadedBy = :uploadedBy")
    long countByUploadedBy(@Param("uploadedBy") Long uploadedBy);

    /**
     * 统计申诉的材料数量
     * 
     * @param appealId 申诉ID
     * @return 材料数量
     */
    @Query("SELECT COUNT(am) FROM AppealMaterial am WHERE am.appealId = :appealId")
    long countByAppealId(@Param("appealId") String appealId);

    /**
     * 查询超大的文件材料
     * 
     * @param minSize 最小文件大小（字节）
     * @return 材料列表
     */
    @Query("SELECT am FROM AppealMaterial am WHERE am.fileSize > :minSize ORDER BY am.fileSize DESC")
    List<AppealMaterial> findLargeFilesOrderBySize(@Param("minSize") Long minSize);

    /**
     * 批量删除申诉的所有材料
     * 
     * @param appealId 申诉ID
     * @return 删除数量
     */
    @Query("DELETE FROM AppealMaterial am WHERE am.appealId = :appealId")
    int deleteAllByAppealId(@Param("appealId") String appealId);

    /**
     * 根据文件状态统计材料数量
     * 
     * @param appealId 申诉ID
     * @return 状态和数量的映射
     */
    @Query("SELECT am.status, COUNT(am) FROM AppealMaterial am WHERE am.appealId = :appealId GROUP BY am.status")
    List<Object[]> countByAppealIdGroupByStatus(@Param("appealId") String appealId);

    /**
     * 查询未设置缩略图的图片文件
     * 
     * @param maxLimit 最大查询数量
     * @return 材料列表
     */
    @Query("SELECT am FROM AppealMaterial am WHERE am.thumbnailPath IS NULL AND am.fileType LIKE 'image%' ORDER BY am.uploadedAt ASC")
    List<AppealMaterial> findImagesWithoutThumbnail(@Param("maxLimit") int maxLimit);

    /**
     * 查询失败的上传材料
     * 
     * @param hoursAgo 多长时间前
     * @return 材料列表
     */
    @Query("SELECT am FROM AppealMaterial am WHERE am.status = 'FAILED' AND am.uploadedAt < :cutoffTime")
    List<AppealMaterial> findFailedUploadsOlderThan(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);
}
