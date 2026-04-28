package com.echarge.modules.device.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.common.util.RedisUtil;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.entity.NcOpLog;
import com.echarge.modules.device.mapper.NcDeviceMapper;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.service.INcOpLogService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Edwin
 */
@Slf4j
@Service
public class NcDeviceServiceImpl extends ServiceImpl<NcDeviceMapper, NcDevice> implements INcDeviceService {

    @Autowired
    private OcppCommandSender ocppCommandSender;

    @Autowired
    private RedisUtil redisClient;

    @Autowired
    private INcOpLogService opLogService;

    /** {@inheritDoc} */
    @Override
    public void register(NcDevice device) {
        String sn = device.getSn();
        if (StringUtils.isNotBlank(sn)) {
            sn = sn.trim().replaceAll("\\s+", "");
            device.setSn(sn);
        }
        if (StringUtils.isBlank(device.getSn())) {
            throw new NeuronBootException("\u8bbe\u5907SN\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (StringUtils.isBlank(device.getDealer())) {
            throw new NeuronBootException("\u7ecf\u9500\u5546\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (device.getShipDate() == null) {
            throw new NeuronBootException("\u51fa\u8d27\u65e5\u671f\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (existsBySn(device.getSn())) {
            throw new NeuronBootException("\u8bbe\u5907SN\u5df2\u5b58\u5728");
        }
        device.setDeviceType(BizConstant.TYPE_N3_LITE);
        device.setOnlineStatus(BizConstant.DEVICE_UNACTIVATED);
        device.setStatus("NORMAL");
        device.setDelFlag(0);
        assignRandomLocation(device);
        this.save(device);
    }

    /** {@inheritDoc} */
    @Override
    public void disable(String id) {
        NcDevice device = this.getById(id);
        if (device == null) {
            throw new NeuronBootException("\u8bbe\u5907\u4e0d\u5b58\u5728");
        }
        device.setStatus("DISABLED");
        this.updateById(device);
    }

    /** {@inheritDoc} */
    @Override
    public void enable(String id) {
        NcDevice device = this.getById(id);
        if (device == null) {
            throw new NeuronBootException("\u8bbe\u5907\u4e0d\u5b58\u5728");
        }
        device.setStatus("NORMAL");
        this.updateById(device);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsBySn(String sn) {
        return this.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getSn, sn)
                .eq(NcDevice::getDelFlag, 0)) > 0;
    }

    /** {@inheritDoc} */
    @Override
    public void sendDlmConfig(String sn, int breakerRating, String opUser) {
        NcDevice device = this.getOne(new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn));
        if (device == null) {
            throw new NeuronBootException("设备不存在: " + sn);
        }
        if (!BizConstant.VALID_BREAKER_RATINGS.contains(breakerRating)) {
            throw new NeuronBootException("breakerRating 必须是 16/20/25/32/40/50/63 之一");
        }

        Integer oldRating = device.getBreakerRating();

        // 更新数据库
        device.setBreakerRating(breakerRating);
        this.updateById(device);

        // 同步更新 Redis
        String redisKey = "device:dlm:" + sn;
        Object dlmRaw = redisClient.get(redisKey);
        if (dlmRaw != null) {
            try {
                JSONObject dlm = JSONObject.parseObject(dlmRaw.toString());
                dlm.put("breakerRating", breakerRating);
                redisClient.set(redisKey, dlm.toJSONString(), 300);
            } catch (Exception e) {
                log.warn("更新 Redis DLM 数据失败: {}", e.getMessage());
            }
        }

        // OCPP 下发
        if (ocppCommandSender.isDeviceConnected(sn)) {
            String messageId = "dlm-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            JsonObject payload = new JsonObject();
            payload.addProperty("breakerRating", breakerRating);

            JsonArray call = new JsonArray();
            call.add(2);
            call.add(messageId);
            call.add("DataTransfer");

            JsonObject dtPayload = new JsonObject();
            dtPayload.addProperty("vendorId", "AlwaysControl");
            dtPayload.addProperty("messageId", BizConstant.DT_SET_DLM_CONFIG);
            dtPayload.addProperty("data", payload.toString());
            call.add(dtPayload);

            ocppCommandSender.sendCall(sn, call.toString());
            log.info("[DLM] Config sent to {}: breakerRating={}A", sn, breakerRating);
        }

        // 操作日志
        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(sn);
        opLog.setOpUser(opUser);
        opLog.setOpType(NcOpLog.DLM_CONFIG);
        opLog.setOpContent((oldRating != null ? oldRating : "?") + "A → " + breakerRating + "A");
        opLog.setOpResult(NcOpLog.SUCCESS);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        opLogService.save(opLog);
    }

    /** {@inheritDoc} */
    @Override
    public void sendWorkMode(String sn, List<Map<String, String>> deviceList, String opUser) {
        NcDevice device = this.getOne(new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn));
        if (device == null) {
            throw new NeuronBootException("设备不存在: " + sn);
        }
        if (!ocppCommandSender.isDeviceConnected(sn)) {
            throw new NeuronBootException("设备离线，无法下发工作模式切换");
        }

        Set<String> validModes = Set.of("Plc", "App", "Ocpp");
        for (Map<String, String> item : deviceList) {
            if (item.get("sn") == null || item.get("sn").isBlank()) {
                throw new NeuronBootException("桩 SN 不能为空");
            }
            if (!validModes.contains(item.get("workMode"))) {
                throw new NeuronBootException("workMode 必须是 Plc/App/Ocpp 之一");
            }
        }

        // 构建 OCPP DataTransfer CALL
        JsonObject payload = new JsonObject();
        com.google.gson.JsonArray list = new com.google.gson.JsonArray();
        StringBuilder desc = new StringBuilder();
        for (Map<String, String> item : deviceList) {
            JsonObject obj = new JsonObject();
            obj.addProperty("sn", item.get("sn"));
            obj.addProperty("workMode", item.get("workMode"));
            list.add(obj);
            if (!desc.isEmpty()) {
                desc.append(", ");
            }
            desc.append(item.get("sn")).append("→").append(item.get("workMode"));
        }
        payload.add("deviceList", list);

        String messageId = "wm-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        JsonArray call = new JsonArray();
        call.add(2);
        call.add(messageId);
        call.add("DataTransfer");

        JsonObject dtPayload = new JsonObject();
        dtPayload.addProperty("vendorId", "AlwaysControl");
        dtPayload.addProperty("messageId", BizConstant.DT_SET_WORK_MODE);
        dtPayload.addProperty("data", payload.toString());
        call.add(dtPayload);

        ocppCommandSender.sendCall(sn, call.toString());
        log.info("[WorkMode] Command sent to {}: {}", sn, desc);

        // 操作日志
        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(sn);
        opLog.setOpUser(opUser);
        opLog.setOpType(NcOpLog.WORK_MODE);
        opLog.setOpContent("工作模式切换: " + desc);
        opLog.setOpResult(NcOpLog.SUCCESS);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        opLogService.save(opLog);
    }

