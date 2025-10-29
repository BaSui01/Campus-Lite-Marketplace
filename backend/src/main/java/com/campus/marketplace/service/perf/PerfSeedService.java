package com.campus.marketplace.service.perf;

import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 性能环境数据预置服务。
 *
 * <p>在 perf Profile 下用于批量构造超时订单数据，供 JMeter 等压测脚本消费。</p>
 */
@Slf4j
@Service
@Profile("perf")
@RequiredArgsConstructor
public class PerfSeedService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final GoodsRepository goodsRepository;
    private final OrderRepository orderRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final String DEFAULT_SELLER_USERNAME = "seller_north";
    private static final String DEFAULT_BUYER_USERNAME = "buyer_grad";

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("199.99");

    /**
     * 清理之前压测生成的数据（以 PERF- 前缀标识）。
     *
     * @return 被删除的记录总数
     */
    @Transactional
    public int clearTimeoutFixtures() {
        int orderRows = 0;
        int goodsRows = 0;
        try {
            orderRows = jdbcTemplate.update("DELETE FROM t_order WHERE order_no LIKE 'PERF-%'");
        } catch (Exception ex) {
            log.debug("压测订单清理跳过，表尚未初始化: {}", ex.getMessage());
        }
        try {
            goodsRows = jdbcTemplate.update("DELETE FROM t_goods WHERE title LIKE '性能压测商品 %'");
        } catch (Exception ex) {
            log.debug("压测商品清理跳过，表尚未初始化: {}", ex.getMessage());
        }
        log.info("性能环境清理完成: orders={}, goods={}", orderRows, goodsRows);
        return orderRows + goodsRows;
    }

    /**
     * 批量构造超时订单，满足 cancelTimeoutOrders 的真实业务数据需求。
     *
     * @param count 期望构造的订单数量
     * @return 实际构造的订单数量
     */
    @Transactional
    public int seedTimeoutOrders(int count) {
        if (count <= 0) {
            return 0;
        }

        User seller = userRepository.findByUsername(DEFAULT_SELLER_USERNAME)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "性能压测卖家账号缺失: " + DEFAULT_SELLER_USERNAME));
        User buyer = userRepository.findByUsername(DEFAULT_BUYER_USERNAME)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "性能压测买家账号缺失: " + DEFAULT_BUYER_USERNAME));

        Category category = categoryRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "性能压测缺少基础分类数据"));

        List<Goods> goodsBatch = new ArrayList<>(count);
        List<Order> orderBatch = new ArrayList<>(count);
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(65);

        for (int i = 0; i < count; i++) {
            Goods goods = Goods.builder()
                    .title("性能压测商品 " + UUID.randomUUID())
                    .description("Perf dataset seed for timeout orders")
                    .price(DEFAULT_PRICE)
                    .categoryId(category.getId())
                    .sellerId(seller.getId())
                    .campusId(seller.getCampusId())
                    .status(GoodsStatus.SOLD)
                    .images(new String[]{"https://cdn.campus.test/perf.png"})
                    .build();
            goodsBatch.add(goods);
        }

        goodsRepository.saveAll(goodsBatch);
        goodsRepository.flush();

        for (Goods goods : goodsBatch) {
            Order order = Order.builder()
                    .orderNo("PERF-" + UUID.randomUUID())
                    .goodsId(goods.getId())
                    .buyerId(buyer.getId())
                    .sellerId(seller.getId())
                    .campusId(buyer.getCampusId())
                    .amount(DEFAULT_PRICE)
                    .actualAmount(DEFAULT_PRICE)
                    .status(OrderStatus.PENDING_PAYMENT)
                    .build();
            orderBatch.add(order);
        }

        orderRepository.saveAll(orderBatch);
        orderRepository.flush();

        int adjusted = 0;
        for (Order order : orderBatch) {
            LocalDateTime createdAt = timeoutThreshold.minusSeconds(adjusted % 120);
            Timestamp ts = Timestamp.valueOf(createdAt);
            jdbcTemplate.update("UPDATE t_order SET created_at = ?, updated_at = ? WHERE id = ?", ts, ts, order.getId());
            adjusted++;
        }

        log.info("性能环境已生成超时订单数据: orders={}", orderBatch.size());
        return orderBatch.size();
    }
}
