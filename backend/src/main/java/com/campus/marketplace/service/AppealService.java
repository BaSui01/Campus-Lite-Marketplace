package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.BatchReviewRequest;
import com.campus.marketplace.common.dto.request.CreateAppealRequest;
import com.campus.marketplace.common.dto.request.ReviewRequest;
import com.campus.marketplace.common.dto.response.AppealStatistics;
import com.campus.marketplace.common.dto.response.AppealDetailResponse;
import com.campus.marketplace.common.dto.response.BatchReviewResult;
import com.campus.marketplace.common.dto.response.MaterialUploadResponse;
import com.campus.marketplace.common.entity.Appeal;
import com.campus.marketplace.common.entity.AppealMaterial;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 * 申诉服务接口
 * 
 * @author BaSui
 * @date 2025-11-02
 */
public interface AppealService {

    /**
     * 提交申诉
     * 
     * @param request 申诉请求
     * @return 申诉ID
     */
    Long submitAppeal(CreateAppealRequest request);

    /**
     * 批量审核申诉
     * 
     * @param request 批量审核请求
     * @return 批量审核结果
     */
    BatchReviewResult batchReviewAppeals(BatchReviewRequest request);

    /**
     * 查询用户的申诉列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 申诉分页结果
     */
    Page<Appeal> getUserAppeals(Long userId, Pageable pageable);

    /**
     * 审核申诉
     * 
     * @param request 审核请求
     * @return 审核后的申诉
     */
    Appeal reviewAppeal(ReviewRequest request);

    /**
     * 验证申诉资格
     * 
     * @param request 申诉请求
     * @return 是否有资格
     */
    boolean validateAppealEligibility(CreateAppealRequest request);

    /**
     * 标记过期申诉
     * 
     * @return 过期的申诉数量
     */
    int markExpiredAppeals();

    /**
     * 取消申诉
     * 
     * @param appealId 申诉ID
     * @return 是否成功
     */
    boolean cancelAppeal(Long appealId);

    /**
     * 获取申诉统计信息
     * 
     * @return 统计数据
     */
    AppealStatistics getAppealStatistics();

    /**
     * 获取申诉详细信息（包含材料）
     * 
     * @param appealId 申诉ID
     * @return 申诉详情
     */
    AppealDetailResponse getAppealDetail(Long appealId);

    /**
     * 上传申诉材料
     * 
     * @param appealId 申诉ID
     * @param files 上传的文件
     * @param uploadedBy 上传用户ID
     * @param uploadedByName 上传用户名
     * @return 上传结果
     */
    MaterialUploadResponse uploadAppealMaterials(String appealId, MultipartFile[] files, Long uploadedBy, String uploadedByName);

    /**
     * 删除申诉材料
     * 
     * @param materialId 材料ID
     * @param deletedBy 删除用户ID
     * @return 是否成功
     */
    boolean deleteAppealMaterial(Long materialId, Long deletedBy);

    /**
     * 获取申诉材料列表
     * 
     * @param appealId 申诉ID
     * @return 材料列表
     */
    java.util.List<AppealMaterial> getAppealMaterials(String appealId);
}