    /**
     * 如果设备没有经纬度，随机分配一个全球陆地坐标
     */
    public static void assignRandomLocation(NcDevice device) {
        if (device.getLat() != null && device.getLng() != null) {
            return;
        }
        // 全球主要城市坐标 { 纬度, 经度 } + 小范围偏移，保证落在陆地上
        double[][] cities = {
            // 北京
            { 39.9, 116.4 },
            // 上海
            { 31.2, 121.5 },
            // 广州
            { 23.1, 113.3 },
            // 深圳
            { 22.5, 114.1 },
            // 东京
            { 35.7, 139.7 },
            // 首尔
            { 37.6, 127.0 },
            // 新加坡
            { 1.35, 103.8 },
            // 曼谷
            { 13.8, 100.5 },
            // 新德里
            { 28.6, 77.2 },
            // 伦敦
            { 51.5, -0.1 },
            // 巴黎
            { 48.9, 2.35 },
            // 柏林
            { 52.5, 13.4 },
            // 马德里
            { 40.4, -3.7 },
            // 罗马
            { 41.9, 12.5 },
            // 纽约
            { 40.7, -74.0 },
            // 洛杉矶
            { 34.1, -118.2 },
            // 多伦多
            { 43.7, -79.4 },
            // 圣保罗
            { -23.6, -46.6 },
            // 开普敦
            { -33.9, 18.4 },
            // 悉尼
            { -33.9, 151.2 },
            // 墨尔本
            { -37.8, 145.0 },
            // 莫斯科
            { 55.8, 37.6 },
            // 迪拜
            { 25.3, 55.3 },
        };
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double[] city = cities[rng.nextInt(cities.length)];
        // ±1度 ≈ ±111km
        double lat = city[0] + (rng.nextDouble() - 0.5) * 2;
        double lng = city[1] + (rng.nextDouble() - 0.5) * 2;
        device.setLat(BigDecimal.valueOf(lat).setScale(6, RoundingMode.HALF_UP));
        device.setLng(BigDecimal.valueOf(lng).setScale(6, RoundingMode.HALF_UP));
    }
}