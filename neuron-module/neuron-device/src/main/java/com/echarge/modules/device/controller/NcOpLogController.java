package com.echarge.modules.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.modules.device.entity.NcOpLog;
import com.echarge.modules.device.service.INcOpLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 设备操作日志
 */
@Slf4j
@Tag(name = "操作日志")
@RestController
@RequestMapping("/oplog")
public class NcOpLogController {

    @Autowired
    private INcOpLogService ncOpLogService;

    /**
     * 全局操作日志列表（分页 + 筛选）
     */
    @Operation(summary = "操作日志列表")
    @GetMapping("/list")
    public Result<IPage<NcOpLog>> list(
            @RequestParam(required = false) String deviceSn,
            @RequestParam(required = false) String opType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        LambdaQueryWrapper<NcOpLog> query = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(deviceSn)) {
            query.like(NcOpLog::getDeviceSn, deviceSn);
        }
        if (StringUtils.isNotBlank(opType) && !"all".equals(opType)) {
            query.eq(NcOpLog::getOpType, opType);
        }
        if (StringUtils.isNotBlank(startTime)) {
            try {
                Date start = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
                query.ge(NcOpLog::getOpTime, start);
            } catch (Exception ignored) {}
        }
        if (StringUtils.isNotBlank(endTime)) {
            try {
                Date end = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime);
                query.le(NcOpLog::getOpTime, end);
            } catch (Exception ignored) {}
        }
        query.orderByDesc(NcOpLog::getOpTime);

        return Result.OK(ncOpLogService.page(new Page<>(pageNo, pageSize), query));
    }

    /**
     * 单台设备操作日志（设备详情页 Tab 用）
     */
    @Operation(summary = "设备操作日志")
    @GetMapping("/device/{sn}")
    public Result<List<NcOpLog>> deviceLogs(@PathVariable String sn,
            @RequestParam(defaultValue = "20") Integer limit) {
        List<NcOpLog> logs = ncOpLogService.list(
                new LambdaQueryWrapper<NcOpLog>()
                        .eq(NcOpLog::getDeviceSn, sn)
                        .orderByDesc(NcOpLog::getOpTime)
                        .last("LIMIT " + limit)
        );
        return Result.OK(logs);
    }
}
