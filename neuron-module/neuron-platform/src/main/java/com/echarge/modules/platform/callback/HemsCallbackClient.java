package com.echarge.modules.platform.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * H 平台事件回调 HTTP 客户端
 *
 * <p>当 N 平台收到设备上/下线事件时，通过此类向 H 平台推送回调。
 */
@Slf4j
@Component
public class HemsCallbackClient {

    private static final String KEY_HEADER = "X-Internal-Key";
    private static final String EVENT_PATH = "/internal/v1/n3lite/events";

    private final HemsCallbackProperties properties;
    private final RestClient restClient;

    public HemsCallbackClient(HemsCallbackProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    /**
     * 向 H 平台推送设备事件
     *
     * @param eventType       事件类型：DEVICE_ONLINE / DEVICE_OFFLINE
     * @param sn              设备序列号
     * @param firmwareVersion 固件版本（可为 null）
     */
    public void sendEvent(String eventType, String sn, String firmwareVersion) {
        if (!properties.isEnabled()) {
            return;
        }
        if (properties.getCallbackUrl() == null || properties.getCallbackUrl().isBlank()) {
            log.warn("【推送H平台】callbackUrl 未配置，跳过 eventType={} sn={}", eventType, sn);
            return;
        }

        String url = properties.getCallbackUrl() + EVENT_PATH;
        log.info("【推送H平台】POST {} eventType={} sn={} firmware={}", url, eventType, sn, firmwareVersion);

        Map<String, Object> body = new HashMap<>();
        body.put("eventType", eventType);
        body.put("sn", sn);
        body.put("firmwareVersion", firmwareVersion);
        body.put("timestamp", Instant.now().toString());

        try {
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(KEY_HEADER, properties.getCallbackKey())
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("【推送H平台成功】eventType={} sn={}", eventType, sn);
        } catch (RestClientException e) {
            log.error("【推送H平台失败】eventType={} sn={} err={}", eventType, sn, e.getMessage());
        }
    }
}
