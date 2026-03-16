package org.jeecg.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Compatibility bean for hibernate-re jar.
 * Standalone config that binds to the same "neuron" YAML prefix.
 * The jar autowires this by type; project code uses NeuronBaseConfig.
 */
@Component("jeecgBaseConfig")
@ConfigurationProperties(prefix = "neuron")
public class JeecgBaseConfig {
    private String signatureSecret = "dd05f1c54d63749eda95f9fa6d49v442a";
    private String customResourcePrefixPath;
    private String signUrls;
    private String uploadType;
    private org.jeecg.config.vo.Firewall firewall;
    private com.echarge.config.vo.Shiro shiro;
    private com.echarge.config.vo.Path path;
    private com.echarge.config.vo.DomainUrl domainUrl;
    private String fileViewDomain;
    private com.echarge.config.vo.Elasticsearch elasticsearch;
    private com.echarge.config.vo.WeiXinPay weiXinPay;
    private com.echarge.config.vo.BaiduApi baiduApi;
    private com.echarge.config.vo.NeuronMinio minio;
    private com.echarge.config.vo.NeuronOSS oss;
    private String smsSendType = "aliyun";
    private com.echarge.config.tencent.NeuronTencent tencent;

    public String getSignatureSecret() { return signatureSecret; }
    public void setSignatureSecret(String v) { this.signatureSecret = v; }
    public String getCustomResourcePrefixPath() { return customResourcePrefixPath; }
    public void setCustomResourcePrefixPath(String v) { this.customResourcePrefixPath = v; }
    public String getSignUrls() { return signUrls; }
    public void setSignUrls(String v) { this.signUrls = v; }
    public String getUploadType() { return uploadType; }
    public void setUploadType(String v) { this.uploadType = v; }
    public org.jeecg.config.vo.Firewall getFirewall() { return firewall; }
    public void setFirewall(org.jeecg.config.vo.Firewall v) { this.firewall = v; }
    public com.echarge.config.vo.Shiro getShiro() { return shiro; }
    public void setShiro(com.echarge.config.vo.Shiro v) { this.shiro = v; }
    public com.echarge.config.vo.Path getPath() { return path; }
    public void setPath(com.echarge.config.vo.Path v) { this.path = v; }
    public com.echarge.config.vo.DomainUrl getDomainUrl() { return domainUrl; }
    public void setDomainUrl(com.echarge.config.vo.DomainUrl v) { this.domainUrl = v; }
    public String getFileViewDomain() { return fileViewDomain; }
    public void setFileViewDomain(String v) { this.fileViewDomain = v; }
    public com.echarge.config.vo.Elasticsearch getElasticsearch() { return elasticsearch; }
    public void setElasticsearch(com.echarge.config.vo.Elasticsearch v) { this.elasticsearch = v; }
    public com.echarge.config.vo.WeiXinPay getWeiXinPay() { return weiXinPay; }
    public void setWeiXinPay(com.echarge.config.vo.WeiXinPay v) { this.weiXinPay = v; }
    public com.echarge.config.vo.BaiduApi getBaiduApi() { return baiduApi; }
    public void setBaiduApi(com.echarge.config.vo.BaiduApi v) { this.baiduApi = v; }
    public com.echarge.config.vo.NeuronMinio getMinio() { return minio; }
    public void setMinio(com.echarge.config.vo.NeuronMinio v) { this.minio = v; }
    public com.echarge.config.vo.NeuronOSS getOss() { return oss; }
    public void setOss(com.echarge.config.vo.NeuronOSS v) { this.oss = v; }
    public String getSmsSendType() { return smsSendType; }
    public void setSmsSendType(String v) { this.smsSendType = v; }
    public com.echarge.config.tencent.NeuronTencent getTencent() { return tencent; }
    public void setTencent(com.echarge.config.tencent.NeuronTencent v) { this.tencent = v; }
}
