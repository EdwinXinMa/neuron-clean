package com.echarge.modules.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.echarge.common.constant.BizConstant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.modules.app.vo.AppResult;
import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.entity.AppUserDevice;
import com.echarge.modules.app.mapper.AppUserMapper;
import com.echarge.modules.app.mapper.AppUserDeviceMapper;
import com.echarge.modules.app.service.FcmService;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.websocket.AppWebSocket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * App 设备绑定/解绑/列表
 * @author Edwin
 */
@Slf4j
@RestController
@RequestMapping("/app/device")
@Tag(name = "App 设备管理")
public class AppDeviceController {

    private static final String BIND_LIMIT_KEY = "device:bind:limit:";
    private static final String BIND_REQ_KEY   = "device:bind:req:";
    private static final String BIND_CODE_KEY  = "device:bind:code:";

    private static final long LIMIT_TTL   = 60L;
    private static final long REQ_TTL     = 600L;
    private static final long CODE_TTL    = 120L;

    @Autowired
    private AppUserDeviceMapper userDeviceMapper;

    @Autowired
    private AppUserMapper userMapper;

    @Autowired
    private INcDeviceService deviceService;

    @Autowired
    private FcmService fcmService;

    @Value("${app.device.bind-auth-enabled:false}")
    private boolean bindAuthEnabled;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 绑定设备
     * 若该设备已有其他账号绑定，则发起授权请求，不直接绑定
     */
    @PostMapping("/bind")
    @Operation(summary = "绑定设备到当前用户")
    public AppResult<?> bind(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        AppUser user = (AppUser) request.getAttribute("appUser");
        String deviceSn = (String) params.get("deviceSn");

        if (deviceSn == null || deviceSn.isBlank()) {
            return AppResult.error("设备序列号不能为空");
        }

        // 当前用户已绑定该设备
        long selfCount = userDeviceMapper.selectCount(
                new LambdaQueryWrapper<AppUserDevice>()
                        .eq(AppUserDevice::getUserId, user.getId())
                        .eq(AppUserDevice::getDeviceSn, deviceSn));
        if (selfCount > 0) {
            return AppResult.error("该设备已绑定");
        }

        // 是否有其他账号已绑定该设备
        long otherCount = userDeviceMapper.selectCount(
                new LambdaQueryWrapper<AppUserDevice>()
                        .eq(AppUserDevice::getDeviceSn, deviceSn)
                        .ne(AppUserDevice::getUserId, user.getId()));

        if (otherCount > 0 && bindAuthEnabled) {
            return handleAuthBind(user, deviceSn);
        }

        // 无其他绑定账号，直接绑定
        Double longitude = params.get("longitude") != null ? ((Number) params.get("longitude")).doubleValue() : null;
        Double latitude  = params.get("latitude")  != null ? ((Number) params.get("latitude")).doubleValue()  : null;

        boolean online = saveDeviceIfAbsent(deviceSn, longitude, latitude);

        AppUserDevice binding = new AppUserDevice()
                .setUserId(user.getId())
                .setDeviceSn(deviceSn)
                .setLongitude(longitude)
                .setLatitude(latitude)
                .setCreateTime(new Date());
        userDeviceMapper.insert(binding);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("needAuth", false);
        data.put("deviceSn", deviceSn);
        data.put("online", online);
        return AppResult.ok("绑定成功", data);
    }

