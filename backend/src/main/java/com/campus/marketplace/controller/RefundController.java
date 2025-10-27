package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.service.RefundService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/orders/{orderNo}/refunds")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public ApiResponse<String> apply(@PathVariable String orderNo,
                                     @RequestParam @NotBlank String reason,
                                     @RequestBody(required = false) Map<String, Object> evidence) {
        String refundNo = refundService.applyRefund(orderNo, reason, evidence);
        return ApiResponse.success(refundNo);
    }

    @PutMapping("/admin/refunds/{refundNo}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<Void> approve(@PathVariable String refundNo) {
        refundService.approveAndRefund(refundNo);
        return ApiResponse.success(null);
    }

    @PutMapping("/admin/refunds/{refundNo}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<Void> reject(@PathVariable String refundNo,
                                    @RequestParam @NotBlank String reason) {
        refundService.reject(refundNo, reason);
        return ApiResponse.success(null);
    }

    @GetMapping("/admin/refunds/{refundNo}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<RefundRequest> detail(@PathVariable String refundNo) {
        return ApiResponse.success(refundService.getByRefundNo(refundNo));
    }
}
