package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateReplyRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.ReplyResponse;
import com.campus.marketplace.service.ReplyService;
import com.campus.marketplace.common.annotation.RateLimit;
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

import java.util.List;

@RestController
@RequestMapping("/api/replies")
@RequiredArgsConstructor
@Tag(name = "回复管理", description = "帖子回复相关接口")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "创建回复", description = "回复帖子或楼中楼")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "创建回复请求体",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateReplyRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"postId\": 98765,
                                      \"content\": \"支持一下！\",
                                      \"parentId\": null,
                                      \"toUserId\": null
                                    }
                                    """
                    )
            )
    )
    @RateLimit(key = "reply:create", maxRequests = 15, timeWindow = 60)
    public ApiResponse<Long> createReply(@Valid @RequestBody CreateReplyRequest request) {
        Long replyId = replyService.createReply(request);
        return ApiResponse.success(replyId);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "查询帖子的回复列表", description = "分页查询帖子的所有回复")
    public ApiResponse<Page<ReplyResponse>> listReplies(
            @Parameter(description = "帖子 ID", example = "98765") @PathVariable Long postId,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Page<ReplyResponse> result = replyService.listReplies(postId, page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/{parentId}/sub")
    @Operation(summary = "查询子回复列表", description = "查询楼中楼回复")
    public ApiResponse<List<ReplyResponse>> listSubReplies(
            @Parameter(description = "父回复 ID", example = "123456") @PathVariable Long parentId
    ) {
        List<ReplyResponse> result = replyService.listSubReplies(parentId);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "删除回复", description = "作者或管理员删除回复")
    public ApiResponse<Void> deleteReply(
            @Parameter(description = "回复 ID", example = "54321") @PathVariable Long id
    ) {
        replyService.deleteReply(id);
        return ApiResponse.success(null);
    }
}
