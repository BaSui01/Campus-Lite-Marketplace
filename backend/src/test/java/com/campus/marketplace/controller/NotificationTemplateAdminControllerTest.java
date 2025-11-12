package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.entity.NotificationTemplate;
import com.campus.marketplace.common.enums.NotificationChannel;
import com.campus.marketplace.repository.NotificationTemplateRepository;
import com.campus.marketplace.service.NotificationTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = NotificationTemplateAdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("NotificationTemplateAdminController MockMvc 测试")
class NotificationTemplateAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationTemplateRepository repository;

    @MockBean
    private NotificationTemplateService templateService;

    @Test
    @DisplayName("具备权限的管理员可以查询模板列表")
    @WithMockUser(authorities = "system:rate-limit:manage")
    void list_success() throws Exception {
        NotificationTemplate tpl = NotificationTemplate.builder()
                .code("ORDER_PAID")
                .titleKey("tpl.order.title")
                .contentKey("tpl.order.content")
                .channels("IN_APP,EMAIL")
                .build();
        when(repository.findAll()).thenReturn(List.of(tpl));

        mockMvc.perform(get("/admin/notification-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("ORDER_PAID"));

        verify(repository).findAll();
    }

    @Test
    @DisplayName("保存模板时写入仓库")
    @WithMockUser(authorities = "system:rate-limit:manage")
    void save_success() throws Exception {
        NotificationTemplate body = NotificationTemplate.builder()
                .code("NEW_MESSAGE")
                .titleKey("title")
                .contentKey("content")
                .channels("IN_APP")
                .build();
        when(repository.save(any(NotificationTemplate.class))).thenReturn(body);

        mockMvc.perform(post("/admin/notification-templates")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("NEW_MESSAGE"));

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("NEW_MESSAGE");
    }

    @Test
    @DisplayName("删除模板成功")
    @WithMockUser(authorities = "system:rate-limit:manage")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/admin/notification-templates/{id}", 9L))
                .andExpect(status().isOk());

        verify(repository).deleteById(9L);
    }

    @Test
    @DisplayName("渲染模板返回标题与内容")
    @WithMockUser(authorities = "system:rate-limit:manage")
    void render_success() throws Exception {
        NotificationTemplateService.Rendered rendered = new NotificationTemplateService.Rendered(
                "标题", "内容", Set.of(NotificationChannel.IN_APP, NotificationChannel.EMAIL));
        when(templateService.render(eq("ORDER_PAID"), any(Locale.class), eq(Map.of("orderNo", "1001"))))
                .thenReturn(rendered);

        mockMvc.perform(post("/admin/notification-templates/render/{code}", "ORDER_PAID")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(Map.of("orderNo", "1001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("标题"))
                .andExpect(jsonPath("$.data.channels").isArray());

        verify(templateService).render(eq("ORDER_PAID"), any(Locale.class), eq(Map.of("orderNo", "1001")));
    }
}
