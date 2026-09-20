package com.echarge.modules.app.controller;

import com.echarge.common.exception.NeuronBootException;
import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.i18n.AppI18n;
import com.echarge.modules.app.i18n.LangContext;
import com.echarge.modules.app.mapper.AppUserDeviceMapper;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.entity.NcDevice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * App allocation configuration contracts, access checks and i18n.
 * @author Edwin
 */
class AllocationModeRpcTest {
    private static final String SN = "9EN03L251119Y0036";
    private static final String SET = "ConfigManager.SetConfig";
    private static final String GET = "ConfigManager.GetConfig";
    private AppRpcController controller;
    private INcDeviceService devices;
    private AppUserDeviceMapper bindings;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        controller = new AppRpcController();
        devices = mock(INcDeviceService.class);
        bindings = mock(AppUserDeviceMapper.class);
        ReflectionTestUtils.setField(controller, "deviceService", devices);
        ReflectionTestUtils.setField(controller, "userDeviceMapper", bindings);
        when(bindings.selectCount(any())).thenReturn(1L);
        request = new MockHttpServletRequest();
        AppUser user = new AppUser();
        user.setId("user-1");
        user.setEmail("user@example.test");
        request.setAttribute("appUser", user);
    }

    @AfterEach
    void clearLanguage() {
        LangContext.clear();
    }

    @Test
    void settingUsesDeviceSnAndDoesNotRequireLegacyDlmFlag() {
        Map<String, Object> result = controller.rpccall(body(SET), request);
        assertEquals(Map.of("ret", Map.of(SET, "success"), "deviceId", SN, "code", 200,
                "data", Map.of("configname", "AllocationMode")), result);
        verify(devices).setAllocationMode(SN, "FIFO", "user@example.test");
        verifyNoMoreInteractions(devices);
    }

    @Test
    void queryWrapsDeviceModeInAppData() {
        when(devices.getAllocationMode(SN)).thenReturn("Average");
        Map<String, Object> result = controller.rpccall(body(GET), request);
        assertEquals(Map.of("ret", Map.of(GET, "success"), "deviceId", SN, "code", 200,
                "data", Map.of("configname", "AllocationMode", "AllocationMode", "Average")), result);
    }

    @Test
    void legacyCurrentSettingKeepsExistingServicePath() {
        Map<String, Object> input = body(SET);
        input.put("data", Map.of("configname", "InflowMaxCurrent", "InflowMaxCurrent", 32,
                "InflowSafetyMargin", 5, "newDlmSupported", true));
        Map<String, Object> result = controller.rpccall(input, request);
        assertEquals(200, result.get("code"));
        assertEquals(Map.of("configname", "InflowMaxCurrent"), result.get("data"));
        verify(devices).sendDlmConfig(SN, 32, 5, "user@example.test");
        verifyNoMoreInteractions(devices);
    }

    @Test
    void legacyVersionQueryKeepsExistingResponse() {
        NcDevice device = new NcDevice();
        device.setFirmwareVersion("2.0.36");
        when(devices.getOne(any())).thenReturn(device);
        Map<String, Object> input = body(GET);
        input.put("data", Map.of("configname", "version"));
        Map<String, Object> result = controller.rpccall(input, request);
        assertEquals(200, result.get("code"));
        assertEquals(Map.of("configname", "version", "version", "2.0.36"), result.get("data"));
        verify(devices).getOne(any());
        verifyNoMoreInteractions(devices);
    }

    @ParameterizedTest
    @ValueSource(strings = {SET, GET})
    void unboundDeviceNeverReachesService(String method) {
        when(bindings.selectCount(any())).thenReturn(0L);
        assertEquals(403, controller.rpccall(body(method), request).get("code"));
        verifyNoInteractions(devices);
    }

    @Test
    void missingAuthenticationDoesNotReachService() {
        request.removeAttribute("appUser");
        assertEquals(401, controller.rpccall(body(SET), request).get("code"));
        verifyNoInteractions(devices, bindings);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void absentDeviceSnIsNotReplacedWithBluetoothDeviceId(String sn) {
        Map<String, Object> input = body(SET);
        input.put("deviceSn", sn);
        input.put("deviceId", SN);
        assertEquals(400, controller.rpccall(input, request).get("code"));
        verifyNoInteractions(devices);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"fifo", "AVERAGE", "FIFO ", "Other"})
    void invalidModesAreParameterErrors(String mode) {
        Map<String, Object> input = body(SET);
        Map<String, Object> data = new HashMap<>();
        data.put("configname", "AllocationMode");
        data.put("AllocationMode", mode);
        input.put("data", data);
        assertEquals(400, controller.rpccall(input, request).get("code"));
        verifyNoInteractions(devices);
    }

    @Test
    void wrongJsonTypesAreParameterErrors() {
        for (Object value : List.of(1, true, List.of("FIFO"), Map.of("mode", "FIFO"))) {
            Map<String, Object> input = body(SET);
            input.put("data", Map.of("configname", "AllocationMode", "AllocationMode", value));
            assertEquals(400, controller.rpccall(input, request).get("code"));
        }
        for (String key : List.of("method", "deviceSn", "data")) {
            Map<String, Object> input = body(SET);
            input.put(key, 1);
            assertEquals(400, controller.rpccall(input, request).get("code"));
        }
        verifyNoInteractions(devices);
    }

    @Test
    void firmwareFailureIsLocalizedAndHasNoDefaultMode() {
        LangContext.set("en");
        when(devices.getAllocationMode(SN)).thenThrow(new NeuronBootException("设备未返回有效响应"));
        Map<String, Object> result = controller.rpccall(body(GET), request);
        assertEquals(500, result.get("code"));
        assertEquals(Map.of(GET, "failed"), result.get("ret"));
        assertEquals(Map.of(), result.get("data"));
        assertEquals("Device did not return a valid response", result.get("message"));
    }

    @Test
    void deviceErrorsPreserveCodesAndUnexpectedErrorsDoNotLeakDetails() {
        doThrow(new NeuronBootException("设备不存在", 404)).when(devices).setAllocationMode(anyString(), anyString(), anyString());
        assertEquals(404, controller.rpccall(body(SET), request).get("code"));
        doThrow(new IllegalStateException("internal database details")).when(devices).setAllocationMode(anyString(), anyString(), anyString());
        Map<String, Object> result = controller.rpccall(body(SET), request);
        assertEquals(500, result.get("code"));
        assertEquals("电流分配模式操作失败", result.get("message"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "tw", "es", "pt"})
    void allocationErrorsHaveAllSupportedTranslations(String language) {
        for (String message : List.of("AllocationMode 必须为 Average 或 FIFO", "设备未返回有效响应",
                "电流分配模式仅支持 N3lite 网关", "设备离线，无法操作电流分配模式",
                "设备返回的电流分配模式格式无效", "设备拒绝设置电流分配模式",
                "设备拒绝查询电流分配模式", "电流分配模式操作失败", "deviceSn 必须为非空字符串")) {
            assertNotEquals(message, AppI18n.get(message, language));
        }
    }

    private Map<String, Object> body(String method) {
        Map<String, Object> result = new HashMap<>();
        result.put("method", method);
        result.put("deviceSn", SN);
        result.put("data", SET.equals(method)
                ? Map.of("configname", "AllocationMode", "AllocationMode", "FIFO")
                : Map.of("configname", "AllocationMode"));
        return result;
    }
}
