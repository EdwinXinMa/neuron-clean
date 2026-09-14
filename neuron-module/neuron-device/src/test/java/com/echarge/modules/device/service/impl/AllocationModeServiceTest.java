package com.echarge.modules.device.service.impl;

import com.echarge.common.constant.BizConstant;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.common.util.RedisUtil;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.entity.NcOpLog;
import com.echarge.modules.device.service.INcOpLogService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Allocation mode firmware contract and confirmation failure coverage.
 * @author Edwin
 */
class AllocationModeServiceTest {
    private static final String SN = "9EN03L251119Y0036";
    private NcDeviceServiceImpl service;
    private OcppCommandSender sender;
    private INcOpLogService logs;
    private RedisUtil redis;
    private NcDevice device;

    @BeforeEach
    void setUp() {
        service = spy(new NcDeviceServiceImpl());
        sender = mock(OcppCommandSender.class);
        logs = mock(INcOpLogService.class);
        redis = mock(RedisUtil.class);
        ReflectionTestUtils.setField(service, "ocppCommandSender", sender);
        ReflectionTestUtils.setField(service, "opLogService", logs);
        ReflectionTestUtils.setField(service, "redisClient", redis);
        device = new NcDevice();
        device.setSn(SN);
        device.setDeviceType(BizConstant.TYPE_N3_LITE);
        doReturn(device).when(service).getOne(any());
        when(sender.isDeviceConnected(SN)).thenReturn(true);
        when(logs.save(any())).thenReturn(true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Average", "FIFO"})
    void settingWaitsForConfirmationAndOnlySendsMode(String mode) {
        when(sender.sendCallAndWait(anyString(), anyString(), anyString(), eq(10L))).thenAnswer(call -> {
            verifyNoInteractions(logs);
            return "{\"status\":\"Accepted\"}";
        });
        service.setAllocationMode(SN, mode, "app");
        JsonObject dataTransfer = sentPayload("SetAllocationMode");
        JsonObject payload = JsonParser.parseString(dataTransfer.get("data").getAsString()).getAsJsonObject();
        assertEquals(1, payload.size());
        assertEquals(mode, payload.get("AllocationMode").getAsString());
        verify(logs).save(argThat(log -> NcOpLog.SUCCESS.equals(log.getOpResult())
                && NcOpLog.DLM_CONFIG.equals(log.getOpType()) && log.getFailReason() == null));
        verify(service, never()).updateById(any(NcDevice.class));
        verifyNoInteractions(redis);
    }

    @Test
    void queryReadsLatestDeviceValueWithoutCacheOrDefault() {
        when(sender.sendCallAndWait(anyString(), anyString(), anyString(), eq(10L)))
                .thenReturn("{\"status\":\"Accepted\",\"AllocationMode\":\"FIFO\"}",
                        "{\"status\":\"Accepted\",\"AllocationMode\":\"Average\"}");
        assertEquals("FIFO", service.getAllocationMode(SN));
        assertEquals("", sentPayload("GetAllocationMode").get("data").getAsString());
        assertEquals("Average", service.getAllocationMode(SN));
        verifyNoInteractions(redis, logs);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "[]", "null", "not-json", "{}", "{\"status\":null}",
            "{\"status\":true}", "{\"status\":[]}", "{\"status\":\"Rejected\"}",
            "{\"status\":\"UnknownMessageId\"}", "{\"status\":\"accepted\"}"})
    void failedOrMalformedConfirmationNeverBecomesSuccess(String response) {
        when(sender.sendCallAndWait(anyString(), anyString(), anyString(), eq(10L))).thenReturn(response);
        assertThrows(NeuronBootException.class, () -> service.setAllocationMode(SN, "FIFO", "app"));
        verify(logs).save(argThat(log -> NcOpLog.FAIL.equals(log.getOpResult()) && log.getFailReason() != null));
        verifyNoInteractions(redis);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"{}", "{\"status\":\"Rejected\"}", "{\"status\":\"Accepted\"}",
            "{\"status\":\"Accepted\",\"data\":{\"AllocationMode\":\"FIFO\"}}",
            "{\"status\":\"Accepted\",\"AllocationMode\":null}",
            "{\"status\":\"Accepted\",\"AllocationMode\":1}",
            "{\"status\":\"Accepted\",\"AllocationMode\":true}",
            "{\"status\":\"Accepted\",\"AllocationMode\":[\"FIFO\"]}",
            "{\"status\":\"Accepted\",\"AllocationMode\":\"fifo\"}"})
    void invalidQueryResponsesAreNotDefaultAverage(String response) {
        when(sender.sendCallAndWait(anyString(), anyString(), anyString(), eq(10L))).thenReturn(response);
        assertThrows(NeuronBootException.class, () -> service.getAllocationMode(SN));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"fifo", "AVERAGE", " FIFO", "Other"})
    void invalidModesAreRejectedBeforeSending(String mode) {
        NeuronBootException error = assertThrows(NeuronBootException.class,
                () -> service.setAllocationMode(SN, mode, "app"));
        assertEquals(400, error.getErrCode());
        verifyNoInteractions(sender, logs, redis);
    }

    @Test
    void offlineDeviceDoesNotSendOrReportSuccess() {
        when(sender.isDeviceConnected(SN)).thenReturn(false);
        assertThrows(NeuronBootException.class, () -> service.setAllocationMode(SN, "FIFO", "app"));
        assertThrows(NeuronBootException.class, () -> service.getAllocationMode(SN));
        verify(sender, never()).sendCallAndWait(anyString(), anyString(), anyString(), anyLong());
        verify(logs).save(argThat(log -> NcOpLog.FAIL.equals(log.getOpResult())));
    }

    @Test
    void absentDeviceAndChildPileAreRejected() {
        doReturn(null).when(service).getOne(any());
        assertEquals(404, assertThrows(NeuronBootException.class, () -> service.getAllocationMode(SN)).getErrCode());
        doReturn(device).when(service).getOne(any());
        device.setParentDeviceId("parent");
        assertEquals(400, assertThrows(NeuronBootException.class, () -> service.getAllocationMode(SN)).getErrCode());
        verifyNoInteractions(sender);
    }

    @Test
    void auditFailureDoesNotReplaceConfirmedDeviceResult() {
        when(sender.sendCallAndWait(anyString(), anyString(), anyString(), eq(10L)))
                .thenReturn("{\"status\":\"Accepted\"}");
        when(logs.save(any())).thenThrow(new IllegalStateException("database unavailable"));
        assertDoesNotThrow(() -> service.setAllocationMode(SN, "FIFO", "app"));
    }

    private JsonObject sentPayload(String command) {
        ArgumentCaptor<String> wire = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> id = ArgumentCaptor.forClass(String.class);
        verify(sender).sendCallAndWait(eq(SN), wire.capture(), id.capture(), eq(10L));
        JsonArray call = JsonParser.parseString(wire.getValue()).getAsJsonArray();
        assertEquals(2, call.get(0).getAsInt());
        assertEquals(id.getValue(), call.get(1).getAsString());
        assertEquals("DataTransfer", call.get(2).getAsString());
        JsonObject dataTransfer = call.get(3).getAsJsonObject();
        assertEquals("AlwaysControl", dataTransfer.get("vendorId").getAsString());
        assertEquals(command, dataTransfer.get("messageId").getAsString());
        return dataTransfer;
    }
}
