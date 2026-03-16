package com.echarge.modules.station.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.common.system.query.QueryGenerator;
import com.echarge.modules.station.entity.Station;
import com.echarge.modules.station.service.IStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "站点管理")
@RestController
@RequestMapping("/station")
public class StationController {

    @Autowired
    private IStationService stationService;

    @Operation(summary = "分页查询站点列表")
    @GetMapping("/list")
    public Result<IPage<Station>> list(Station station,
                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                        HttpServletRequest req) {
        QueryWrapper<Station> queryWrapper = QueryGenerator.initQueryWrapper(station, req.getParameterMap());
        Page<Station> page = new Page<>(pageNo, pageSize);
        IPage<Station> pageList = stationService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加站点")
    @PostMapping("/add")
    public Result<String> add(@RequestBody Station station) {
        stationService.save(station);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑站点")
    @PutMapping("/edit")
    public Result<String> edit(@RequestBody Station station) {
        stationService.updateById(station);
        return Result.OK("编辑成功!");
    }

    @Operation(summary = "删除站点")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String id) {
        stationService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "根据ID查询站点")
    @GetMapping("/queryById")
    public Result<Station> queryById(@RequestParam String id) {
        Station station = stationService.getById(id);
        return Result.OK(station);
    }
}
