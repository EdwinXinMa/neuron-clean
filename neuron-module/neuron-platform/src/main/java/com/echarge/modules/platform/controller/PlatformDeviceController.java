package com.echarge.modules.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.platform.vo.DeviceStatusVO;
import com.echarge.modules.platform.vo.PileVO;
import com.echarge.modules.platform.vo.PlatformResult;
import com.echarge.modules.platform.vo.SearchedPileVO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 平台间对接接口 — 设备查询
 * 前缀：/platform/v1/
 * 鉴权：X-Platform-Key（由 PlatformApiKeyFilter 处理）
 *
 * @author Edwin
 */
@Slf4j
@RestController
@RequestMapping("/platform/v1")
@RequiredArgsConstructor
public class PlatformDeviceController {

    private final INcDeviceService ncDeviceService;
    private final OcppCommandSender ocppCommandSender;

    private static final Gson GSON = new Gson();
    private static final int SEARCH_TIMEOUT_SECONDS = 15;

    /**
     * 健康检查（无需 API Key）
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    /**
     * 查询设备在线状态
     * H 平台绑定网关时调用，判断 SN 是否已接入 N 平台且在线
     *
     * @param sn N3 Lite 网关序列号
     */
    @GetMapping("/devices/{sn}/status")
    public PlatformResult<DeviceStatusVO> getDeviceStatus(@PathVariable String sn) {
        NcDevice device = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn));

        if (device == null) {
            return PlatformResult.error(404, "Device not found: " + sn);
        }

        DeviceStatusVO vo = new DeviceStatusVO();
        vo.setSn(device.getSn());
        vo.setOnlineStatus(device.getOnlineStatus());
        vo.setFirmwareVersion(device.getFirmwareVersion());
        return PlatformResult.ok(vo);
    }

    /**
     * 获取网关下挂充电桩列表
     * H 平台绑定成功后查询可绑定的子桩
     *
     * @param sn N3 Lite 网关序列号
     */
    @GetMapping("/devices/{sn}/piles")
    public PlatformResult<List<PileVO>> getDevicePiles(@PathVariable String sn) {
        NcDevice gateway = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn));

        if (gateway == null) {
            return PlatformResult.error(404, "Device not found: " + sn);
        }

        // 查询以此网关为父设备的所有桩（ATP III）
        List<NcDevice> piles = ncDeviceService.list(
                new LambdaQueryWrapper<NcDevice>()
                        .eq(NcDevice::getParentDeviceId, gateway.getId())
                        .eq(NcDevice::getDelFlag, 0));

        List<PileVO> voList = piles.stream().map(pile -> {
            PileVO vo = new PileVO();
            vo.setSubDevId(pile.getSn());
            vo.setDeviceSn(pile.getSn() + "_" + sn);
            vo.setDeviceType(pile.getDeviceType());
            vo.setDeviceModel(pile.getDeviceModel());
            vo.setFirmwareVersion(pile.getFirmwareVersion());
            vo.setOnlineStatus(pile.getOnlineStatus());
            return vo;
        }).toList();

        return PlatformResult.ok(voList);
    }

    /**
     * 触发 N3 Lite 通过 PLC 扫描周边充电桩
     * H 平台绑定成功后调用，获取可纳管的 ATP III 列表
     *
     * @param sn N3 Lite 网关序列号
     */
    @GetMapping("/devices/{sn}/search-piles")
    public PlatformResult<List<SearchedPileVO>> searchPiles(@PathVariable String sn) {
        // 1. 校验设备存在
        NcDevice gateway = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn));
        if (gateway == null) {
            return PlatformResult.error(404, "Device not found: " + sn);
        }

        // 2. 校验 OCPP 会话在线
        if (!ocppCommandSender.isDeviceConnected(sn)) {
            return PlatformResult.error(503, "Device not connected: " + sn);
        }

        // 3. 构造 DataTransfer SearchDeviceList OCPP CALL 消息
        String messageId = "search-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        JsonArray call = new JsonArray();
        call.add(2);
        call.add(messageId);
        call.add("DataTransfer");
        JsonObject payload = new JsonObject();
        payload.addProperty("vendorId", "AlwaysControl");
        payload.addProperty("messageId", BizConstant.DT_SEARCH_DEVICE_LIST);
        payload.addProperty("data", "{}");
        call.add(payload);

        log.info("【搜索桩】发送 SearchDeviceList sn={} msgId={}", sn, messageId);

        // 4. 同步等待 CALLRESULT（最多 15s）
        String resultJson = ocppCommandSender.sendCallAndWait(sn, call.toString(), messageId, SEARCH_TIMEOUT_SECONDS);
        if (resultJson == null) {
            log.warn("【搜索桩】超时未响应 sn={}", sn);
            return PlatformResult.error(504, "Device response timeout");
        }

        log.info("【搜索桩】固件原始响应 sn={} payload={}", sn, resultJson);

        // 5. 解析 CALLRESULT payload：{ "status": "Accepted", "data": "{\"evList\":[...]}" }
        JsonObject result = GSON.fromJson(resultJson, JsonObject.class);
        String status = result.has("status") ? result.get("status").getAsString() : "";
        if (!"Accepted".equals(status)) {
            log.warn("【搜索桩】固件拒绝 sn={} status={}", sn, status);
            return PlatformResult.error(502, "Device rejected: " + status);
        }

        String dataStr = result.has("data") ? result.get("data").getAsString() : "{}";
        JsonObject dataObj = GSON.fromJson(dataStr, JsonObject.class);
        JsonArray evList = dataObj.has("evList") ? dataObj.getAsJsonArray("evList") : new JsonArray();

        List<SearchedPileVO> piles = new ArrayList<>();
        for (JsonElement elem : evList) {
            JsonObject item = elem.getAsJsonObject();
            SearchedPileVO vo = new SearchedPileVO();
            vo.setSubDevId(getStr(item, "subDevId"));
            vo.setDeviceSn(getStr(item, "deviceSn"));
            vo.setProductType(getStr(item, "productType"));
            vo.setDeviceBrand(getStr(item, "deviceBrand"));
            vo.setModelId(getStr(item, "modelId"));
            vo.setModelVersion(getStr(item, "modelVersion"));
            vo.setProductId(getStr(item, "productId"));
            vo.setConnectStatus(getStr(item, "connectStatus"));
            vo.setConnectorCount(item.has("connectorCount") ? item.get("connectorCount").getAsInt() : 1);
            vo.setPortName(getStr(item, "portName"));
            vo.setProtocolAddr(getStr(item, "protocolAddr"));
            piles.add(vo);
        }

        log.info("【搜索桩】完成 sn={} 发现{}台桩", sn, piles.size());
        return PlatformResult.ok(piles);
    }

    private static String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
}
