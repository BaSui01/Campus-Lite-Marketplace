package com.campus.marketplace.acceptance;

import com.campus.marketplace.common.dto.request.ApproveGoodsRequest;
import com.campus.marketplace.common.dto.request.CreateGoodsRequest;
import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Notification;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.NotificationStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.common.enums.RefundStatus;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.integration.IntegrationTestBase;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.NotificationRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.RefundRequestRepository;
import com.campus.marketplace.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.transaction.TestTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 黑盒验收用例：覆盖核心正向流程与关键异常路径。
 *
 * <p>目标：模拟真实用户行为完成冒烟 + 回归级别的黑盒测试，确保核心链路稳定、权限矩阵和状态机约束正确生效。</p>
 */
@DisplayName("黑盒验收：核心流程与异常路径")
class BlackBoxAcceptanceIT extends IntegrationTestBase {

    private static final String SAMPLE_IMAGE = "https://cdn.campus.test/goods/black-box.jpg";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.campus.marketplace.repository.CampusRepository campusRepository;

    @Autowired
    private RefundRequestRepository refundRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private com.campus.marketplace.repository.GoodsRepository goodsRepository;

    @Autowired
    private com.campus.marketplace.service.GoodsService goodsService;

    private Long defaultCategoryId;

    @BeforeEach
    void setUpCategoryBaseline() {
        defaultCategoryId = categoryRepository.findAll().stream()
                .findFirst()
                .map(Category::getId)
                .orElseThrow(() -> new IllegalStateException("预置分类数据缺失，无法执行黑盒验收"));
    }

    @Test
    @DisplayName("正向：卖家发布→管理员审核→买家下单成功")
    void shouldCompleteOrderFlowWhenGoodsApproved() throws Exception {
        Long goodsId = publishGoodsAs("student2");
        approveGoodsAsAdmin(goodsId);

        String orderNo = placeOrder(goodsId, "student1");
        assertThat(orderNo).isNotBlank();

        mockMvc.perform(get("/api/orders/{orderNo}", orderNo)
                        .with(user("student1").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.orderNo").value(orderNo))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"));
    }

