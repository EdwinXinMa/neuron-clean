package com.echarge.modules.platform.callback;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * H 平台事件回调配置
 */
@Component
@ConfigurationProperties(prefix = "neuron.hems-callback")
public class HemsCallbackProperties {

    /** 回调开关：false 时不发送任何回调请求 */
    private boolean enabled = false;

    /** H 平台内部接口基础 URL，例如 http://127.0.0.1:8080 */
    private String callbackUrl;

    /** H 平台内部接口鉴权 Key，对应 X-Internal-Key 请求头 */
    private String callbackKey;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getCallbackKey() {
        return callbackKey;
    }

    public void setCallbackKey(String callbackKey) {
        this.callbackKey = callbackKey;
    }
}
