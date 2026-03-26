package com.echarge.modules.alert.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.modules.alert.entity.NcAlert;
import com.echarge.modules.alert.service.INcAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 告警记录（v3.0 只读）
 * @author Edwin
 */
@Slf4j
@Tag(name = "告警记录")
@RestController
@RequestMapping("/alert")
public class NcAlertController {

    @Autowired
    private INcAlertService ncAlertService;

    /**
     * 告警列表（分页 + 筛选）
     * 支持：级别筛选、设备SN搜索、时间范围
     */
    @Operation(summary = "告警列表")
    @GetMapping("/list")
    public Result<IPage<NcAlert>> list(
            @RequestParam(required = false) String deviceSn,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        LambdaQueryWrapper<NcAlert> query = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(deviceSn)) {
            query.like(NcAlert::getDeviceSn, deviceSn);
        }
        if (StringUtils.isNotBlank(alertLevel)) {
            query.eq(NcAlert::getAlertLevel, alertLevel);
        }
        // 时间范围筛选
        if (StringUtils.isNotBlank(startTime)) {
            try {
                Date start = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
                query.ge(NcAlert::getAlertTime, start);
            } catch (Exception ignored) {}
        }
        if (StringUtils.isNotBlank(endTime)) {
            try {
                Date end = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime);
                query.le(NcAlert::getAlertTime, end);
            } catch (Exception ignored) {}
        }

        query.orderByDesc(NcAlert::getAlertTime);

        IPage<NcAlert> page = ncAlertService.page(new Page<>(pageNo, pageSize), query);
        return Result.ok(page);
    }

    /**
     * 导航角标：最近 24h 严重+重要告警数
     */
    @Operation(summary = "导航角标告警数")
    @GetMapping("/badge")
    public Result<Long> badge() {
        return Result.ok(ncAlertService.countRecentCritical());
    }
}
