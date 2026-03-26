package com.echarge.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.echarge.common.api.CommonApi;
import com.echarge.common.constant.CacheConstant;
import com.echarge.common.constant.CommonConstant;
import com.echarge.common.exception.NeuronBoot401Exception;
import com.echarge.common.system.util.JwtUtil;
import com.echarge.common.system.vo.LoginUser;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Author Edwin
 * @Date2026-03-22
 * @Description: 编程校验token有效性
 * @author Edwin
 */
@Slf4j
public class TokenUtils {

    /**
     * 获取 request 里传递的 token
     *
     * @param request
     * @return
     */
    public static String getTokenByRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String token = request.getParameter("token");
        if (token == null) {
            token = request.getHeader("X-Access-Token");
        }
        return token;
    }
    
    /**
     * 获取 request 里传递的 token
     * @return
     */
    public static String getTokenByRequest() {
        String token = null;
        try {
            HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
            token = TokenUtils.getTokenByRequest(request);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return token;
    }

    /**
     * 获取 request 里传递的 tenantId (租户ID)
     *
     * @param request
     * @return
     */
    public static String getTenantIdByRequest(HttpServletRequest request) {
        String tenantId = null;
        if (tenantId == null) {
            tenantId = OConvertUtils.getString(request.getHeader(CommonConstant.TENANT_ID));
        }

        if (OConvertUtils.isNotEmpty(tenantId) && "undefined".equals(tenantId)) {
            return null;
        }
        return tenantId;
    }

    /**
     * 获取 request 里传递的 lowAppId (低代码应用ID)
     *
     * @param request
     * @return
     */
    public static String getLowAppIdByRequest(HttpServletRequest request) {
        String lowAppId = null;
        if (lowAppId == null) {
            // tenant removed
        }
        return lowAppId;
    }

    /**
     * 验证Token
     */
    public static boolean verifyToken(HttpServletRequest request, CommonApi commonApi, RedisUtil redisUtil) {
        log.debug(" -- url --" + request.getRequestURL());
        String token = getTokenByRequest(request);
        return TokenUtils.verifyToken(token, commonApi, redisUtil);
    }

    /**
     * 验证Token
     */
    public static boolean verifyToken(String token, CommonApi commonApi, RedisUtil redisUtil) {
        if (StringUtils.isBlank(token)) {
            throw new NeuronBoot401Exception("token不能为空!");
        }

        // 解密获得username，用于和数据库进行对比
        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new NeuronBoot401Exception("token非法无效!");
        }

        // 查询用户信息
        LoginUser user = TokenUtils.getLoginUser(username, commonApi, redisUtil);
        //LoginUser user = commonApi.getUserByName(username);
        if (user == null) {
            throw new NeuronBoot401Exception("用户不存在!");
        }
        // 判断用户状态
        if (user.getStatus() != 1) {
            throw new NeuronBoot401Exception("账号已被锁定,请联系管理员!");
        }
        // 校验token是否超时失效 & 或者账号密码是否错误
        if (!jwtTokenRefresh(token, username, user.getPassword(), redisUtil)) {
            // 用户登录Token过期提示信息
            String userLoginTokenErrorMsg = OConvertUtils.getString(redisUtil.get(CommonConstant.PREFIX_USER_TOKEN_ERROR_MSG + token));
            throw new NeuronBoot401Exception(OConvertUtils.isEmpty(userLoginTokenErrorMsg)? CommonConstant.TOKEN_IS_INVALID_MSG: userLoginTokenErrorMsg);
        }
        return true;
    }

    /**
     * 刷新token（保证用户在线操作不掉线）
     * @param token
     * @param userName
     * @param passWord
     * @param redisUtil
     * @return
     */
    private static boolean jwtTokenRefresh(String token, String userName, String passWord, RedisUtil redisUtil) {
        String cacheToken = OConvertUtils.getString(redisUtil.get(CommonConstant.PREFIX_USER_TOKEN + token));
        if (OConvertUtils.isNotEmpty(cacheToken)) {
            // 校验token有效性
            if (!JwtUtil.verify(cacheToken, userName, passWord)) {
                // 从token中解析客户端类型，保持续期时使用相同的客户端类型
                String clientType = JwtUtil.getClientType(token);
                String newAuthorization = JwtUtil.sign(userName, passWord, clientType);
                // 根据客户端类型设置对应的缓存有效时间
                long expireTime = CommonConstant.CLIENT_TYPE_APP.equalsIgnoreCase(clientType) 
                    ? JwtUtil.APP_EXPIRE_TIME * 2 / 1000 
                    : JwtUtil.EXPIRE_TIME * 2 / 1000;
                redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, newAuthorization);
                redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, expireTime);
            }
            return true;
        }
        return false;
    }

    /**
     * 获取登录用户
     *
     * @param commonApi
     * @param username
     * @return
     */
    public static LoginUser getLoginUser(String username, CommonApi commonApi, RedisUtil redisUtil) {
        LoginUser loginUser = null;
        String loginUserKey = CacheConstant.SYS_USERS_CACHE + "::" + username;
        //【重要】此处通过redis原生获取缓存用户，是为了解决微服务下system服务挂了，其他服务互调不通问题---
        if (redisUtil.hasKey(loginUserKey)) {
            try {
                loginUser = (LoginUser) redisUtil.get(loginUserKey);
                //解密用户
                // SensitiveInfoUtil removed
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 查询用户信息
            loginUser = commonApi.getUserByName(username);
        }
        return loginUser;
    }
}
