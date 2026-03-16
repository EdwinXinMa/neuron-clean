package com.echarge.modules.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.common.system.query.QueryGenerator;
import com.echarge.modules.data.entity.AlarmRecord;
import com.echarge.modules.data.service.IAlarmRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "告警管理")
@RestController
@RequestMapping("/data/alarm")
public class AlarmController {

    @Autowired
    private IAlarmRecordService alarmRecordService;

    @Operation(summary = "分页查询告警记录")
    @GetMapping("/list")
    public Result<IPage<AlarmRecord>> list(AlarmRecord alarmRecord,
                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                            HttpServletRequest req) {
        QueryWrapper<AlarmRecord> queryWrapper = QueryGenerator.initQueryWrapper(alarmRecord, req.getParameterMap());
        Page<AlarmRecord> page = new Page<>(pageNo, pageSize);
        IPage<AlarmRecord> pageList = alarmRecordService.page(page, queryWrapper);
        return Result.OK(pageList);
    }
}
