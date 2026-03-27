package com.echarge.protocol.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Edwin
 */
@Data
@Component
@ConfigurationProperties(prefix = "neuron.protocol")
public class ProtocolProperties {

    private boolean enabled = true;
    private int port = 9001;
    private String path = "/ocpp";
    private int bossThreads = 1;
    private int workerThreads = 0;
    private int maxFrameSize = 65536;

    /** 是否开启连接认证 */
    private boolean authEnabled = false;
    /** 全局连接密码 */
    private String authPassword = "";
}
