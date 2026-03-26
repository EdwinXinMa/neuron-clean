package com.echarge.modules.device.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.modules.device.entity.NcDlmHistory;
import com.echarge.modules.device.entity.NcDlmHistoryAllocation;
import com.echarge.modules.device.mapper.NcDlmHistoryAllocationMapper;
import com.echarge.modules.device.mapper.NcDlmHistoryMapper;
import com.echarge.modules.device.service.INcDlmHistoryService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Edwin
 */
@Slf4j
@Service
public class NcDlmHistoryServiceImpl extends ServiceImpl<NcDlmHistoryMapper, NcDlmHistory> implements INcDlmHistoryService {

    @Autowired
    private NcDlmHistoryAllocationMapper allocationMapper;

    private final Gson gson = new Gson();

    @Async
    @Override
    public void saveDlmReport(String deviceSn, String payload) {
        try {
            JsonObject data = gson.fromJson(payload, JsonObject.class);
            Date now = new Date();

            // 写主表
            NcDlmHistory history = new NcDlmHistory()
                    .setTime(now)
                    .setDeviceSn(deviceSn)
                    .setTotalCurrent(getFloat(data, "totalCurrent"))
                    .setVoltage(getFloat(data, "voltage"))
                    .setTotalPower(getFloat(data, "totalPower"))
                    .setDeviceTemp(getFloat(data, "deviceTemp"))
                    .setWifiRssi(getShort(data, "wifiRssi"))
                    .setBreakerRating(getShort(data, "breakerRating"));
            baseMapper.insert(history);

            // 写分配表
            if (data.has("pileAllocations")) {
                JsonArray allocations = data.getAsJsonArray("pileAllocations");
                for (JsonElement elem : allocations) {
                    JsonObject alloc = elem.getAsJsonObject();
                    NcDlmHistoryAllocation allocation = new NcDlmHistoryAllocation()
                            .setTime(now)
                            .setDeviceSn(deviceSn)
                            .setPileSn(alloc.get("sn").getAsString())
                            .setAllocatedCurrent(alloc.get("allocatedCurrent").getAsFloat());
                    allocationMapper.insert(allocation);
                }
            }
        } catch (Exception e) {
            log.warn("[DlmHistory] Failed to save DLM report for {}: {}", deviceSn, e.getMessage());
        }
    }

    private Float getFloat(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsFloat() : null;
    }

    private Short getShort(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsShort() : null;
    }
}
