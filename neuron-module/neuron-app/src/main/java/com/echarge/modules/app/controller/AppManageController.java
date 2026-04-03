package com.echarge.modules.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.entity.AppUserDevice;
import com.echarge.modules.app.mapper.AppUserDeviceMapper;
import com.echarge.modules.app.service.IAppUserService;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.INcDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运维后台 — App 账号管理（走 Shiro 认证）
 * @author Edwin
 */
@RestController
@RequestMapping("/device/appUsers")
@Tag(name = "App 账号管理（运维后台）")
public class AppManageController {

    @Autowired
    private IAppUserService appUserService;

    @Autowired
    private AppUserDeviceMapper userDeviceMapper;

    @Autowired
    private INcDeviceService deviceService;

    /**
     * App 账号列表（分页）
     */
    @GetMapping
    @Operation(summary = "App 账号列表")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNo,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String email) {

        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<AppUser>()
                .like(StringUtils.isNotBlank(email), AppUser::getEmail, email)
                .orderByDesc(AppUser::getCreateTime);

        Page<AppUser> page = appUserService.page(new Page<>(pageNo, pageSize), wrapper);

        // 脱敏：不返回密码和盐值
        List<Map<String, Object>> records = new ArrayList<>();
        for (AppUser user : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", user.getId());
            item.put("email", user.getEmail());
            item.put("name", user.getName());
            item.put("status", user.getStatus());
            item.put("createTime", user.getCreateTime());

            // 查询绑定设备数
            long bindCount = userDeviceMapper.selectCount(
                    new LambdaQueryWrapper<AppUserDevice>()
                            .eq(AppUserDevice::getUserId, user.getId()));
            item.put("bindCount", bindCount);

            records.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());

        return Result.ok(result);
    }

    /**
     * 查询某个 App 用户的绑定设备列表
     */
    @GetMapping("/{userId}/bindings")
    @Operation(summary = "查询用户绑定的设备列表")
    public Result<?> bindings(@PathVariable String userId) {
        List<AppUserDevice> bindings = userDeviceMapper.selectList(
                new LambdaQueryWrapper<AppUserDevice>()
                        .eq(AppUserDevice::getUserId, userId)
                        .orderByDesc(AppUserDevice::getCreateTime));

        List<Map<String, Object>> list = new ArrayList<>();
        for (AppUserDevice binding : bindings) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("deviceSn", binding.getDeviceSn());
            item.put("bindTime", binding.getCreateTime());
            item.put("longitude", binding.getLongitude());
            item.put("latitude", binding.getLatitude());

            // 补充设备在线状态
            NcDevice device = deviceService.getOne(
                    new LambdaQueryWrapper<NcDevice>()
                            .eq(NcDevice::getSn, binding.getDeviceSn()));
            if (device != null) {
                item.put("online", "ONLINE".equals(device.getOnlineStatus()));
                item.put("deviceModel", device.getDeviceModel());
            } else {
                item.put("online", false);
                item.put("deviceModel", null);
            }

            list.add(item);
        }

        return Result.ok(list);
    }
}
