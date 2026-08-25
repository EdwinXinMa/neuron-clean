package com.echarge.modules.device.service.impl;

import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.INcOpLogService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Scheduled charging protocol contract tests.
 *
 * @author Edwin
 */
class NcDeviceServiceImplTest {

    /**
     * SetScheduledCharging must follow the firmware protocol's deviceList payload shape.
     */
    @Test
    void sendScheduledChargingUsesDeviceListPayload() {
        NcDeviceServiceImpl service = spy(new NcDeviceServiceImpl());
        OcppCommandSender commandSender = mock(OcppCommandSender.class);
        INcOpLogService opLogService = mock(INcOpLogService.class);
        ReflectionTestUtils.setField(service, "ocppCommandSender", commandSender);
        ReflectionTestUtils.setField(service, "opLogService", opLogService);

        NcDevice device = new NcDevice();
        device.setSn("9EN03L260528Y0035");
        doReturn(device).when(service).getOne(any());
        when(commandSender.isDeviceConnected(device.getSn())).thenReturn(true);
        when(commandSender.sendCallAndWait(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn("{\"status\":\"Accepted\"}");

        service.sendScheduledCharging(
                device.getSn(),
                "9307260450002",
                List.of(List.of("17:39", "17:40")),
                "app");

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(commandSender).sendCallAndWait(
                eq(device.getSn()), messageCaptor.capture(), anyString(), eq(10L));

        JsonArray call = JsonParser.parseString(messageCaptor.getValue()).getAsJsonArray();
        JsonObject dataTransfer = call.get(3).getAsJsonObject();
        JsonObject data = JsonParser.parseString(dataTransfer.get("data").getAsString()).getAsJsonObject();

        assertEquals("SetScheduledCharging", dataTransfer.get("messageId").getAsString());
        assertTrue(data.has("deviceList"));
        assertFalse(data.has("sn"));
        assertFalse(data.has("timePeriods"));

        JsonArray deviceList = data.getAsJsonArray("deviceList");
        assertEquals(1, deviceList.size());
        JsonObject item = deviceList.get(0).getAsJsonObject();
        assertEquals("9307260450002", item.get("sn").getAsString());
        JsonArray period = item.getAsJsonArray("timePeriods").get(0).getAsJsonArray();
        assertEquals("17:39", period.get(0).getAsString());
        assertEquals("17:40", period.get(1).getAsString());
    }
}
