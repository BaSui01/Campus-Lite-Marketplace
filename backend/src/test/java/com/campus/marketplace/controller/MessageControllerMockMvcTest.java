package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.response.ConversationResponse;
import com.campus.marketplace.common.dto.response.MessageResponse;
import com.campus.marketplace.common.enums.MessageStatus;
import com.campus.marketplace.common.enums.MessageType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MessageController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("MessageController MockMvc 测试")
class MessageControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @Test
    @DisplayName("发送消息成功返回消息ID")
    @WithMockUser(roles = "USER")
    void sendMessage_success() throws Exception {
        SendMessageRequest request = new SendMessageRequest(10086L, MessageType.TEXT, "你好");
        when(messageService.sendMessage(any(SendMessageRequest.class))).thenReturn(42L);

        mockMvc.perform(post("/api/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(42));

        verify(messageService).sendMessage(eq(request));
    }

    @Test
    @DisplayName("非 USER 角色调用发送消息接口返回 403")
    @WithMockUser(roles = "STUDENT")
    void sendMessage_forbidden() throws Exception {
        SendMessageRequest request = new SendMessageRequest(10086L, MessageType.TEXT, "你好");

        mockMvc.perform(post("/api/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(messageService, never()).sendMessage(any());
    }

    @Test
    @DisplayName("发送消息缺少必填内容返回 400")
    @WithMockUser(roles = "USER")
    void sendMessage_validationError() throws Exception {
        String invalidJson = """
                {
                  "receiverId": 10086,
                  "messageType": "TEXT",
                  "content": ""
                }
                """;

        mockMvc.perform(post("/api/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_ERROR.getCode()));

        verify(messageService, never()).sendMessage(any());
    }

    @Test
    @DisplayName("查询会话列表返回分页结果")
    @WithMockUser(roles = "USER")
    void listConversations_success() throws Exception {
        ConversationResponse conversation = ConversationResponse.builder()
                .conversationId(20001L)
                .otherUserId(30001L)
                .otherUsername("alice")
                .lastMessageContent("hi")
                .lastMessageTime(LocalDateTime.parse("2025-10-29T10:15:30"))
                .unreadCount(2)
                .build();
        when(messageService.listConversations(0, 20))
                .thenReturn(new PageImpl<>(List.of(conversation)));

        mockMvc.perform(get("/api/messages/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].conversationId").value(20001))
                .andExpect(jsonPath("$.data.content[0].unreadCount").value(2));
    }

    @Test
    @DisplayName("查询聊天记录会话不存在返回业务 404")
    @WithMockUser(roles = "USER")
    void listMessages_notFound() throws Exception {
        when(messageService.listMessages(20001L, 0, 50))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND, "会话不存在"));

        mockMvc.perform(get("/api/messages/conversations/{conversationId}/messages", 20001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("会话不存在"));
    }

    @Test
    @DisplayName("标记会话为已读返回已读条数")
    @WithMockUser(roles = "USER")
    void markConversationAsRead_success() throws Exception {
        when(messageService.markConversationAsRead(20001L)).thenReturn(5);

        mockMvc.perform(post("/api/messages/conversations/{conversationId}/mark-read", 20001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));

        verify(messageService).markConversationAsRead(20001L);
    }

    @Test
    @DisplayName("撤回消息成功返回统一响应")
    @WithMockUser(roles = "USER")
    void recallMessage_success() throws Exception {
        mockMvc.perform(post("/api/messages/messages/{messageId}/recall", 30001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(messageService).recallMessage(30001L);
    }

    @Test
    @DisplayName("查询未读消息数返回整数")
    @WithMockUser(roles = "USER")
    void getUnreadCount_success() throws Exception {
        when(messageService.getUnreadCount()).thenReturn(7);

        mockMvc.perform(get("/api/messages/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(7));
    }

    @Test
    @DisplayName("查询聊天记录成功返回消息内容")
    @WithMockUser(roles = "USER")
    void listMessages_success() throws Exception {
        MessageResponse message = MessageResponse.builder()
                .messageId(50001L)
                .conversationId(20001L)
                .messageType(MessageType.TEXT)
                .content("hello")
                .status(MessageStatus.UNREAD)
                .createdAt(LocalDateTime.parse("2025-10-29T09:00:00"))
                .build();
        when(messageService.listMessages(20001L, 0, 50))
                .thenReturn(new PageImpl<>(List.of(message)));

        mockMvc.perform(get("/api/messages/conversations/{conversationId}/messages", 20001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].messageId").value(50001))
                .andExpect(jsonPath("$.data.content[0].content").value("hello"));
    }
}
