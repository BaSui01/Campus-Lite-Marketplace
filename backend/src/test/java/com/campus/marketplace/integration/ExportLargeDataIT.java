package com.campus.marketplace.integration;

import com.campus.marketplace.common.entity.ExportJob;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.repository.ExportJobRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.service.ExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("导出-大数据集成测试（多页流式写出与下载）")
@TestPropertySource(properties = {
        "export.maxRows=10000"
})
class ExportLargeDataIT extends IntegrationTestBase {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ExportService exportService;

    @Autowired
    private ExportJobRepository jobRepo;

    @Test
    @DisplayName("创建 3000 条商品并导出，校验文件行数与下载有效")
    @Transactional
    void exportGoodsLargeVolume() throws Exception {
        List<Goods> batch = new ArrayList<>();
        for (int i = 1; i <= 3000; i++) {
            Goods g = Goods.builder()
                    .title("G" + i)
                    .description("desc" + i)
                    .price(new java.math.BigDecimal("9.99"))
                    .categoryId(1L)
                    .sellerId(1L)
                    .status(GoodsStatus.APPROVED)
                    .build();
            batch.add(g);
            if (batch.size() == 200) {
                goodsRepository.saveAll(batch);
                goodsRepository.flush();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            goodsRepository.saveAll(batch);
            goodsRepository.flush();
            batch.clear();
        }

        Long jobId = exportService.requestExport("GOODS", null);

        ExportJob job = jobRepo.findById(jobId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo("SUCCESS");
        assertThat(job.getFilePath()).isNotBlank();
        assertThat(job.getDownloadToken()).isNotBlank();
        assertThat(job.getExpireAt()).isAfter(Instant.now());

        File f = new File(job.getFilePath());
        assertThat(f).exists();
        String content = java.nio.file.Files.readString(f.toPath(), StandardCharsets.UTF_8);
        long lines = content.lines().count();
        assertThat(lines).isGreaterThanOrEqualTo(3001);

        byte[] bytes = exportService.download(job.getDownloadToken());
        assertThat(bytes.length).isGreaterThan(100);
    }
}
