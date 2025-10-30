package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.response.ConversationResponse;
import com.campus.marketplace.common.dto.response.MessageResponse;
import com.campus.marketplace.common.enums.MessageStatus;
import com.campus.marketplace.common.enums.MessageType;
import com.campus.marketplace.common.exception.GlobalExceptionHandler;
import com.campus.marketplace.service.MessageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageController MockMvc 测试")
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alice",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        mockMvc = MockMvcBuilders.standaloneSetup(messageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("发送消息成功返回消息ID")
    void sendMessage_success() throws Exception {
        when(messageService.sendMessage(any())).thenReturn(123L);

        mockMvc.perform(post("/api/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiverId": 200,
                                  "messageType": "TEXT",
                                  "content": "Hello!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(123));

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(messageService).sendMessage(captor.capture());
        assertThat(captor.getValue().receiverId()).isEqualTo(200L);
        assertThat(captor.getValue().messageType()).isEqualTo(MessageType.TEXT);
        assertThat(captor.getValue().content()).isEqualTo("Hello!");
    }

    @Test
    @DisplayName("获取未读消息数")
    void getUnreadCount_success() throws Exception {
        when(messageService.getUnreadCount()).thenReturn(5);

        mockMvc.perform(get("/api/messages/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));

        verify(messageService).getUnreadCount();
    }

    @Test
    @DisplayName("分页查询会话列表")
    void listConversations_success() throws Exception {
        ConversationResponse conversation = ConversationResponse.builder()
                .conversationId(3001L)
                .otherUserId(42L)
                .otherUsername("bob")
                .lastMessageContent("Latest")
                .lastMessageTime(LocalDateTime.now())
                .unreadCount(2)
                .build();
        Page<ConversationResponse> page = new PageImpl<>(
                List.of(conversation),
                PageRequest.of(1, 5),
                1
        );
        when(messageService.listConversations(1, 5)).thenReturn(page);

        mockMvc.perform(get("/api/messages/conversations")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].conversationId").value(3001))
                .andExpect(jsonPath("$.data.content[0].otherUsername").value("bob"));

        verify(messageService).listConversations(1, 5);
    }

    @Test
    @DisplayName("分页查询聊天记录")
    void listMessages_success() throws Exception {
        MessageResponse message = MessageResponse.builder()
                .messageId(555L)
                .conversationId(900L)
                .senderId(1L)
                .receiverId(2L)
                .messageType(MessageType.TEXT)
                .content("hi there")
                .status(MessageStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .build();
        Page<MessageResponse> page = new PageImpl<>(
                List.of(message),
                PageRequest.of(2, 10),
                1
        );
        when(messageService.listMessages(900L, 2, 10)).thenReturn(page);

        mockMvc.perform(get("/api/messages/conversations/{conversationId}/messages", 900)
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].messageId").value(555))
                .andExpect(jsonPath("$.data.content[0].content").value("hi there"));

        verify(messageService).listMessages(900L, 2, 10);
    }

    @Test
    @DisplayName("标记会话为已读")
    void markConversationAsRead_success() throws Exception {
        when(messageService.markConversationAsRead(77L)).thenReturn(4);

        mockMvc.perform(post("/api/messages/conversations/{conversationId}/mark-read", 77))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(4));

        verify(messageService).markConversationAsRead(77L);
    }

    @Test
    @DisplayName("撤回消息成功")
    void recallMessage_success() throws Exception {
        mockMvc.perform(post("/api/messages/messages/{messageId}/recall", 808))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(messageService).recallMessage(eq(808L));
    }
}
