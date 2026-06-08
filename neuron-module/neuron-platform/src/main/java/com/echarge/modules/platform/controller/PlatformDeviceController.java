package com.echarge.modules.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.platform.vo.DeviceStatusVO;
import com.echarge.modules.platform.vo.PileVO;
import com.echarge.modules.platform.vo.PlatformResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
}
