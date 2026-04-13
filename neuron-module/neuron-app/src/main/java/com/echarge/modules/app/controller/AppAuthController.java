package com.echarge.modules.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.modules.app.vo.AppResult;
import com.echarge.common.system.util.JwtUtil;
import com.echarge.common.util.OConvertUtils;
import com.echarge.common.util.PasswordUtil;
import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.entity.AppUserDevice;
import com.echarge.modules.app.mapper.AppUserDeviceMapper;
import com.echarge.modules.app.service.IAppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * App 用户认证（注册/登录）
 * @author Edwin
 */
@Slf4j
@RestController
@RequestMapping("/app/auth")
@Tag(name = "App 认证")
public class AppAuthController {

    @Autowired
    private IAppUserService appUserService;

    @Autowired
    private AppUserDeviceMapper userDeviceMapper;

    /**
     * 注册
     */
    @PostMapping("/register")
    @Operation(summary = "App 用户注册")
    public AppResult<?> register(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String password = params.get("password");
        String name = params.get("name");

        if (StringUtils.isAnyBlank(email, password, name)) {
            return AppResult.error("邮箱、密码、姓名不能为空");
        }

        // 检查邮箱是否已注册
        long count = appUserService.count(
                new LambdaQueryWrapper<AppUser>().eq(AppUser::getEmail, email));
        if (count > 0) {
            return AppResult.error("该邮箱已注册");
        }

        // 创建用户
        String salt = OConvertUtils.randomGen(8);
        String encryptedPassword = PasswordUtil.encrypt(email, password, salt);

        AppUser user = new AppUser()
                .setEmail(email)
                .setPassword(encryptedPassword)
                .setSalt(salt)
                .setName(name)
                .setStatus(1)
                .setCreateTime(new Date());
        appUserService.save(user);

        // 签发 Token
        String token = JwtUtil.sign(email, encryptedPassword, "APP");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", user.getId());
        data.put("email", user.getEmail());
        data.put("token", token);

        return AppResult.ok("注册成功", data);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    @Operation(summary = "App 用户登录")
    public AppResult<?> login(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String password = params.get("password");

        if (StringUtils.isAnyBlank(email, password)) {
            return AppResult.error("邮箱和密码不能为空");
        }

        // 查用户
        AppUser user = appUserService.getOne(
                new LambdaQueryWrapper<AppUser>().eq(AppUser::getEmail, email));
        if (user == null) {
            return AppResult.error("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return AppResult.error("账号已禁用");
        }

        // 校验密码
        String encryptedInput = PasswordUtil.encrypt(email, password, user.getSalt());
        if (!encryptedInput.equals(user.getPassword())) {
            return AppResult.error("密码错误");
        }

        // 签发 Token
        String token = JwtUtil.sign(email, user.getPassword(), "APP");

        // 查询绑定设备
        List<AppUserDevice> bindings = userDeviceMapper.selectList(
                new LambdaQueryWrapper<AppUserDevice>().eq(AppUserDevice::getUserId, user.getId()));

        List<Map<String, Object>> devices = new ArrayList<>();
        for (AppUserDevice binding : bindings) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("deviceSn", binding.getDeviceSn());
            devices.add(d);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", user.getId());
        data.put("email", user.getEmail());
        data.put("token", token);
        data.put("devices", devices);

        return AppResult.ok(data);
    }
}
