package com.echarge.config.vo;

import lombok.Data;

/**
 * 平台安全配置
 * @author Edwin
 */
@Data
public class Firewall {
    /**
     * 数据源安全 (开启后，数据源为必填)
     */
    private Boolean dataSourceSafe = false;
    /**
     * 低代码模式（dev / prod）
     */
    private String lowCodeMode;
    /**
     * 是否允许同一账号多地同时登录
     */
    private Boolean isConcurrent = true;
    /**
     * 是否开启默认密码登录提醒
     */
    private Boolean enableDefaultPwdCheck = false;
    /**
     * 是否开启登录验证码校验
     */
    private Boolean enableLoginCaptcha = true;
}
