package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateReplyRequest;
import com.campus.marketplace.common.dto.response.ReplyResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 回复服务接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface ReplyService {

    /**
     * 创建回复
     * 
     * @param request 回复请求
     * @return 回复 ID
     */
    Long createReply(CreateReplyRequest request);

    /**
     * 查询帖子的回复列表
     * 
     * @param postId 帖子 ID
     * @param page 页码
     * @param size 每页大小
     * @return 回复分页结果
     */
    Page<ReplyResponse> listReplies(Long postId, int page, int size);

    /**
     * 查询回复的子回复（楼中楼）
     * 
     * @param parentId 父回复 ID
     * @return 子回复列表
     */
    List<ReplyResponse> listSubReplies(Long parentId);

    /**
     * 删除回复
     * 
     * @param id 回复 ID
     */
    void deleteReply(Long id);
}