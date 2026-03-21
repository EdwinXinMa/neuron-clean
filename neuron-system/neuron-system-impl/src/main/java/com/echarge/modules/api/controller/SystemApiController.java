package com.echarge.modules.api.controller;

import lombok.extern.slf4j.Slf4j;
import com.echarge.common.system.vo.LoginUser;
import com.echarge.common.system.vo.SysUserCacheInfo;
import com.echarge.modules.system.service.impl.SysBaseApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 系统模块内部 API（精简版）
 */
@Slf4j
@RestController
@RequestMapping("/sys/api")
public class SystemApiController {

    @Autowired
    private SysBaseApiImpl sysBaseApi;

    @GetMapping("/getUserByName")
    public LoginUser getUserByName(@RequestParam("username") String username) {
        return sysBaseApi.getUserByName(username);
    }

    @GetMapping("/getUserIdByName")
    public String getUserIdByName(@RequestParam("username") String username) {
        return sysBaseApi.getUserIdByName(username);
    }

    @GetMapping("/queryUserRoles")
    public Set<String> queryUserRoles(@RequestParam("username") String username) {
        return sysBaseApi.queryUserRoles(username);
    }

    @GetMapping("/queryUserAuths")
    public Set<String> queryUserAuths(@RequestParam("userId") String userId) {
        return sysBaseApi.queryUserAuths(userId);
    }

    @GetMapping("/getCacheUser")
    public SysUserCacheInfo getCacheUser(@RequestParam("username") String username) {
        return sysBaseApi.getCacheUser(username);
    }
}
