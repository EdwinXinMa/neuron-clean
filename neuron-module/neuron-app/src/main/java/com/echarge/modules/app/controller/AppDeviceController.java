package com.echarge.modules.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.modules.app.vo.AppResult;
import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.entity.AppUserDevice;
import com.echarge.modules.app.mapper.AppUserDeviceMapper;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.INcDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * App 设备绑定/解绑/列表
 * @author Edwin
 */
@Slf4j
@RestController
@RequestMapping("/app/device")
@Tag(name = "App 设备管理")
public class AppDeviceController {

    @Autowired
    private AppUserDeviceMapper userDeviceMapper;

    @Autowired
    private INcDeviceService deviceService;

    /**
     * 绑定设备
     */
    @PostMapping("/bind")
    @Operation(summary = "绑定设备到当前用户")
    public AppResult<?> bind(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        AppUser user = (AppUser) request.getAttribute("appUser");
        String deviceSn = (String) params.get("deviceSn");

        if (deviceSn == null || deviceSn.isBlank()) {
            return AppResult.error("设备序列号不能为空");
        }

        // 检查是否已绑定
        long count = userDeviceMapper.selectCount(
                new LambdaQueryWrapper<AppUserDevice>()
                        .eq(AppUserDevice::getUserId, user.getId())
                        .eq(AppUserDevice::getDeviceSn, deviceSn));
        if (count > 0) {
            return AppResult.error("该设备已绑定");
        }

        Double longitude = params.get("longitude") != null ? ((Number) params.get("longitude")).doubleValue() : null;
        Double latitude = params.get("latitude") != null ? ((Number) params.get("latitude")).doubleValue() : null;

        // 查询设备是否已上线过
        NcDevice device = deviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, deviceSn));
        boolean online = false;

        if (device == null) {
            // 设备未上线过，自动创建离线记录
            device = new NcDevice();
            device.setSn(deviceSn);
            device.setOnlineStatus("OFFLINE");
            if (longitude != null && latitude != null) {
                device.setLng(java.math.BigDecimal.valueOf(longitude));
                device.setLat(java.math.BigDecimal.valueOf(latitude));
            }
            device.setCreateTime(new Date());
            deviceService.save(device);
        } else {
            online = "ONLINE".equals(device.getOnlineStatus());
            // 如果有坐标，同步更新 nc_device 表
            if (longitude != null && latitude != null) {
                device.setLng(java.math.BigDecimal.valueOf(longitude));
                device.setLat(java.math.BigDecimal.valueOf(latitude));
                deviceService.updateById(device);
            }
        }

        // 绑定
        AppUserDevice binding = new AppUserDevice()
                .setUserId(user.getId())
                .setDeviceSn(deviceSn)
                .setLongitude(longitude)
                .setLatitude(latitude)
                .setCreateTime(new Date());
        userDeviceMapper.insert(binding);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("deviceSn", deviceSn);
        data.put("online", online);
        return AppResult.ok("绑定成功", data);
    }

    /**
     * 解绑设备
     */
    @PostMapping("/unbind")
    @Operation(summary = "解绑设备")
    public AppResult<?> unbind(@RequestBody Map<String, String> params, HttpServletRequest request) {
        AppUser user = (AppUser) request.getAttribute("appUser");
        String deviceSn = params.get("deviceSn");

        if (deviceSn == null || deviceSn.isBlank()) {
            return AppResult.error("设备序列号不能为空");
        }

        userDeviceMapper.delete(
                new LambdaQueryWrapper<AppUserDevice>()
                        .eq(AppUserDevice::getUserId, user.getId())
                        .eq(AppUserDevice::getDeviceSn, deviceSn));

        return AppResult.ok("解绑成功");
    }

    /**
     * 查询当前用户绑定的设备列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询绑定的设备列表")
    public AppResult<?> list(HttpServletRequest request) {
        AppUser user = (AppUser) request.getAttribute("appUser");

        List<AppUserDevice> bindings = userDeviceMapper.selectList(
                new LambdaQueryWrapper<AppUserDevice>().eq(AppUserDevice::getUserId, user.getId()));

        List<Map<String, Object>> devices = new ArrayList<>();
        for (AppUserDevice binding : bindings) {
            NcDevice device = deviceService.getOne(
                    new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, binding.getDeviceSn()));
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("deviceSn", binding.getDeviceSn());
            if (device != null) {
                d.put("deviceModel", device.getDeviceModel());
                d.put("online", "ONLINE".equals(device.getOnlineStatus()));
                d.put("firmwareVersion", device.getFirmwareVersion());
            } else {
                d.put("online", false);
            }
            devices.add(d);
        }

        return AppResult.ok(Map.of("devices", devices));
    }
}
