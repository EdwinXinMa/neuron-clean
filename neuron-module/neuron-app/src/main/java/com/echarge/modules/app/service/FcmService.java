package com.echarge.modules.app.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * FCM 推送服务
 * 只发静默消息（data-only），不弹系统通知，由 App 决定如何展示
 */
@Slf4j
@Service
public class FcmService {

    public void sendSilent(String fcmToken, String type, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            return;
        }
        try {
            Message.Builder builder = Message.builder()
                    .setToken(fcmToken)
                    .putData("type", type);
            if (data != null) {
                data.forEach(builder::putData);
            }
            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.info("[FCM] 发送成功 type={}, response={}", type, response);
        } catch (Exception e) {
            log.warn("[FCM] 发送失败 type={}, token前8位={}: {}", type,
                    fcmToken.length() > 8 ? fcmToken.substring(0, 8) : fcmToken, e.getMessage());
        }
    }
}
