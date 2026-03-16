package com.echarge.config;

import com.echarge.config.vo.GaoDeApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 高德账号配置
 */
@Lazy(false)
@Configuration("neuronGaodeBaseConfig")
@ConfigurationProperties(prefix = "neuron.jmreport")
public class NeuronGaodeBaseConfig {

    /**
     * 高德开放API配置
     */
    private GaoDeApi gaoDeApi;

    public GaoDeApi getGaoDeApi() {
        return gaoDeApi;
    }

    public void setGaoDeApi(GaoDeApi gaoDeApi) {
        this.gaoDeApi = gaoDeApi;
    }
    
}
