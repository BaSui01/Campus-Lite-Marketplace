package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreatePostRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.PostResponse;
import com.campus.marketplace.service.PostService;
import com.campus.marketplace.common.annotation.RateLimit;
import com.campus.marketplace.common.dto.request.UpdatePostRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 *
 * 处理论坛帖子相关的 HTTP 请求
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "论坛管理", description = "帖子发布、查询、审核等接口")
public class PostController {

    private final PostService postService;

        @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "发布帖子", description = "用户发布论坛帖子")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "创建帖子请求体",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreatePostRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"title\": \"二手耳机转让\",
                                      \"content\": \"九成新，音质很好，支持当面验货\",
                                      \"images\": [
                                        \"https://cdn.campus.com/post/p1.png\",
                                        \"https://cdn.campus.com/post/p2.png\"
                                      ]
                                    }
                                    """
                    )
            )
    )
    @RateLimit(key = "post:create", maxRequests = 10, timeWindow = 60)
    public ApiResponse<Long> createPost(@Valid @RequestBody CreatePostRequest request) {
        Long postId = postService.createPost(request);
        return ApiResponse.success(postId);
    }

        @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "修改帖子", description = "作者或管理员可编辑帖子，内容变更将重置为待审核")
    public ApiResponse<Void> updatePost(
            @Parameter(description = "帖子 ID", example = "98765") @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        postService.updatePost(id, request);
        return ApiResponse.success(null);
    }

        @GetMapping
    @Operation(summary = "查询帖子列表", description = "分页查询帖子列表，支持排序")
    public ApiResponse<Page<PostResponse>> listPosts(
            @Parameter(description = "页码（从 0 开始）", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段（createdAt/viewCount/replyCount）", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向（ASC/DESC）", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Page<PostResponse> result = postService.listPosts(page, size, sortBy, sortDirection);
        return ApiResponse.success(result);
    }

        @GetMapping("/search")
    @Operation(summary = "搜索帖子", description = "根据关键词搜索帖子")
    public ApiResponse<Page<PostResponse>> searchPosts(
            @Parameter(description = "搜索关键词", example = "二手自行车") @RequestParam String keyword,
            @Parameter(description = "页码（从 0 开始）", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Page<PostResponse> result = postService.searchPosts(keyword, page, size);
        return ApiResponse.success(result);
    }

        @GetMapping("/user/{authorId}")
    @Operation(summary = "查询指定用户的帖子列表", description = "根据作者 ID 查询其发布的所有帖子")
    public ApiResponse<Page<PostResponse>> listPostsByAuthor(
            @Parameter(description = "作者 ID", example = "10002") @PathVariable Long authorId,
            @Parameter(description = "页码（从 0 开始）", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Page<PostResponse> result = postService.listPostsByAuthor(authorId, page, size);
        return ApiResponse.success(result);
    }

        @GetMapping("/{id}")
    @Operation(summary = "查询帖子详情", description = "根据帖子 ID 查询详细信息")
    public ApiResponse<PostResponse> getPostDetail(
            @Parameter(description = "帖子 ID", example = "98765") @PathVariable Long id
    ) {
        PostResponse response = postService.getPostDetail(id);
        return ApiResponse.success(response);
    }

        @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_POST_APPROVE)")
    @Operation(summary = "审核帖子", description = "管理员审核帖子，通过或拒绝")
    public ApiResponse<Void> approvePost(
            @Parameter(description = "帖子 ID", example = "98765") @PathVariable Long id,
            @Parameter(description = "是否通过", example = "true") @RequestParam boolean approved,
            @Parameter(description = "拒绝原因（可选）", example = "含有广告信息") @RequestParam(required = false) String reason
    ) {
        postService.approvePost(id, approved, reason);
        return ApiResponse.success(null);
    }

        @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "删除帖子", description = "作者或管理员删除帖子")
    public ApiResponse<Void> deletePost(
            @Parameter(description = "帖子 ID", example = "98765") @PathVariable Long id
    ) {
        postService.deletePost(id);
        return ApiResponse.success(null);
    }

    // ==================== 新增接口（2025-11-09 - BaSui 😎）====================

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_POST_APPROVE)")
    @Operation(summary = "查询待审核帖子列表", description = "管理员查询所有待审核的帖子")
    public ApiResponse<Page<PostResponse>> listPendingPosts(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Page<PostResponse> result = postService.listPendingPosts(page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/hot")
    @Operation(summary = "查询热门帖子", description = "根据热度算法（点赞数*2 + 浏览量 + 回复数*3）排序")
    public ApiResponse<Page<PostResponse>> listHotPosts(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Page<PostResponse> result = postService.listHotPosts(page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/my-likes")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询我的点赞列表", description = "查询当前用户点赞的所有帖子")
    public ApiResponse<Page<PostResponse>> listMyLikes(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = com.campus.marketplace.common.utils.SecurityUtil.getCurrentUserId();
        Page<PostResponse> result = postService.listUserLikes(userId, page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/my-collects")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询我的收藏列表", description = "查询当前用户收藏的所有帖子")
    public ApiResponse<Page<PostResponse>> listMyCollects(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = com.campus.marketplace.common.utils.SecurityUtil.getCurrentUserId();
        Page<PostResponse> result = postService.listUserCollects(userId, page, size);
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/top")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_POST_APPROVE)")
    @Operation(summary = "置顶/取消置顶帖子", description = "管理员置顶或取消置顶帖子")
    public ApiResponse<Void> toggleTopPost(
            @Parameter(description = "帖子 ID", example = "98765") @PathVariable Long id,
            @Parameter(description = "是否置顶", example = "true") @RequestParam boolean isTop
    ) {
        postService.toggleTopPost(id, isTop);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch-approve")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_POST_APPROVE)")
    @Operation(summary = "批量审核帖子", description = "管理员批量审核多个帖子")
    public ApiResponse<Integer> batchApprovePosts(
            @Parameter(description = "帖子 ID 列表") @RequestBody java.util.List<Long> ids,
            @Parameter(description = "是否通过", example = "true") @RequestParam boolean approved,
            @Parameter(description = "拒绝原因（可选）") @RequestParam(required = false) String reason
    ) {
        int successCount = postService.batchApprovePosts(ids, approved, reason);
        return ApiResponse.success(successCount);
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "获取帖子统计信息", description = "查询帖子的详细统计数据（点赞用户、收藏用户等）")
    public ApiResponse<com.campus.marketplace.common.dto.response.PostStatsResponse> getPostStats(
            @Parameter(description = "帖子 ID", example = "98765") @PathVariable Long id
    ) {
        com.campus.marketplace.common.dto.response.PostStatsResponse stats = postService.getPostStats(id);
        return ApiResponse.success(stats);
    }
}
