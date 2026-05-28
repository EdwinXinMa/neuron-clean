package com.echarge.modules.app.service;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 极光推送服务
 * 发透传消息（不弹系统通知），由 App 决定如何展示
 */
@Slf4j
@Service
public class FcmService {

    @Autowired
    private JPushClient jpushClient;

    public void sendSilent(String registrationId, String type, Map<String, String> data) {
        if (registrationId == null || registrationId.isBlank()) {
            return;
        }
        try {
            Message.Builder msgBuilder = Message.newBuilder()
                    .setMsgContent(type)
                    .addExtra("type", type);
            if (data != null) {
                data.forEach(msgBuilder::addExtra);
            }
            PushPayload payload = PushPayload.newBuilder()
                    .setPlatform(Platform.all())
                    .setAudience(Audience.registrationId(registrationId))
                    .setMessage(msgBuilder.build())
                    .build();
            PushResult result = jpushClient.sendPush(payload);
            log.info("[JPush] 发送成功 type={}, msgId={}", type, result.msg_id);
        } catch (Exception e) {
            log.warn("[JPush] 发送失败 type={}, registrationId前8位={}: {}",
                    type,
                    registrationId.length() > 8 ? registrationId.substring(0, 8) : registrationId,
                    e.getMessage());
        }
    }
}
