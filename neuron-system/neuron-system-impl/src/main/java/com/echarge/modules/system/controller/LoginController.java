package com.echarge.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import com.echarge.common.api.vo.Result;
import com.echarge.common.constant.CommonConstant;
import com.echarge.common.system.util.JwtUtil;
import com.echarge.common.util.PasswordUtil;
import com.echarge.common.util.RedisUtil;
import com.echarge.common.util.OConvertUtils;
import com.echarge.modules.system.entity.SysUser;
import com.echarge.modules.system.model.SysLoginModel;
import com.echarge.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 登录控制器（精简版）
 * @author Edwin
 */
@Slf4j
@RestController
@RequestMapping("/sys")
public class LoginController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<JSONObject> login(@RequestBody SysLoginModel sysLoginModel) {
        String username = sysLoginModel.getUsername();
        String password = sysLoginModel.getPassword();

        // 查询用户
        SysUser sysUser = sysUserService.getUserByName(username);

        // 校验用户有效性
        Result result = sysUserService.checkUserIsEffective(sysUser);
        if (!result.isSuccess()) {
            return result;
        }

        // 校验密码
        String userPassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
        if (!userPassword.equals(sysUser.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        // 生成 Token
        String token = JwtUtil.sign(username, sysUser.getPassword());

        // 设置登录缓存
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);

        JSONObject obj = new JSONObject();
        obj.put("token", token);
        obj.put("userInfo", sysUser);

        log.info("用户 {} 登录成功", username);
        return Result.ok(obj);
    }

    /**
     * 登出
     */
    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<Object> logout() {
        return Result.ok("退出登录成功");
    }
}
