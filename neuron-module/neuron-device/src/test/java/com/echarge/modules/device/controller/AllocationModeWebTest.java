package com.echarge.modules.device.controller;

import com.echarge.common.exception.NeuronBootException;
import com.echarge.common.i18n.WebI18n;
import com.echarge.common.system.vo.LoginUser;
import com.echarge.modules.device.service.INcDeviceService;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Web allocation endpoints use the same device service and localized Result envelope.
 * @author Edwin
 */
class AllocationModeWebTest {
    private static final String SN = "9EN03L251119Y0036";
    private static final String URL = "/device/" + SN + "/allocation-mode";
    private INcDeviceService devices;
    private MockMvc mvc;
    private Subject subject;

    @BeforeEach
    void setUp() {
        devices = mock(INcDeviceService.class);
        NcDeviceController controller = new NcDeviceController();
        ReflectionTestUtils.setField(controller, "ncDeviceService", devices);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en");
        ReflectionTestUtils.setField(controller, "request", request);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        subject = mock(Subject.class);
        LoginUser user = new LoginUser();
        user.setUsername("operator");
        when(subject.getPrincipal()).thenReturn(user);
        ThreadContext.bind(subject);
    }

    @AfterEach
    void clearSubject() {
        ThreadContext.unbindSubject();
    }

    @Test
    void getUsesLiveServiceAndResultEnvelope() throws Exception {
        when(devices.getAllocationMode(SN)).thenReturn("FIFO");
        mvc.perform(get(URL)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.AllocationMode").value("FIFO"));
        verify(devices).getAllocationMode(SN);
    }

    @Test
    void postUsesFirmwareFieldAndCurrentOperator() throws Exception {
        mvc.perform(post(URL).contentType("application/json").content("{\"AllocationMode\":\"FIFO\"}"))
                .andExpect(jsonPath("$.success").value(true));
        verify(devices).setAllocationMode(SN, "FIFO", "operator");
    }

    @Test
    void malformedSettingDoesNotReachDevice() throws Exception {
        for (String body : List.of("{}", "{\"AllocationMode\":1}", "{\"AllocationMode\":true}",
                "{\"AllocationMode\":\"fifo\"}", "{\"AllocationMode\":[\"FIFO\"]}")) {
            mvc.perform(post(URL).contentType("application/json").content(body))
                    .andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.code").value(400));
        }
        verifyNoInteractions(devices);
    }

    @Test
    void noOperatorCannotSetDevice() throws Exception {
        when(subject.getPrincipal()).thenReturn(null);
        mvc.perform(post(URL).contentType("application/json").content("{\"AllocationMode\":\"FIFO\"}"))
                .andExpect(jsonPath("$.code").value(401));
        verifyNoInteractions(devices);
    }

    @Test
    void failureIsTranslatedWithoutDefaultMode() throws Exception {
        when(devices.getAllocationMode(SN)).thenThrow(new NeuronBootException("设备未返回有效响应"));
        mvc.perform(get(URL)).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Device did not return a valid response"))
                .andExpect(jsonPath("$.result.AllocationMode").doesNotExist());
    }

    @Test
    void settingCannotSucceedOnDeviceRejection() throws Exception {
        doThrow(new NeuronBootException("设备拒绝设置电流分配模式"))
                .when(devices).setAllocationMode(anyString(), anyString(), anyString());
        mvc.perform(post(URL).contentType("application/json").content("{\"AllocationMode\":\"FIFO\"}"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Device rejected the allocation mode setting"));
    }

    @Test
    void supportedLanguagesHaveAllocationTranslations() {
        for (String lang : List.of("en", "tw", "es", "pt")) {
            for (String text : List.of("设备未返回有效响应", "设备拒绝设置电流分配模式", "电流分配模式操作失败",
                    "AllocationMode 必须为 Average 或 FIFO", "设备离线，无法操作电流分配模式")) {
                assertNotEquals(text, WebI18n.get(text, lang));
            }
        }
    }
}
