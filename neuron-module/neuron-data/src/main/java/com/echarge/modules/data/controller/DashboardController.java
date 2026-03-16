package com.echarge.modules.data.controller;

import com.echarge.common.api.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "数据看板")
@RestController
@RequestMapping("/data/dashboard")
public class DashboardController {

    @Operation(summary = "获取概览数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalDevices", 0);
        data.put("onlineDevices", 0);
        data.put("totalStations", 0);
        data.put("todayEnergy", 0);
        return Result.OK(data);
    }
}
