package com.campus.marketplace.controller.test;

import com.campus.marketplace.common.annotation.RateLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/test/ratelimit")
public class RateLimitTestController {

    @GetMapping("/tb")
    @RateLimit(key = "test:tb", limitType = RateLimit.LimitType.IP,
            algorithm = RateLimit.Algorithm.TOKEN_BUCKET,
            tokenBucketCapacity = 3, refillTokens = 3, refillInterval = 60)
    public ResponseEntity<String> tokenBucket() {
        return ResponseEntity.ok("ok");
    }
}
