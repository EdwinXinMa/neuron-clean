package com.echarge.modules.device.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.echarge.common.util.RedisUtil;
import com.echarge.modules.alert.service.INcAlertService;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.INcConnectorService;
import com.echarge.modules.device.service.INcDeviceService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Web child version contract, including offline and incomplete reports.
 * @author Edwin
 */
class PileFirmwareVersionWebTest {
    @ParameterizedTest
    @CsvSource({"offline,stored", "missing,stored", "blank,stored", "live,live-version"})
    void detailKeepsExistingVersionField(String scenario, String expected) {
        NcDeviceController controller = new NcDeviceController();
        INcDeviceService devices = mock(INcDeviceService.class);
        RedisUtil redis = mock(RedisUtil.class);
        ReflectionTestUtils.setField(controller, "ncDeviceService", devices);
        ReflectionTestUtils.setField(controller, "redisClient", redis);
        ReflectionTestUtils.setField(controller, "ncConnectorService", mock(INcConnectorService.class));
        ReflectionTestUtils.setField(controller, "ncAlertService", mock(INcAlertService.class));
        NcDevice parent = new NcDevice();
        parent.setId("gateway-id");
        NcDevice child = new NcDevice().setSn("pile-sn").setFirmwareVersion("stored");
        child.setId("pile-id");
        when(devices.getOne(any())).thenReturn(parent);
        when(devices.list(any(Wrapper.class))).thenReturn(List.of(child));
        if (!"offline".equals(scenario)) {
            String field = switch (scenario) {
                case "live" -> ",\"charge_version\":\"live-version\"";
                case "blank" -> ",\"charge_version\":\"  \"";
                default -> "";
            };
            when(redis.get("device:dlm:gateway-sn")).thenReturn(
                    "{\"pileAllocations\":[{\"sn\":\"pile-sn\"" + field + "}]}");
        }
        Map<String, Object> result = controller.detail("gateway-sn").getResult();
        List<Map<String, Object>> chargers = (List<Map<String, Object>>) result.get("chargers");
        assertFalse(chargers.get(0).containsKey("firmwareVersion"));
        assertEquals(expected, chargers.get(0).get("charge_version"));
    }
}
