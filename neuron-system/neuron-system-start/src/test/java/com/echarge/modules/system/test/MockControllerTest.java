//package com.echarge.modules.system.test;
//
//import com.echarge.config.NeuronBaseConfig;
//import com.echarge.modules.base.service.BaseCommonService;
//import com.echarge.modules.demo.mock.MockController;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//
///**
// * 单个controller测试
// * @date 2025/4/7 11:21
// */
//@WebMvcTest(value = MockController.class)
//public class MockControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private BaseCommonService baseCommonService;
//    @MockBean
//    private NeuronBaseConfig neuronBaseConfig;
//
//    @Test
//    public void testSave() throws Exception {
//        mockMvc.perform(get("/mock/api/json/area"))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isOk());
//    }
//
//}