    @Test
    @DisplayName("权限矩阵：普通学生跨校访问被拒绝")
    void shouldRejectCrossCampusAccessWithoutPermission() throws Exception {
        Long goodsId = publishGoodsAs("seller_north");
        approveGoodsAsAdmin(goodsId);

        // 确保跨校权限校验生效：student1 所属校区设置为 default（与 north 不同）
        var defaultCampus = campusRepository.findByCode("default")
                .orElseThrow(() -> new IllegalStateException("预置校区 default 缺失"));
        assertThat(campusRepository.findByCode("north")).as("预置校区 north").isPresent();
        userRepository.findByUsername("student1").ifPresent(user -> {
            user.setCampusId(defaultCampus.getId());
            userRepository.save(user);
            entityManager.flush();
        });

        boolean restartedTransaction = false;
        if (TestTransaction.isActive()) {
            TestTransaction.flagForCommit();
            TestTransaction.end();
            restartedTransaction = true;
        }
        entityManager.clear();

        var student = userRepository.findByUsername("student1").orElseThrow();
        var goods = goodsRepository.findById(goodsId).orElseThrow();
        assertThat(student.getCampusId()).isNotNull();
        assertThat(goods.getCampusId()).isNotNull();
        assertThat(goods.getCampusId()).isNotEqualTo(student.getCampusId());
        var goodsForCheck = goodsRepository.findByIdWithDetails(goodsId).orElseThrow();
        assertThat(goodsForCheck.getCampusId()).isNotNull();
        assertThat(goodsForCheck.getCampusId()).isEqualTo(goods.getCampusId());
        assertThat(goodsForCheck.getCampusId()).isNotEqualTo(student.getCampusId());

        if (restartedTransaction && !TestTransaction.isActive()) {
            TestTransaction.start();
        }

        mockMvc.perform(get("/api/goods/{id}", goodsId)
                        .with(user("student1").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message", containsString("跨校区")));

        mockMvc.perform(get("/api/goods/{id}", goodsId)
                        .with(user("admin")
                                .roles("ADMIN")
                                .authorities(new SimpleGrantedAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(goodsId));
    }

    @Test
    @DisplayName("状态机：同一物品重复下单被阻断")
    void shouldBlockDuplicateOrderForSameGoods() throws Exception {
        Long goodsId = publishGoodsAs("student2");
        approveGoodsAsAdmin(goodsId);

        placeOrder(goodsId, "student1");

        CreateOrderRequest duplicateOrder = new CreateOrderRequest(goodsId, null);
        mockMvc.perform(post("/api/orders")
                        .with(user("buyer_grad").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.GOODS_ALREADY_SOLD.getCode()))
                .andExpect(jsonPath("$.message", containsString("已售出")));
    }

    @Test
    @DisplayName("参数异常：发布物品缺少必填字段触发校验失败")
    void shouldRejectGoodsCreationWhenPayloadInvalid() throws Exception {
        CreateGoodsRequest invalidRequest = new CreateGoodsRequest(
                "短",
                "描述不足",
                new BigDecimal("0.00"),
                null,
                List.of(),
                List.of()
        );

        mockMvc.perform(post("/api/goods")
                        .with(user("student2").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_ERROR.getCode()));
    }

    @Test
    @DisplayName("退款流程：买家申请、管理员审批并通知双方")
    void shouldProcessRefundAndNotifyParties() throws Exception {
        Long goodsId = publishGoodsAs("student2");
        approveGoodsAsAdmin(goodsId);

        String orderNo = placeOrder(goodsId, "student1");
        markOrderPaid(orderNo, PaymentMethod.WECHAT);

        String refundNo = applyRefund(orderNo, "student1", "收到货品破损");

        mockMvc.perform(put("/api/admin/refunds/{refundNo}/approve", refundNo)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()));

        RefundRequest refund = refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new AssertionError("退款单不存在"));
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.REFUNDED);

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new AssertionError("订单不存在"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        User seller = userRepository.findByUsername("student2")
                .orElseThrow(() -> new AssertionError("卖家不存在"));
        User buyer = userRepository.findByUsername("student1")
                .orElseThrow(() -> new AssertionError("买家不存在"));

        List<Notification> sellerNotifications = notificationRepository.findAll().stream()
                .filter(n -> n.getReceiverId().equals(seller.getId()))
                .toList();
        List<Notification> buyerNotifications = notificationRepository.findAll().stream()
                .filter(n -> n.getReceiverId().equals(buyer.getId()))
                .toList();

        assertThat(sellerNotifications)
                .as("卖家应收到退款申请通知")
                .anyMatch(n -> n.getTitle().contains("退款申请"));
        assertThat(buyerNotifications)
                .as("买家应收到退款成功通知")
                .anyMatch(n -> n.getTitle().contains("退款成功"));
    }

    @Test
    @DisplayName("通知中心：查看未读消息并标记为已读")
    void shouldListAndAcknowledgeNotificationsViaApi() throws Exception {
        RefundScenario scenario = prepareRefundScenario();

        assertThat(scenario.sellerNotifications()).isNotEmpty();
        Notification sellerNotification = scenario.sellerNotifications().getFirst();
        try (MockedStatic<SecurityUtil> security = Mockito.mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(scenario.seller().getId());
            security.when(SecurityUtil::getCurrentUsername).thenReturn(scenario.seller().getUsername());

            mockMvc.perform(get("/api/notifications/unread-count")
                            .with(user(scenario.seller().getUsername()).roles("STUDENT")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                    .andExpect(jsonPath("$.data").value(scenario.sellerNotifications().size()));

            mockMvc.perform(put("/api/notifications/mark-read")
                            .with(user(scenario.seller().getUsername()).roles("STUDENT"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(sellerNotification.getId()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                    .andExpect(jsonPath("$.data").value("标记成功"));
        }

        entityManager.flush();
        entityManager.clear();

        Notification refreshed = notificationRepository.findById(sellerNotification.getId())
                .orElseThrow(() -> new AssertionError("通知不存在"));
        assertThat(refreshed.getStatus()).isEqualTo(NotificationStatus.READ);

        assertThat(scenario.buyerNotifications()).isNotEmpty();
        try (MockedStatic<SecurityUtil> security = Mockito.mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(scenario.buyer().getId());
            security.when(SecurityUtil::getCurrentUsername).thenReturn(scenario.buyer().getUsername());

            mockMvc.perform(get("/api/notifications")
                            .with(user(scenario.buyer().getUsername()).roles("STUDENT"))
                            .param("status", "UNREAD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                    .andExpect(jsonPath("$.data.content[0].title", containsString("退款成功")));
        }
    }

    @Test
    @DisplayName("异常：非买家申请退款返回权限不足")
    void shouldRejectRefundApplyWhenNotBuyer() throws Exception {
        Long goodsId = publishGoodsAs("student2");
        approveGoodsAsAdmin(goodsId);
        String orderNo = placeOrder(goodsId, "student1");
        markOrderPaid(orderNo, PaymentMethod.ALIPAY);

        mockMvc.perform(post("/api/orders/{orderNo}/refunds", orderNo)
                        .with(user("buyer_grad").roles("STUDENT"))
                        .param("reason", "想退款")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("note", "测试"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PERMISSION_DENIED.getCode()))
                .andExpect(jsonPath("$.message", containsString("只能为自己的订单")));
    }

    private Long publishGoodsAs(String username) throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        CreateGoodsRequest request = new CreateGoodsRequest(
                "黑盒验收专用物品-" + suffix,
                "黑盒验收自动化测试描述，覆盖关键冒烟路径和回归路径 " + suffix,
                new BigDecimal("199.90"),
                defaultCategoryId,
                List.of(SAMPLE_IMAGE),
                List.of()
        );

        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getCampusId() == null) {
                String campusCode = username.contains("north") ? "north" : "default";
                campusRepository.findByCode(campusCode)
                        .or(() -> campusRepository.findAll().stream().findFirst())
                        .ifPresent(campus -> {
                            user.setCampusId(campus.getId());
                            userRepository.save(user);
                            entityManager.flush();
                        });
            }
        });

        MvcResult result = mockMvc.perform(post("/api/goods")
                        .with(user(username).roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isNumber())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        long goodsId = root.path("data").asLong();
        assertThat(goodsId).isPositive();
        return goodsId;
    }

    private String placeOrder(Long goodsId, String buyerUsername) throws Exception {
        CreateOrderRequest orderRequest = new CreateOrderRequest(goodsId, null);
        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .with(user(buyerUsername).roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isString())
                .andReturn();

        JsonNode orderJson = objectMapper.readTree(orderResult.getResponse().getContentAsString());
        return orderJson.path("data").asText();
    }

    private void markOrderPaid(String orderNo, PaymentMethod method) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new AssertionError("订单不存在"));
        order.setStatus(OrderStatus.PAID);
        order.setPaymentMethod(method.name());
        orderRepository.save(order);
    }

    private String applyRefund(String orderNo, String buyerUsername, String reason) throws Exception {
        MvcResult refundResult = mockMvc.perform(post("/api/orders/{orderNo}/refunds", orderNo)
                        .with(user(buyerUsername).roles("STUDENT"))
                        .param("reason", reason)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("note", "自动化凭证"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isString())
                .andReturn();

        JsonNode refundJson = objectMapper.readTree(refundResult.getResponse().getContentAsString());
        return refundJson.path("data").asText();
    }

    private RefundScenario prepareRefundScenario() throws Exception {
        Long goodsId = publishGoodsAs("student2");
        approveGoodsAsAdmin(goodsId);

        String orderNo = placeOrder(goodsId, "student1");
        markOrderPaid(orderNo, PaymentMethod.WECHAT);
        String refundNo = applyRefund(orderNo, "student1", "质量问题");

        mockMvc.perform(put("/api/admin/refunds/{refundNo}/approve", refundNo)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()));

        User seller = userRepository.findByUsername("student2")
                .orElseThrow(() -> new AssertionError("卖家不存在"));
        User buyer = userRepository.findByUsername("student1")
                .orElseThrow(() -> new AssertionError("买家不存在"));

        List<Notification> sellerNotifications = notificationRepository.findAll().stream()
                .filter(n -> n.getReceiverId().equals(seller.getId()))
                .toList();
        List<Notification> buyerNotifications = notificationRepository.findAll().stream()
                .filter(n -> n.getReceiverId().equals(buyer.getId()))
                .toList();

        return new RefundScenario(orderNo, refundNo, seller, buyer, sellerNotifications, buyerNotifications);
    }

    private void approveGoodsAsAdmin(Long goodsId) throws Exception {
        ApproveGoodsRequest approveRequest = new ApproveGoodsRequest(true, null);
        mockMvc.perform(post("/api/goods/{id}/approve", goodsId)
                        .with(user("admin")
                                .roles("ADMIN")
                                .authorities(new SimpleGrantedAuthority(PermissionCodes.SYSTEM_GOODS_APPROVE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()));
    }

    private record RefundScenario(
            String orderNo,
            String refundNo,
            User seller,
            User buyer,
            List<Notification> sellerNotifications,
            List<Notification> buyerNotifications
    ) {}
}
