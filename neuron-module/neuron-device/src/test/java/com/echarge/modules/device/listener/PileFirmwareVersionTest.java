package com.echarge.modules.device.listener;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.event.DeviceEvent;
import com.echarge.common.util.RedisUtil;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.mapper.NcChargingSessionMapper;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.service.INcDlmHistoryService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DLM child firmware persistence and ownership regressions.
 * @author Edwin
 */
class PileFirmwareVersionTest {
    private DeviceEventHandler handler;
    private INcDeviceService devices;
    private NcDevice child;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"), NcDevice.class);
        handler = new DeviceEventHandler();
        devices = mock(INcDeviceService.class);
        ReflectionTestUtils.setField(handler, "ncDeviceService", devices);
        ReflectionTestUtils.setField(handler, "redisUtil", mock(RedisUtil.class));
        ReflectionTestUtils.setField(handler, "dlmHistoryService", mock(INcDlmHistoryService.class));
        NcDevice parent = new NcDevice();
        parent.setId("gateway-id");
        parent.setSn("gateway-sn");
        parent.setOnlineStatus(BizConstant.DEVICE_ONLINE);
        when(devices.getOne(any())).thenReturn(parent);
        child = new NcDevice();
        child.setId("pile-id");
        child.setSn("pile-sn");
        child.setParentDeviceId(parent.getId());
        when(devices.list(any(Wrapper.class))).thenReturn(List.of(child));
        when(devices.update(any(Wrapper.class))).thenReturn(true);
    }

    @Test
    void savesVersionWithoutConnectorsAndScopesReadAndWriteToParent() {
        report("\"00713700B2\"");
        ArgumentCaptor<LambdaQueryWrapper<NcDevice>> query = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(devices).list(query.capture());
        assertTrue(query.getValue().getSqlSegment().contains("parent_device_id"));
        assertTrue(query.getValue().getParamNameValuePairs().containsValue("gateway-id"));
        assertTrue(query.getValue().getParamNameValuePairs().containsValue("pile-sn"));
        ArgumentCaptor<LambdaUpdateWrapper<NcDevice>> update = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(devices).update(update.capture());
        assertTrue(update.getValue().getSqlSegment().contains("parent_device_id"));
        assertTrue(update.getValue().getParamNameValuePairs().containsValue("gateway-id"));
        assertTrue(update.getValue().getParamNameValuePairs().containsValue("pile-id"));
        assertTrue(update.getValue().getParamNameValuePairs().containsValue("00713700B2"));
        assertTrue(update.getValue().getSqlSet().contains("firmware_version"));
        assertFalse(update.getValue().getSqlSet().contains("online_status"));
        verify(devices, never()).updateById(any(NcDevice.class));
    }

    @Test
    void repeatedVersionDoesNotWriteAgain() {
        child.setFirmwareVersion("00713700B2");
        report("\"00713700B2\"");
        verify(devices, never()).update(any(Wrapper.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "\"\"", "\"   \"", "123", "{}", "[]"})
    void emptyOrInvalidVersionKeepsStoredVersion(String version) {
        child.setFirmwareVersion("00713700B2");
        report(version);
        verify(devices, never()).update(any(Wrapper.class));
        verify(devices, never()).list(any(Wrapper.class));
    }

    @Test
    void missingVersionDoesNotClearStoredVersion() {
        handler.onDeviceEvent(new DeviceEvent(DeviceEvent.DLM_STATUS, "gateway-sn",
                "{\"pileAllocations\":[{\"sn\":\"pile-sn\"}]}"));
        verify(devices, never()).update(any(Wrapper.class));
    }

    @Test
    void unregisteredOrOtherGatewayPileIsNotCreatedOrUpdated() {
        when(devices.list(any(Wrapper.class))).thenReturn(List.of());
        report("\"00713700B2\"");
        verify(devices, never()).update(any(Wrapper.class));
        verify(devices, never()).save(any());
    }

    private void report(String version) {
        handler.onDeviceEvent(new DeviceEvent(DeviceEvent.DLM_STATUS, "gateway-sn",
                "{\"pileAllocations\":[{\"sn\":\"pile-sn\",\"charge_version\":" + version + "}]}"));
    }

    @Test
    void versionSaveFailureDoesNotPreventChargingSessionProcessing() {
        NcChargingSessionMapper sessions = mock(NcChargingSessionMapper.class);
        ReflectionTestUtils.setField(handler, "chargingSessionMapper", sessions);
        when(devices.update(any(Wrapper.class))).thenThrow(new IllegalStateException("version write failed"));
        handler.onDeviceEvent(new DeviceEvent(DeviceEvent.DLM_STATUS, "gateway-sn",
                "{\"pileAllocations\":[{\"sn\":\"pile-sn\",\"charge_version\":\"00713700B2\","
                        + "\"connectors\":[{\"connectorId\":1,\"status\":\"Charging\"}]}]}"));
        verify(devices).update(any(Wrapper.class));
        verify(sessions).selectOne(any(Wrapper.class));
    }
}
