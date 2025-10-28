package com.campus.marketplace.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.campus.marketplace.common.config.SmsProperties;
import com.campus.marketplace.service.impl.AliyunSmsService;
import com.campus.marketplace.service.impl.DevLoggingSmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("短信服务实现测试")
class SmsServiceImplsTest {

    @Test
    @DisplayName("DevLoggingSmsService 发送不抛异常")
    void devLoggingSmsService_send() {
        DevLoggingSmsService service = new DevLoggingSmsService();
        assertThatCode(() -> service.send("13800000000", "TEMP", Map.of("code", "123456")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("AliyunSmsService 发送成功返回不抛异常")
    void aliyunSmsService_success() throws Exception {
        SmsProperties props = buildProps();
        AliyunSmsService service = new AliyunSmsService(props);

        SendSmsResponseBody body = new SendSmsResponseBody();
        body.setCode("OK");
        body.setMessage("success");
        SendSmsResponse response = new SendSmsResponse();
        response.setBody(body);

        try (MockedConstruction<Client> mocked = mockConstruction(Client.class, (mock, context) ->
                when(mock.sendSms(any(SendSmsRequest.class))).thenReturn(response)
        )) {
            assertThatCode(() -> service.send("13800000000", "TEMP", Map.of("code", "123")))
                    .doesNotThrowAnyException();

            Client constructed = mocked.constructed().getFirst();
            verify(constructed).sendSms(any(SendSmsRequest.class));
        }
    }

    @Test
    @DisplayName("AliyunSmsService 响应失败抛异常")
    void aliyunSmsService_failureResponse() {
        SmsProperties props = buildProps();
        AliyunSmsService service = new AliyunSmsService(props);

        SendSmsResponseBody body = new SendSmsResponseBody();
        body.setCode("BUSINESS_LIMIT_CONTROL");
        body.setMessage("too many requests");
        SendSmsResponse response = new SendSmsResponse();
        response.setBody(body);

        try (MockedConstruction<Client> mocked = mockConstruction(Client.class, (mock, context) ->
                when(mock.sendSms(any(SendSmsRequest.class))).thenReturn(response)
        )) {
            assertThatThrownBy(() -> service.send("13800000000", "TEMP", Map.of()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("短信发送异常");
        }
    }

    @Test
    @DisplayName("AliyunSmsService 调用异常抛短信异常")
    void aliyunSmsService_exception() throws Exception {
        SmsProperties props = buildProps();
        AliyunSmsService service = new AliyunSmsService(props);

        try (MockedConstruction<Client> mocked = mockConstruction(Client.class, (mock, context) ->
                when(mock.sendSms(any(SendSmsRequest.class))).thenThrow(new RuntimeException("network"))
        )) {
            assertThatThrownBy(() -> service.send("13800000000", "TEMP", Map.of()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("短信发送异常");
        }
    }

    private static SmsProperties buildProps() {
        SmsProperties props = new SmsProperties();
        props.setAccessKeyId("test-id");
        props.setAccessKeySecret("test-secret");
        props.setSignName("Campus");
        props.setTemplateRegister("TEMP");
        props.setTemplateReset("TEMP2");
        return props;
    }
}
