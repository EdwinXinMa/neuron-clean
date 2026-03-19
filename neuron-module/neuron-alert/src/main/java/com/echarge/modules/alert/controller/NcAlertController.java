package com.echarge.modules.alert.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.common.system.query.QueryGenerator;
import com.echarge.modules.alert.entity.NcAlert;
import com.echarge.modules.alert.service.INcAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "告警管理")
@RestController
@RequestMapping("/alert/ncAlert")
public class NcAlertController {

    @Autowired
    private INcAlertService ncAlertService;

    @Operation(summary = "分页查询告警列表")
    @GetMapping("/list")
    public Result<IPage<NcAlert>> list(NcAlert ncAlert,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) {
        QueryWrapper<NcAlert> queryWrapper = QueryGenerator.initQueryWrapper(ncAlert, req.getParameterMap());
        queryWrapper.orderByDesc("alert_time");
        Page<NcAlert> page = new Page<>(pageNo, pageSize);
        IPage<NcAlert> pageList = ncAlertService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "查询未处理告警数量")
    @GetMapping("/activeCount")
    public Result<Long> activeCount() {
        return Result.OK(ncAlertService.countActive());
    }

    @Operation(summary = "手动处理告警")
    @PostMapping("/resolve")
    public Result<String> resolve(@RequestParam String id,
                                  @RequestParam(required = false) String remark) {
        // TODO: 从登录用户信息中获取操作人
        ncAlertService.resolveAlertManual(id, "admin", remark);
        return Result.OK("处理成功");
    }

    @Operation(summary = "根据设备序列号查询告警记录")
    @GetMapping("/listByDevice")
    public Result<?> listByDevice(@RequestParam String deviceSn) {
        return Result.OK(ncAlertService.list(
                new QueryWrapper<NcAlert>().eq("device_sn", deviceSn).orderByDesc("alert_time")
        ));
    }
}
