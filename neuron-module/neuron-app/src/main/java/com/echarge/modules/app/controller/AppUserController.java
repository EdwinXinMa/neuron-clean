package com.echarge.modules.app.controller;

import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.service.IAppUserService;
import com.echarge.modules.app.vo.AppResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * App 用户信息
 * @author Edwin
 */
@RestController
@RequestMapping("/app/user")
@Tag(name = "App 用户")
public class AppUserController {

    @Autowired
    private IAppUserService appUserService;

    /**
     * App 启动时上报 FCM Token，用于离线推送
     */
    @PostMapping("/fcm-token")
    @Operation(summary = "上报 FCM 推送 Token")
    public AppResult<?> updateFcmToken(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String fcmToken = params.get("fcmToken");
        if (fcmToken == null || fcmToken.isBlank()) {
            return AppResult.error("fcmToken 不能为空");
        }
        AppUser user = (AppUser) request.getAttribute("appUser");
        appUserService.updateById(new AppUser().setId(user.getId()).setFcmToken(fcmToken).setUpdateTime(new Date()));
        return AppResult.ok("上报成功");
    }
}