    /**
     * 有其他账号已绑定时，发起授权请求
     */
    private AppResult<?> handleAuthBind(AppUser user, String deviceSn) {
        String limitKey = BIND_LIMIT_KEY + deviceSn + ":" + user.getId();
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(limitKey))) {
            return AppResult.error("请求过于频繁，请 1 分钟后再试");
        }

        // 写频率限制
        stringRedisTemplate.opsForValue().set(limitKey, "1", LIMIT_TTL, TimeUnit.SECONDS);

        // 生成请求ID
        String requestId = UUID.randomUUID().toString().replace("-", "");

        // 写请求记录
        JSONObject req = new JSONObject();
        req.put("deviceSn", deviceSn);
        req.put("requesterId", user.getId());
        req.put("requesterName", user.getName());
        req.put("status", "PENDING");
        stringRedisTemplate.opsForValue().set(BIND_REQ_KEY + requestId, req.toJSONString(), REQ_TTL, TimeUnit.SECONDS);

        // WS 推送给已绑该设备的所有账号
        JSONObject push = new JSONObject();
        push.put("type", "DEVICE_BIND_REQUEST");
        push.put("requestId", requestId);
        push.put("requesterName", user.getName());
        push.put("expireSeconds", REQ_TTL);
        AppWebSocket.publish("deviceBindAuth:" + deviceSn, push);

        // FCM 静默推送兜底（App 关闭时也能收到通知）
        List<AppUserDevice> boundList = userDeviceMapper.selectList(
                new LambdaQueryWrapper<AppUserDevice>().eq(AppUserDevice::getDeviceSn, deviceSn));
        for (AppUserDevice bound : boundList) {
            AppUser boundUser = userMapper.selectById(bound.getUserId());
            if (boundUser != null && boundUser.getRegistrationId() != null) {
                Map<String, String> fcmData = new HashMap<>();
                fcmData.put("requestId", requestId);
                fcmData.put("requesterName", user.getName());
                fcmService.sendSilent(boundUser.getRegistrationId(), "DEVICE_BIND_REQUEST", fcmData);
            }
        }

        log.info("[BindAuth] 授权请求已推送, deviceSn={}, requesterId={}, requestId={}", deviceSn, user.getId(), requestId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("needAuth", true);
        data.put("requestId", requestId);
        return AppResult.ok("授权请求已发送，请等待设备主人同意", data);
    }

    /**
     * 响应绑定授权请求（同意 / 拒绝）
     */
    @PostMapping("/bind/respond")
    @Operation(summary = "响应设备绑定授权请求")
    public AppResult<?> respond(@RequestBody Map<String, String> params, HttpServletRequest request) {
        AppUser user = (AppUser) request.getAttribute("appUser");
        String requestId = params.get("requestId");
        String action    = params.get("action");

        if (requestId == null || requestId.isBlank()) {
            return AppResult.error("requestId 不能为空");
        }
        if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
            return AppResult.error("action 无效，必须为 APPROVE 或 REJECT");
        }

        String reqJson = stringRedisTemplate.opsForValue().get(BIND_REQ_KEY + requestId);
        if (reqJson == null) {
            return AppResult.error("请求不存在或已过期");
        }

        JSONObject req = JSONObject.parseObject(reqJson);

        // 校验当前用户确实绑定了该设备
        String deviceSn = req.getString("deviceSn");
        long boundCount = userDeviceMapper.selectCount(
                new LambdaQueryWrapper<AppUserDevice>()
                        .eq(AppUserDevice::getUserId, user.getId())
                        .eq(AppUserDevice::getDeviceSn, deviceSn));
        if (boundCount == 0) {
            return AppResult.error("无权操作");
        }

        if (!"PENDING".equals(req.getString("status"))) {
            return AppResult.error("已由其他人授权，无需重复操作");
        }

        String requesterId = req.getString("requesterId");

        if ("APPROVE".equals(action)) {
            return doApprove(requestId, requesterId, req);
        } else {
            return doReject(requestId, requesterId, req);
        }
    }

    private AppResult<?> doApprove(String requestId, String requesterId, JSONObject req) {
        // 生成6位授权码，SETNX 防并发
        String authCode = String.format("%06d", new Random().nextInt(1000000));
        Boolean set = stringRedisTemplate.opsForValue()
                .setIfAbsent(BIND_CODE_KEY + requestId, authCode, CODE_TTL, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(set)) {
            return AppResult.error("已由其他人授权，无需重复操作");
        }

        // 更新状态
        req.put("status", "APPROVED");
        stringRedisTemplate.opsForValue().set(BIND_REQ_KEY + requestId, req.toJSONString(), REQ_TTL, TimeUnit.SECONDS);

        // WS 推送授权码给申请人
        JSONObject push = new JSONObject();
        push.put("type", "DEVICE_BIND_CODE");
        push.put("requestId", requestId);
        push.put("authCode", authCode);
        push.put("expireSeconds", CODE_TTL);
        AppWebSocket.publish("deviceBindCode:" + requesterId, push);

        log.info("[BindAuth] 已同意, requestId={}, requesterId={}", requestId, requesterId);
        return AppResult.ok("已同意，授权码已发送给申请人");
    }

    private AppResult<?> doReject(String requestId, String requesterId, JSONObject req) {
        // 更新状态
        req.put("status", "REJECTED");
        stringRedisTemplate.opsForValue().set(BIND_REQ_KEY + requestId, req.toJSONString(), REQ_TTL, TimeUnit.SECONDS);

        // WS 推送拒绝通知给申请人
        JSONObject push = new JSONObject();
        push.put("type", "DEVICE_BIND_REJECTED");
        push.put("requestId", requestId);
        AppWebSocket.publish("deviceBindCode:" + requesterId, push);

        log.info("[BindAuth] 已拒绝, requestId={}, requesterId={}", requestId, requesterId);
        return AppResult.ok("已拒绝");
    }

    /**
     * 验证授权码完成绑定
     */
    @PostMapping("/bind/verify")
    @Operation(summary = "验证授权码完成设备绑定")
    public AppResult<?> verify(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        AppUser user = (AppUser) request.getAttribute("appUser");
        String requestId = (String) params.get("requestId");
        String authCode  = (String) params.get("authCode");
        String deviceSn  = (String) params.get("deviceSn");

        if (requestId == null || authCode == null || deviceSn == null) {
            return AppResult.error("requestId、authCode、deviceSn 不能为空");
        }

        String reqJson = stringRedisTemplate.opsForValue().get(BIND_REQ_KEY + requestId);
        if (reqJson == null) {
            return AppResult.error("请求不存在或已过期");
        }

        JSONObject req = JSONObject.parseObject(reqJson);
        if (!"APPROVED".equals(req.getString("status"))) {
            return AppResult.error("请求状态异常，请重新发起绑定");
        }

        String storedCode = stringRedisTemplate.opsForValue().get(BIND_CODE_KEY + requestId);
        if (storedCode == null) {
            return AppResult.error("授权码已过期");
        }
        if (!storedCode.equals(authCode)) {
            return AppResult.error("授权码错误");
        }

        Double longitude = params.get("longitude") != null ? ((Number) params.get("longitude")).doubleValue() : null;
        Double latitude  = params.get("latitude")  != null ? ((Number) params.get("latitude")).doubleValue()  : null;

        boolean online = saveDeviceIfAbsent(deviceSn, longitude, latitude);

        AppUserDevice binding = new AppUserDevice()
                .setUserId(user.getId())
                .setDeviceSn(deviceSn)
                .setLongitude(longitude)
                .setLatitude(latitude)
                .setCreateTime(new Date());
        userDeviceMapper.insert(binding);

        // 标记已使用，删除授权码
        req.put("status", "USED");
        stringRedisTemplate.opsForValue().set(BIND_REQ_KEY + requestId, req.toJSONString(), REQ_TTL, TimeUnit.SECONDS);
        stringRedisTemplate.delete(BIND_CODE_KEY + requestId);

        log.info("[BindAuth] 绑定完成, deviceSn={}, userId={}, requestId={}", deviceSn, user.getId(), requestId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("deviceSn", deviceSn);
        data.put("online", online);
        return AppResult.ok("绑定成功", data);
    }

    /**
     * 查询绑定请求状态（断线补偿）
     */
    @GetMapping("/bind/status")
    @Operation(summary = "查询绑定授权请求状态")
    public AppResult<?> bindStatus(@RequestParam String requestId) {
        String reqJson = stringRedisTemplate.opsForValue().get(BIND_REQ_KEY + requestId);

        Map<String, Object> data = new LinkedHashMap<>();
        if (reqJson == null) {
            data.put("status", "EXPIRED");
            return AppResult.ok(data);
        }

        JSONObject req = JSONObject.parseObject(reqJson);
        String status = req.getString("status");
        data.put("status", status);

        if ("APPROVED".equals(status)) {
            String storedCode = stringRedisTemplate.opsForValue().get(BIND_CODE_KEY + requestId);
            if (storedCode == null) {
                data.put("status", "EXPIRED");
            } else {
                data.put("authCode", storedCode);
            }
        }

        return AppResult.ok(data);
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

    /**
     * 查询或创建设备记录，返回在线状态
     */
    private boolean saveDeviceIfAbsent(String deviceSn, Double longitude, Double latitude) {
        NcDevice device = deviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, deviceSn));
        if (device == null) {
            device = new NcDevice();
            device.setSn(deviceSn);
            device.setDeviceType(BizConstant.TYPE_N3_LITE);
            device.setOnlineStatus("OFFLINE");
            if (longitude != null && latitude != null) {
                device.setLng(BigDecimal.valueOf(longitude));
                device.setLat(BigDecimal.valueOf(latitude));
            }
            device.setCreateTime(new Date());
            deviceService.save(device);
            return false;
        }
        if (longitude != null && latitude != null) {
            device.setLng(BigDecimal.valueOf(longitude));
            device.setLat(BigDecimal.valueOf(latitude));
            deviceService.updateById(device);
        }
        return "ONLINE".equals(device.getOnlineStatus());
    }
}
