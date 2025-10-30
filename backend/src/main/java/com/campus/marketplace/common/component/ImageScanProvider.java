package com.campus.marketplace.common.component;
/**
 * Image Scan Provider
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface ImageScanProvider {

    enum Decision { PASS, REVIEW, REJECT }

    class Result {
        public final Decision decision;
        public final String reason;

        public Result(Decision decision, String reason) {
            this.decision = decision;
            this.reason = reason;
        }
        public static Result pass() { return new Result(Decision.PASS, null); }
        public static Result review(String reason) { return new Result(Decision.REVIEW, reason); }
        public static Result reject(String reason) { return new Result(Decision.REJECT, reason); }
    }

    Result scanUrl(String imageUrl);
}
