package com.echarge.config.oss;

import jakarta.annotation.PostConstruct;
import com.echarge.common.util.oss.OssBootUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 云存储 配置
 * @author: jeecg-boot
 */
@Lazy(false)
@Configuration
@ConditionalOnProperty(prefix = "neuron.oss", name = "endpoint")
public class OssConfiguration {

    @Value("${neuron.oss.endpoint}")
    private String endpoint;
    @Value("${neuron.oss.accessKey}")
    private String accessKeyId;
    @Value("${neuron.oss.secretKey}")
    private String accessKeySecret;
    @Value("${neuron.oss.bucketName}")
    private String bucketName;
    @Value("${neuron.oss.staticDomain:}")
    private String staticDomain;


    @PostConstruct
    public void initOssBootConfiguration() {
        OssBootUtil.setEndPoint(endpoint);
        OssBootUtil.setAccessKeyId(accessKeyId);
        OssBootUtil.setAccessKeySecret(accessKeySecret);
        OssBootUtil.setBucketName(bucketName);
        OssBootUtil.setStaticDomain(staticDomain);
    }
}