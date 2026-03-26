package com.echarge.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import com.echarge.config.vo.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;


/**
 * 加载项目配置
 * @author Edwin
 */
@Data
@Component("neuronBaseConfig")
@ConfigurationProperties(prefix = "neuron")
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class NeuronBaseConfig {
    /**
     * 签名密钥串(字典等敏感接口)
     */
    private String signatureSecret = "dd05f1c54d63749eda95f9fa6d49v442a";
    /**
     * 上传模式: local / minio / alioss
     */
    private String uploadType;
    /**
     * 平台安全模式配置
     */
    private Firewall firewall;
    /**
     * shiro拦截排除
     */
    private Shiro shiro;
    /**
     * 上传文件配置
     */
    private Path path;
    /**
     * 前端页面访问地址
     */
    private DomainUrl domainUrl;
    /**
     * 文件预览
     */
    private String fileViewDomain;
    /**
     * minio配置
     */
    private NeuronMinio minio;
}
