/*
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.stereotype.Component
 */
package com.echarge.common.modules.redis.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="neuron.redis")
public class NeuronRedisCacheTtls {
    private Map<String, Long> cacheTtls = new HashMap<String, Long>();
    private boolean listenerEnabled = true;

    public Map<String, Long> getCacheTtls() {
        return this.cacheTtls;
    }

    public void setCacheTtls(Map<String, Long> cacheTtls) {
        this.cacheTtls = cacheTtls;
    }

    public boolean isListenerEnabled() {
        return this.listenerEnabled;
    }

    public void setListenerEnabled(boolean listenerEnabled) {
        this.listenerEnabled = listenerEnabled;
    }
}

