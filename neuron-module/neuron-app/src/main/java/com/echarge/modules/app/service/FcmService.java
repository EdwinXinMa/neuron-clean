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
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public void sendSilent(List<String> registrationIds, String type, String title, String alert, Map<String, String> data) {
        if (registrationIds == null || registrationIds.isEmpty()) {
            return;
        }
        try {
            JsonObject intent = new JsonObject();
            intent.addProperty("url", "intent:#Intent;component=com.xuheng.charge/com.xuheng.charge.MainActivity;end");

            AndroidNotification.Builder androidBuilder = AndroidNotification.newBuilder()
                    .setTitle(title)
                    .setAlert(alert)
                    .setIntent(intent)
                    .addExtra("type", type);
            IosNotification.Builder iosBuilder = IosNotification.newBuilder()
                    .setAlert(title + " - " + alert)
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
                    .setAudience(Audience.registrationId(registrationIds))
                    .setNotification(Notification.newBuilder()
                            .addPlatformNotification(androidBuilder.build())
                            .addPlatformNotification(iosBuilder.build())
                            .build())
                    .setOptions(Options.newBuilder()
                            .setApnsProduction(true)
                            .setTimeToLive(600)
                            .build())
                    .build();
            log.info("[JPush] payload={}", payload.toJSON());
            PushResult result = jpushClient.sendPush(payload);
            log.info("[JPush] 发送成功 type={}, msgId={}, targets={}", type, result.msg_id, registrationIds.size());
        } catch (Exception e) {
            log.warn("[JPush] 发送失败 type={}, targets={}: {}", type, registrationIds.size(), e.getMessage());
        }
    }
}
