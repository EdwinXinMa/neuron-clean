package com.echarge.modules.app.service;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 极光推送服务
 * 发系统通知（分平台 android/ios），App 后台也能收到
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
            AndroidNotification.Builder androidBuilder = AndroidNotification.newBuilder()
                    .setTitle("N3 Lite")
                    .setAlert(type)
                    .addExtra("type", type);
            IosNotification.Builder iosBuilder = IosNotification.newBuilder()
                    .setAlert(type)
                    .setSound("default")
                    .addExtra("type", type);
            if (data != null) {
                data.forEach((k, v) -> {
                    androidBuilder.addExtra(k, v);
                    iosBuilder.addExtra(k, v);
                });
            }

            PushPayload payload = PushPayload.newBuilder()
                    .setPlatform(Platform.android_ios())
                    .setAudience(Audience.registrationId(registrationId))
                    .setNotification(Notification.newBuilder()
                            .addPlatformNotification(androidBuilder.build())
                            .addPlatformNotification(iosBuilder.build())
                            .build())
                    .setOptions(Options.newBuilder()
                            .setApnsProduction(false)
                            .setTimeToLive(600)
                            .build())
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
