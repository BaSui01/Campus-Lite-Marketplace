package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.MaterialUploadResponse;
import com.campus.marketplace.common.dto.response.MaterialStatistics;
import com.campus.marketplace.common.entity.AppealMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 申诉材料服务接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface AppealMaterialService {

    /**
     * 上传申诉材料
     *
     * @param appealId 申诉ID
     * @param files 文件数组
     * @param uploadedBy 上传人ID
     * @param uploadedByName 上传人姓名
     * @return 上传响应
     */
    MaterialUploadResponse uploadMaterials(String appealId, MultipartFile[] files, Long uploadedBy, String uploadedByName);

    /**
     * 删除申诉材料
     *
     * @param materialId 材料ID
     * @param deletedBy 删除人ID
     * @return 删除是否成功
     */
    boolean deleteMaterial(Long materialId, Long deletedBy);

    /**
     * 获取申诉的所有材料
     *
     * @param appealId 申诉ID
     * @return 材料列表
     */
    List<AppealMaterial> getAppealMaterials(String appealId);

    /**
     * 分页获取申诉材料
     *
     * @param appealId 申诉ID
     * @param pageable 分页参数
     * @return 材料分页数据
     */
    Page<AppealMaterial> getAppealMaterials(String appealId, Pageable pageable);

    /**
     * 按文件类型获取材料
     *
     * @param appealId 申诉ID
     * @param fileType 文件类型
     * @return 材料列表
     */
    List<AppealMaterial> getMaterialsByType(String appealId, String fileType);

    /**
     * 生成缩略图
     *
     * @param materialId 材料ID
     * @return 是否成功
     */
    boolean generateThumbnail(Long materialId);

    /**
     * 扫描文件病毒
     *
     * @param materialId 材料ID
     * @return 扫描结果
     */
    String scanFileForVirus(Long materialId);

    /**
     * 批量病毒扫描
     *
     * @return 扫描结果摘要
     */
    String batchVirusScan();

    /**
     * 清理过期文件
     *
     * @param daysOld 过期天数
     * @return 清理的文件数
     */
    int cleanupExpiredFiles(int daysOld);

    /**
     * 验证上传权限
     *
     * @param appealId 申诉ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean validateUploadPermission(String appealId, Long userId);

    /**
     * 获取下载URL
     *
     * @param materialId 材料ID
     * @return 下载URL
     */
    String getDownloadUrl(Long materialId);

    /**
     * 检查文件是否存在
     *
     * @param materialId 材料ID
     * @return 文件是否存在
     */
    boolean fileExists(Long materialId);

    /**
     * 获取材料统计信息
     *
     * @param appealId 申诉ID
     * @return 统计信息
     */
    MaterialStatistics getMaterialStatistics(String appealId);
}
