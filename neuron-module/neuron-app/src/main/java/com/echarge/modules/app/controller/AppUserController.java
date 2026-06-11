package com.echarge.modules.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.entity.AppUserRegistration;
import com.echarge.modules.app.mapper.AppUserRegistrationMapper;
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

    @Autowired
    private AppUserRegistrationMapper userRegistrationMapper;

    /**
     * App 启动时上报极光推送 Registration ID
     * 同一账号多端登录时，每台设备的 regId 都会保留，发推送时全部下发
     */
    @PostMapping("/fcm-token")
    @Operation(summary = "上报极光推送 Registration ID")
    public AppResult<?> updateFcmToken(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String registrationId = params.get("registrationId");
        if (registrationId == null || registrationId.isBlank()) {
            return AppResult.error("registrationId 不能为空");
        }
        AppUser user = (AppUser) request.getAttribute("appUser");
        long exists = userRegistrationMapper.selectCount(
                new LambdaQueryWrapper<AppUserRegistration>()
                        .eq(AppUserRegistration::getUserId, user.getId())
                        .eq(AppUserRegistration::getRegId, registrationId));
        if (exists == 0) {
            userRegistrationMapper.insert(new AppUserRegistration()
                    .setUserId(user.getId())
                    .setRegId(registrationId)
                    .setCreateTime(new Date()));
        }
        return AppResult.ok("上报成功");
    }
}
